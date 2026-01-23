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
 * The Kotlin domain model stays in the domain module.
 * This class maps JGit commits and branches to those Kotlin models.
 */
@Service
public class JGitGitIndexer implements GitIndexer {

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

        try (org.eclipse.jgit.lib.Repository jgitRepo = openRepository(Path.of(repo.getLocalPath()))) {
            ObjectId head = resolveBranchHead(jgitRepo, branchName);
            if (head == null) {
                throw new JGitException.ReferenceException("Cannot resolve branch head for branch: " + branchName);
            }

            List<RevCommit> revCommits = walkFrom(jgitRepo, head, null);
            List<Commit> domainCommits = mapCommits(repo, jgitRepo, revCommits);

            // Find the head commit from the mapped commits
            Commit headCommit = domainCommits.stream()
                    .filter(c -> c.getSha().equals(head.getName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Head commit not found in traversal"));

            // Determine branch category and names
            String normalizedName = normalizeBranchName(branchName);
            String fullName = normalizeFullName(jgitRepo, branchName);
            ReferenceCategory category = determineBranchCategory(branchName);

            // Create branch - this registers with repository
            Branch branch = new Branch(normalizedName, fullName, category, repo, headCommit);

            return new Pair<>(branch, domainCommits);
        } catch (IOException e) {
            throw new JGitException.TraversalException("Cannot traverse branch " + branchName, e);
        }
    }

    @NotNull
    @Override
    public List<Branch> findAllBranches(@NotNull Repository repo) {
        Objects.requireNonNull(repo, "repo");

        try (org.eclipse.jgit.lib.Repository jgitRepo = openRepository(Path.of(repo.getLocalPath()))) {
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
                    Commit headCommit = findCommit(repo, headId.getName());

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

        try (org.eclipse.jgit.lib.Repository jgitRepo = openRepository(Path.of(repo.getLocalPath()))) {
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

                // Create the commit
                return createCommit(repo, revCommit);
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

        try (org.eclipse.jgit.lib.Repository jgitRepo = openRepository(Path.of(repo.getLocalPath()))) {
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

            List<RevCommit> revCommits = walkFrom(jgitRepo, sourceId, targetId);
            return mapCommits(repo, jgitRepo, revCommits);
        } catch (IOException e) {
            throw new JGitException.TraversalException("Cannot traverse commits", e);
        }
    }

    // ======================== Helper methods ========================

    private static org.eclipse.jgit.lib.Repository openRepository(Path path) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        builder.findGitDir(path.toFile());
        if (builder.getGitDir() == null) {
            File gitDirCandidate = path.toFile();
            if (gitDirCandidate.isDirectory() && new File(gitDirCandidate, "objects").exists()) {
                builder.setGitDir(gitDirCandidate);
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

    private static List<RevCommit> walkFrom(org.eclipse.jgit.lib.Repository repo, ObjectId start, ObjectId uninteresting) throws IOException {
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
     */
    private List<Commit> mapCommits(Repository repo, org.eclipse.jgit.lib.Repository jgitRepo, List<RevCommit> revCommits) throws IOException {
        Map<String, Commit> commitsBySha = new LinkedHashMap<>();
        Map<String, List<String>> parentShasByCommit = new HashMap<>();
        Map<String, Developer> developersByKey = new HashMap<>();

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
            if (commitsBySha.containsKey(sha)) {
                // Collect parent info even for existing
                parentShasByCommit.put(sha, getParentShas(rc));
                continue;
            }

            Commit commit = createCommit(repo, rc, developersByKey);
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
                            parent = createCommit(repo, parentRc, developersByKey);
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

    private Commit createCommit(Repository repo, RevCommit rc) {
        Map<String, Developer> developersByKey = new HashMap<>();
        for (Developer d : repo.getDevelopers()) {
            developersByKey.put(d.getGitSignature(), d);
        }
        return createCommit(repo, rc, developersByKey);
    }

    private Commit createCommit(Repository repo, RevCommit rc, Map<String, Developer> developersByKey) {
        String sha = rc.getId().getName();

        PersonIdent authorIdent = rc.getAuthorIdent();
        PersonIdent committerIdent = rc.getCommitterIdent();

        // Get or create author developer
        Developer author = getOrCreateDeveloper(repo, authorIdent, developersByKey);
        LocalDateTime authorTime = toLocalDateTime(authorIdent);

        // Get or create committer developer
        Developer committer = getOrCreateDeveloper(repo, committerIdent, developersByKey);
        LocalDateTime commitTime = toLocalDateTime(committerIdent);

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