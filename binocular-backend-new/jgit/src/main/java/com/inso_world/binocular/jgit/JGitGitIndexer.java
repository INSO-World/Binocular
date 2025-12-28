package com.inso_world.binocular.jgit;

import com.inso_world.binocular.core.index.GitIndexer;
import com.inso_world.binocular.model.Branch;
import com.inso_world.binocular.model.Commit;
import com.inso_world.binocular.model.Repository;
import com.inso_world.binocular.model.User;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.lib.PersonIdent;
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

    @Override
    public Repository findRepo(Path path) {
        try (org.eclipse.jgit.lib.Repository jgitRepo = openRepository(path)) {
            File workTree = jgitRepo.getWorkTree();
            String repoRoot = (workTree != null ? workTree : jgitRepo.getDirectory()).getAbsolutePath();
            return Repository.create(repoRoot, null);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot open git repository at " + path, e);
        }
    }

    @Override
    public List<Commit> traverseBranch(Repository repo, Branch branch) {
        Objects.requireNonNull(repo, "repo");
        Objects.requireNonNull(branch, "branch");

        try (org.eclipse.jgit.lib.Repository jgitRepo = openRepository(Path.of(repo.getLocalPath()))) {
            ObjectId head = resolveBranchHead(jgitRepo, branch.getName());
            if (head == null) {
                throw new IllegalArgumentException("Cannot resolve branch head for branch: " + branch.getName());
            }
            List<RevCommit> revCommits = walkFrom(jgitRepo, head, null);
            List<Commit> domainCommits = mapCommits(repo, jgitRepo, revCommits);

            // keep relationships in the domain model in sync
            repo.getBranches().add(branch);
            branch.setRepository(repo);
            branch.getCommits().addAll(domainCommits);
            return domainCommits;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot traverse branch " + branch.getName(), e);
        }
    }

    @NotNull
    @Override
    public List<Branch> findAllBranches(Repository repo) {
        Objects.requireNonNull(repo, "repo");

        try (org.eclipse.jgit.lib.Repository jgitRepo = openRepository(Path.of(repo.getLocalPath()))) {
            String headFull = safeGetFullBranch(jgitRepo);
            try (Git git = new Git(jgitRepo)) {
                List<Ref> refs = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();

                List<Branch> result = new ArrayList<>();
                for (Ref ref : refs) {
                    // Only local branches by default, but keep a defensive filter.
                    if (!ref.getName().startsWith(Constants.R_HEADS)) {
                        continue;
                    }

                    String shortName = org.eclipse.jgit.lib.Repository.shortenRefName(ref.getName());
                    boolean active = headFull != null && headFull.equals(ref.getName());
                    String latestCommit = ref.getObjectId() != null ? ref.getObjectId().getName() : null;

                    Branch b = Branch.create(shortName, repo, active, false, latestCommit);
                    result.add(b);
                }

                // Stable ordering: active first, then alphabetically.
                result.sort(Comparator
                        .comparing(Branch::getActive).reversed()
                        .thenComparing(Branch::getName, Comparator.nullsLast(String::compareToIgnoreCase)));
                return result;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Cannot list branches for repo " + repo.getLocalPath(), e);
        }
    }

    @Override
    public String findCommit(Repository repo, String hash) {
        Objects.requireNonNull(repo, "repo");
        Objects.requireNonNull(hash, "hash");
        try (org.eclipse.jgit.lib.Repository jgitRepo = openRepository(Path.of(repo.getLocalPath()))) {
            ObjectId id = resolveObject(jgitRepo, hash);
            if (id == null) {
                throw new IllegalArgumentException("Cannot resolve commit: " + hash);
            }
            return id.getName();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot resolve commit " + hash, e);
        }
    }

    @Override
    public List<Commit> traverse(Repository repo, String sourceCmt, String trgtCmt) {
        Objects.requireNonNull(repo, "repo");
        Objects.requireNonNull(sourceCmt, "sourceCmt");

        try (org.eclipse.jgit.lib.Repository jgitRepo = openRepository(Path.of(repo.getLocalPath()))) {
            ObjectId source = resolveObject(jgitRepo, sourceCmt);
            if (source == null) {
                throw new IllegalArgumentException("Cannot resolve source commit: " + sourceCmt);
            }

            ObjectId target = null;
            if (trgtCmt != null) {
                target = resolveObject(jgitRepo, trgtCmt);
                if (target == null) {
                    throw new IllegalArgumentException("Cannot resolve target commit: " + trgtCmt);
                }
            }

            List<RevCommit> revCommits = walkFrom(jgitRepo, source, target);
            return mapCommits(repo, jgitRepo, revCommits);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot traverse commits", e);
        }
    }

    private static org.eclipse.jgit.lib.Repository openRepository(Path path) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        builder.findGitDir(path.toFile());
        if (builder.getGitDir() == null) {
            // If the path is already the .git directory, findGitDir may return null.
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

    private static String safeGetFullBranch(org.eclipse.jgit.lib.Repository repo) {
        try {
            return repo.getFullBranch();
        } catch (IOException e) {
            return null;
        }
    }

    private static ObjectId resolveBranchHead(org.eclipse.jgit.lib.Repository repo, String branchName) throws IOException {
        // Best effort resolution for common formats.
        ObjectId id = repo.resolve(Constants.R_HEADS + branchName);
        if (id != null) return id;
        id = repo.resolve(branchName);
        if (id != null) return id;
        id = repo.resolve(Constants.R_REMOTES + branchName);
        if (id != null) return id;
        id = repo.resolve(Constants.HEAD);
        return id;
    }

    private static ObjectId resolveObject(org.eclipse.jgit.lib.Repository repo, String rev) throws IOException {
        try {
            return repo.resolve(rev);
        } catch (AmbiguousObjectException e) {
            // Prefer exact hashes only.
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

    /**
     * Two pass mapping that preserves commit identity and wires parent relationships.
     */
    private static List<Commit> mapCommits(Repository domainRepo,
                                          org.eclipse.jgit.lib.Repository jgitRepo,
                                          List<RevCommit> revCommits) throws IOException {

        Map<String, Commit> commitsBySha = new LinkedHashMap<>();
        Map<String, List<String>> parentShasByCommit = new HashMap<>();
        Map<String, User> usersByKey = new HashMap<>();

        // Seed from existing repository state (important when we mix sources).
        for (Commit c : domainRepo.getCommits()) {
            commitsBySha.putIfAbsent(c.getSha(), c);
        }

        // Pass 1: create commits with scalar fields.
        for (RevCommit rc : revCommits) {
            String sha = rc.getId().getName();

            Commit existing = commitsBySha.get(sha);
            if (existing == null) {
                LocalDateTime commitDt = toLocalDateTime(rc.getCommitterIdent());
                LocalDateTime authorDt = toLocalDateTime(rc.getAuthorIdent());

                Commit created = Commit.create(
                        sha,
                        commitDt,
                        domainRepo,
                        authorDt,
                        rc.getFullMessage(),
                        null
                );
                commitsBySha.put(sha, created);
                domainRepo.getCommits().add(created);
            }

            List<String> parentShas = new ArrayList<>();
            for (RevCommit p : rc.getParents()) {
                parentShas.add(p.getId().getName());
            }
            parentShasByCommit.put(sha, parentShas);
        }

        // Pass 2: wire author/committer and parents.
        for (RevCommit rc : revCommits) {
            String sha = rc.getId().getName();
            Commit commit = commitsBySha.get(sha);
            if (commit == null) {
                continue;
            }

            // users (optional)
            PersonIdent committer = rc.getCommitterIdent();
            if (committer != null) {
                String key = domainRepo.getLocalPath() + "," + committer.getEmailAddress();
                User u = usersByKey.computeIfAbsent(key, k -> User.create(committer.getName(), committer.getEmailAddress(), domainRepo));
                domainRepo.getUser().add(u);
                try {
                    commit.setCommitter(u);
                } catch (IllegalArgumentException ignore) {
                    // already set by domain invariants
                }
            }

            PersonIdent author = rc.getAuthorIdent();
            if (author != null) {
                String key = domainRepo.getLocalPath() + "," + author.getEmailAddress();
                User u = usersByKey.computeIfAbsent(key, k -> User.create(author.getName(), author.getEmailAddress(), domainRepo));
                domainRepo.getUser().add(u);
                try {
                    commit.setAuthor(u);
                } catch (IllegalArgumentException ignore) {
                    // already set by domain invariants
                }
            }

            List<String> parentShas = parentShasByCommit.getOrDefault(sha, List.of());
            // keep JGit parent order for later tree rendering
            try {
                commit.setParentShasOrdered(parentShas);
            } catch (Exception ignore) {
                // best effort only
            }
            for (String pSha : parentShas) {
                Commit parent = commitsBySha.get(pSha);
                if (parent == null) {
                    // placeholder
                    LocalDateTime placeholderDt = commit.getCommitDateTime();
                    if (placeholderDt == null) {
                        placeholderDt = LocalDateTime.now();
                    }
                    parent = Commit.create(pSha, placeholderDt, domainRepo, null, null, null);
                    commitsBySha.put(pSha, parent);
                    domainRepo.getCommits().add(parent);
                }
                commit.getParents().add(parent);
            }
        }

        // Keep output order as provided by RevWalk.
        List<Commit> out = new ArrayList<>();
        for (RevCommit rc : revCommits) {
            Commit c = commitsBySha.get(rc.getId().getName());
            if (c != null) out.add(c);
        }
        return out;
    }

    private static LocalDateTime toLocalDateTime(PersonIdent ident) {
        if (ident == null || ident.getWhen() == null) {
            return LocalDateTime.now();
        }
        return ident.getWhen().toInstant().atZone(ZoneOffset.UTC).toLocalDateTime();
    }
}
