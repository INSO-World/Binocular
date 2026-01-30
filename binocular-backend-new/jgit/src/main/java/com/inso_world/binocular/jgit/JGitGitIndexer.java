package com.inso_world.binocular.jgit;

import com.inso_world.binocular.core.index.GitIndexer;
import com.inso_world.binocular.model.Branch;
import com.inso_world.binocular.model.Commit;
import com.inso_world.binocular.model.Developer;
import com.inso_world.binocular.model.Project;
import com.inso_world.binocular.model.Repository;
import com.inso_world.binocular.model.Signature;
import com.inso_world.binocular.model.vcs.ReferenceCategory;
import kotlin.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * JGit based implementation of {@link GitIndexer}.
 *
 * <h2>Semantics</h2>
 * The Kotlin domain model stays in the domain module.
 * This class maps JGit commits and branches to those Kotlin models.
 *
 * <h2>Configuration</h2>
 * Supports the following configuration options via {@link JGitConfig}:
 * <ul>
 *   <li>{@code binocular.jgit.skip-merges}: When true, filters out merge commits during traversal</li>
 *   <li>{@code binocular.jgit.use-mailmap}: When true, applies .mailmap transformations to identities</li>
 * </ul>
 *
 * @see JGitConfig
 */
@Service
@Profile("jgit")
public class JGitGitIndexer implements GitIndexer {

    private static final Logger logger = LoggerFactory.getLogger(JGitGitIndexer.class);

    private final JGitConfig config;

    public JGitGitIndexer(JGitConfig config) {
        this.config = config;
    }

    @NotNull
    @Override
    public Repository findRepo(@NotNull Path path, @NotNull Project project) {
        try (org.eclipse.jgit.lib.Repository jgitRepo = openRepository(path)) {
            String gitDir = jgitRepo.getDirectory().getAbsolutePath();
            return new Repository(gitDir, project);
        } catch (IOException e) {
            throw new JGitException.DiscoverException("Cannot open git repository at " + path, e);
        }
    }

    @NotNull
    @Override
    public Pair<Branch, List<Commit>> traverseBranch(@NotNull Repository repo, @NotNull String branchName) {
        Objects.requireNonNull(repo, "repo");
        Objects.requireNonNull(branchName, "branchName");

        boolean skipMerges = config.getJgit().isSkipMerges();
        boolean useMailmap = config.getJgit().isUseMailmap();
        logger.trace("Traversing {} with skipMerges={}, useMailmap={}", branchName, skipMerges, useMailmap);

        try (org.eclipse.jgit.lib.Repository jgitRepo = openRepository(Path.of(repo.getLocalPath()))) {
            Mailmap mailmap = useMailmap ? Mailmap.read(jgitRepo) : null;

            ObjectId head = resolveBranchHead(jgitRepo, branchName);
            if (head == null) {
                throw new JGitException.ReferenceException("Cannot resolve branch head for branch: " + branchName);
            }

            List<RevCommit> revCommits = walkFrom(jgitRepo, head, null, skipMerges);
            List<Commit> domainCommits = mapCommits(repo, jgitRepo, revCommits, skipMerges, mailmap);

            // Find the head commit from the mapped commits
            Commit headCommit = domainCommits.stream()
                    .filter(c -> c.getSha().equals(head.getName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Head commit not found in traversal"));

            // Determine branch category and names
            String normalizedName = normalizeBranchName(branchName);
            String fullName = normalizeFullName(jgitRepo, branchName);
            ReferenceCategory category = determineBranchCategory(branchName);

            // Check if branch already exists in repository and update its head
            Branch existingBranch = null;
            for (Branch b : repo.getBranches()) {
                if (b.getName().equals(normalizedName)) {
                    existingBranch = b;
                    break;
                }
            }

            Branch branch;
            if (existingBranch != null) {
                // Update existing branch's head
                existingBranch.setHead(headCommit);
                branch = existingBranch;
            } else {
                // Create new branch - this registers with repository
                branch = new Branch(normalizedName, fullName, category, repo, headCommit);
            }

            return new Pair<>(branch, domainCommits);
        } catch (IOException e) {
            throw new JGitException.TraversalException("Cannot traverse branch " + branchName, e);
        }
    }

    @NotNull
    @Override
    public List<Branch> findAllBranches(@NotNull Repository repo) {
        Objects.requireNonNull(repo, "repo");

        boolean useMailmap = config.getJgit().isUseMailmap();

        try (org.eclipse.jgit.lib.Repository jgitRepo = openRepository(Path.of(repo.getLocalPath()))) {
            Mailmap mailmap = useMailmap ? Mailmap.read(jgitRepo) : null;

            try (Git git = new Git(jgitRepo)) {
                List<Ref> refs = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();

                List<Branch> result = new ArrayList<>();
                for (Ref ref : refs) {
                    String refName = ref.getName();
                    String shortName = org.eclipse.jgit.lib.Repository.shortenRefName(refName);
                    ReferenceCategory category = determineBranchCategory(refName);

                    // Get head commit for this branch
                    ObjectId headId = ref.getObjectId();
                    if (headId == null) {
                        continue;
                    }

                    // Find commit - need to get full commit info
                    Commit headCommit = findCommitInternal(repo, jgitRepo, headId.getName(), mailmap);

                    Branch branch = new Branch(shortName, refName, category, repo, headCommit);
                    result.add(branch);
                }

                return result;
            }
        } catch (Exception e) {
            throw new JGitException.DiscoverException("Cannot list branches for repo " + repo.getLocalPath(), e);
        }
    }

    @NotNull
    @Override
    public Commit findCommit(@NotNull Repository repo, @NotNull String hash) {
        Objects.requireNonNull(repo, "repo");
        Objects.requireNonNull(hash, "hash");

        boolean useMailmap = config.getJgit().isUseMailmap();

        try (org.eclipse.jgit.lib.Repository jgitRepo = openRepository(Path.of(repo.getLocalPath()))) {
            Mailmap mailmap = useMailmap ? Mailmap.read(jgitRepo) : null;
            return findCommitInternal(repo, jgitRepo, hash, mailmap);
        } catch (IOException e) {
            throw new JGitException.TraversalException("Cannot resolve commit " + hash, e);
        }
    }

    /**
     * Internal method to find a commit with optional mailmap support.
     *
     * @param repo      the domain repository
     * @param jgitRepo  the JGit repository
     * @param hash      the commit hash
     * @param mailmap   optional mailmap for identity transformation
     * @return the domain commit
     */
    private Commit findCommitInternal(Repository repo, org.eclipse.jgit.lib.Repository jgitRepo, String hash, Mailmap mailmap) {
        try {
            ObjectId id = resolveObject(jgitRepo, hash);
            if (id == null) {
                throw new JGitException.ReferenceException("Cannot resolve commit: " + hash);
            }

            try (RevWalk walk = new RevWalk(jgitRepo)) {
                RevCommit revCommit = walk.parseCommit(id);

                // Check if already in repository
                for (Commit existing : repo.getCommits()) {
                    if (existing.getSha().equals(id.getName())) {
                        return existing;
                    }
                }

                // Create the commit with mailmap
                return createCommit(repo, revCommit, mailmap);
            }
        } catch (IOException e) {
            throw new JGitException.TraversalException("Cannot resolve commit " + hash, e);
        }
    }

    @NotNull
    @Override
    public List<Commit> traverse(@NotNull Repository repo, @NotNull Commit source, Commit target) {
        Objects.requireNonNull(repo, "repo");
        Objects.requireNonNull(source, "source");

        boolean useMailmap = config.getJgit().isUseMailmap();

        try (org.eclipse.jgit.lib.Repository jgitRepo = openRepository(Path.of(repo.getLocalPath()))) {
            Mailmap mailmap = useMailmap ? Mailmap.read(jgitRepo) : null;

            ObjectId sourceId = resolveObject(jgitRepo, source.getSha());
            if (sourceId == null) {
                throw new JGitException.ReferenceException("Cannot resolve source commit: " + source.getSha());
            }

            ObjectId targetId = null;
            if (target != null) {
                targetId = resolveObject(jgitRepo, target.getSha());
                if (targetId == null) {
                    throw new JGitException.ReferenceException("Cannot resolve target commit: " + target.getSha());
                }
            }

            // Note: traverse does not use skip-merges to maintain consistency with FFI implementation
            List<RevCommit> revCommits = walkFrom(jgitRepo, sourceId, targetId, false);
            return mapCommits(repo, jgitRepo, revCommits, false, mailmap);
        } catch (IOException e) {
            throw new JGitException.TraversalException("Cannot traverse commits", e);
        }
    }

    // ======================== Helper methods ========================

    private static org.eclipse.jgit.lib.Repository openRepository(Path path) throws IOException {
        // Normalize and resolve to absolute path for consistent git discovery
        File startDir = path.toAbsolutePath().normalize().toFile();

        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        // Check if startDir itself is a .git directory
        if (startDir.getName().equals(".git") && startDir.isDirectory()) {
            builder.setGitDir(startDir);
            if (startDir.getParentFile() != null) {
                builder.setWorkTree(startDir.getParentFile());
            }
        } else {
            builder.findGitDir(startDir);

            // If not found directly, try to find .git in parent directories (like gix does)
            if (builder.getGitDir() == null) {
                File current = startDir;
                while (current != null && builder.getGitDir() == null) {
                    File gitDir = new File(current, ".git");
                    if (gitDir.isDirectory()) {
                        builder.setGitDir(gitDir);
                        builder.setWorkTree(current);
                    } else if (gitDir.isFile()) {
                        // Handle git worktrees (gitdir: pointer)
                        builder.setGitDir(gitDir);
                        builder.setWorkTree(current);
                    } else if (current.isDirectory() && new File(current, "objects").exists()) {
                        // Bare repository
                        builder.setGitDir(current);
                    }
                    current = current.getParentFile();
                }
            }

            // If gitDir is set but workTree isn't, and gitDir ends with .git, set workTree to parent
            if (builder.getGitDir() != null && builder.getWorkTree() == null) {
                File gitDir = builder.getGitDir();
                if (gitDir.getName().equals(".git") && gitDir.getParentFile() != null) {
                    builder.setWorkTree(gitDir.getParentFile());
                }
            }
        }

        if (builder.getGitDir() == null) {
            throw new IllegalArgumentException("No .git directory found starting from: " + path);
        }
        return builder.build();
    }

    private static ObjectId resolveBranchHead(org.eclipse.jgit.lib.Repository repo, String branchName) throws IOException {
        // Try various resolution strategies
        ObjectId id = repo.resolve(branchName);
        if (id != null) return id;

        id = repo.resolve(Constants.R_HEADS + branchName);
        if (id != null) return id;

        id = repo.resolve(Constants.R_REMOTES + branchName);
        if (id != null) return id;

        // Try without refs/ prefix
        if (branchName.startsWith("refs/")) {
            id = repo.resolve(branchName.substring(5));
            if (id != null) return id;
        }

        return null;
    }

    private static ObjectId resolveObject(org.eclipse.jgit.lib.Repository repo, String rev) throws IOException {
        try {
            return repo.resolve(rev);
        } catch (AmbiguousObjectException e) {
            return null;
        }
    }

    /**
     * Walks the commit history from a starting point.
     *
     * @param repo          the JGit repository
     * @param start         the starting commit
     * @param uninteresting optional commit to mark as uninteresting (stop point)
     * @param skipMerges    when true, merge commits (commits with multiple parents) are filtered out
     * @return list of commits in topological order
     */
    private static List<RevCommit> walkFrom(org.eclipse.jgit.lib.Repository repo, ObjectId start, ObjectId uninteresting, boolean skipMerges) throws IOException {
        try (RevWalk walk = new RevWalk(repo)) {
            RevCommit startCommit = walk.parseCommit(start);
            walk.markStart(startCommit);
            if (uninteresting != null) {
                RevCommit endCommit = walk.parseCommit(uninteresting);
                walk.markUninteresting(endCommit);
            }
            walk.sort(RevSort.TOPO);
            walk.sort(RevSort.COMMIT_TIME_DESC, true);

            List<RevCommit> commits = new ArrayList<>();
            for (RevCommit rc : walk) {
                // Skip merge commits if configured
                if (skipMerges && rc.getParentCount() > 1) {
                    continue;
                }
                commits.add(rc);
            }
            return commits;
        }
    }

    private static String normalizeBranchName(String refName) {
        String name = refName;
        if (name.startsWith("refs/heads/")) {
            name = name.substring("refs/heads/".length());
        } else if (name.startsWith("refs/remotes/")) {
            name = name.substring("refs/remotes/".length());
        } else if (name.startsWith("refs/tags/")) {
            name = name.substring("refs/tags/".length());
        }
        return name;
    }

    private static String normalizeFullName(org.eclipse.jgit.lib.Repository repo, String branchName) throws IOException {
        // If already a full ref, return as-is
        if (branchName.startsWith("refs/")) {
            return branchName;
        }

        // Try to resolve to determine full name
        Ref ref = repo.findRef(branchName);
        if (ref != null) {
            return ref.getName();
        }

        // Check common prefixes
        if (repo.resolve(Constants.R_HEADS + branchName) != null) {
            return Constants.R_HEADS + branchName;
        }
        if (repo.resolve(Constants.R_REMOTES + branchName) != null) {
            return Constants.R_REMOTES + branchName;
        }
        if (repo.resolve(Constants.R_TAGS + branchName) != null) {
            return Constants.R_TAGS + branchName;
        }

        // Default to heads
        return Constants.R_HEADS + branchName;
    }

    private static ReferenceCategory determineBranchCategory(String refName) {
        if (refName.startsWith("refs/heads/") || refName.startsWith("refs/heads")) {
            return ReferenceCategory.LOCAL_BRANCH;
        } else if (refName.startsWith("refs/remotes/") || refName.contains("origin/") || refName.contains("remote")) {
            return ReferenceCategory.REMOTE_BRANCH;
        } else if (refName.startsWith("refs/tags/")) {
            return ReferenceCategory.TAG;
        } else if (refName.equals("HEAD") || refName.startsWith("HEAD")) {
            return ReferenceCategory.PSEUDO_REF;
        } else {
            return ReferenceCategory.UNKNOWN;
        }
    }

    /**
     * Maps JGit commits to domain commits with full relationship wiring.
     *
     * @param repo        the domain repository
     * @param jgitRepo    the JGit repository for resolving missing parents
     * @param revCommits  the commits to map
     * @param skipMerges  when true, skips wiring parents that are merge commits
     * @param mailmap     optional mailmap for identity transformation, can be null
     * @return mapped domain commits in traversal order
     */
    private List<Commit> mapCommits(Repository repo, org.eclipse.jgit.lib.Repository jgitRepo, List<RevCommit> revCommits, boolean skipMerges, Mailmap mailmap) throws IOException {
        Map<String, Commit> commitsBySha = new LinkedHashMap<>();
        Map<String, List<String>> parentShasByCommit = new HashMap<>();
        Map<String, Developer> developersByKey = new HashMap<>();

        // Track which commits are merge commits for parent filtering
        Set<String> mergeCommitShas = new HashSet<>();

        // Seed from existing repository state
        for (Commit c : repo.getCommits()) {
            commitsBySha.putIfAbsent(c.getSha(), c);
        }
        for (Developer d : repo.getDevelopers()) {
            developersByKey.putIfAbsent(d.getGitSignature(), d);
        }

        // Pass 1: Create commits
        for (RevCommit rc : revCommits) {
            String sha = rc.getId().getName();
            if (rc.getParentCount() > 1) {
                mergeCommitShas.add(sha);
            }
            if (commitsBySha.containsKey(sha)) {
                // Collect parent info even for existing
                parentShasByCommit.put(sha, getParentShas(rc));
                continue;
            }

            Commit commit = createCommit(repo, rc, developersByKey, mailmap);
            commitsBySha.put(sha, commit);
            parentShasByCommit.put(sha, getParentShas(rc));
        }

        // Pass 2: Wire parent relationships
        for (RevCommit rc : revCommits) {
            String sha = rc.getId().getName();
            Commit commit = commitsBySha.get(sha);
            if (commit == null) continue;

            List<String> parentShas = parentShasByCommit.getOrDefault(sha, List.of());
            for (String pSha : parentShas) {
                Commit parent = commitsBySha.get(pSha);
                if (parent == null) {
                    // Parent not in our traversal - need to create placeholder
                    try (RevWalk tempWalk = new RevWalk(jgitRepo)) {
                        ObjectId parentId = jgitRepo.resolve(pSha);
                        if (parentId != null) {
                            RevCommit parentRc = tempWalk.parseCommit(parentId);
                            // Skip creating parent if it's a merge commit and skipMerges is enabled
                            if (skipMerges && parentRc.getParentCount() > 1) {
                                continue;
                            }
                            parent = createCommit(repo, parentRc, developersByKey, mailmap);
                            commitsBySha.put(pSha, parent);
                        }
                    } catch (Exception e) {
                        // Skip if we can't resolve parent
                        continue;
                    }
                }
                if (parent != null && !commit.getParents().contains(parent)) {
                    commit.getParents().add(parent);
                }
            }
        }

        // Maintain output order as provided by RevWalk
        List<Commit> out = new ArrayList<>();
        for (RevCommit rc : revCommits) {
            Commit c = commitsBySha.get(rc.getId().getName());
            if (c != null) out.add(c);
        }
        return out;
    }

    private static List<String> getParentShas(RevCommit rc) {
        List<String> parentShas = new ArrayList<>();
        for (RevCommit p : rc.getParents()) {
            parentShas.add(p.getId().getName());
        }
        return parentShas;
    }

    /**
     * Creates a domain commit from a JGit RevCommit.
     *
     * @param repo      the domain repository
     * @param rc        the JGit RevCommit
     * @param mailmap   optional mailmap for identity transformation
     * @return the domain commit
     */
    private Commit createCommit(Repository repo, RevCommit rc, Mailmap mailmap) {
        Map<String, Developer> developersByKey = new HashMap<>();
        for (Developer d : repo.getDevelopers()) {
            developersByKey.put(d.getGitSignature(), d);
        }
        return createCommit(repo, rc, developersByKey, mailmap);
    }

    /**
     * Creates a domain commit from a JGit RevCommit with developer caching.
     *
     * @param repo            the domain repository
     * @param rc              the JGit RevCommit
     * @param developersByKey cache of developers by git signature
     * @param mailmap         optional mailmap for identity transformation
     * @return the domain commit
     */
    private Commit createCommit(Repository repo, RevCommit rc, Map<String, Developer> developersByKey, Mailmap mailmap) {
        String sha = rc.getId().getName();

        PersonIdent authorIdent = rc.getAuthorIdent();
        PersonIdent committerIdent = rc.getCommitterIdent();

        // Apply mailmap transformation if available
        if (mailmap != null) {
            authorIdent = mailmap.map(authorIdent);
            committerIdent = mailmap.map(committerIdent);
        }

        // Get or create author developer
        Developer author = getOrCreateDeveloper(repo, authorIdent, developersByKey);
        LocalDateTime authorTime = toLocalDateTime(rc.getAuthorIdent()); // Use original ident for time

        // Get or create committer developer
        Developer committer = getOrCreateDeveloper(repo, committerIdent, developersByKey);
        LocalDateTime commitTime = toLocalDateTime(rc.getCommitterIdent()); // Use original ident for time

        // Create signatures
        Signature authorSignature = new Signature(author, authorTime);
        Signature committerSignature = new Signature(committer, commitTime);

        // Create commit
        return new Commit(sha, authorSignature, committerSignature, rc.getFullMessage(), repo);
    }

    private Developer getOrCreateDeveloper(Repository repo, PersonIdent ident, Map<String, Developer> cache) {
        String name = ident.getName();
        String email = ident.getEmailAddress();
        if (email == null || email.isBlank()) {
            email = "unknown@unknown.com";
        }
        if (name == null || name.isBlank()) {
            name = "Unknown";
        }

        String key = name.trim() + " <" + email.trim() + ">";
        Developer developer = cache.get(key);
        if (developer == null) {
            developer = new Developer(name, email, repo);
            cache.put(key, developer);
        }
        return developer;
    }

    private static LocalDateTime toLocalDateTime(PersonIdent ident) {
        if (ident == null || ident.getWhen() == null) {
            return LocalDateTime.now();
        }
        return ident.getWhen().toInstant().atZone(ZoneOffset.UTC).toLocalDateTime();
    }
}