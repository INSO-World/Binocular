package com.inso_world.binocular.jgit.tree;

import com.inso_world.binocular.jgit.JGitGitIndexer;
import com.inso_world.binocular.model.Branch;
import com.inso_world.binocular.model.Commit;
import com.inso_world.binocular.model.Repository;
import com.inso_world.binocular.model.git.EdgeType;
import com.inso_world.binocular.model.git.GitDepsTree;
import com.inso_world.binocular.model.git.GitTreeEdge;
import com.inso_world.binocular.model.git.GitTreeNode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GitDepsTreeBuilderTest {


    @Test
    void buildsTreeWithMergeEdgesAndBranchNames() throws Exception {
        String shaA;
        String shaB;
        String shaC;
        String shaD;
        String shaMerge;


        Path repoDir = createPersistentRepoDir();

        try (Git git = Git.init()
                .setDirectory(repoDir.toFile())
                .setInitialBranch("main")
                .call()) {

            shaA = commit(git, "file.txt", "a", "A").getId().getName();
            shaB = commit(git, "file.txt", "b", "B").getId().getName();

            git.branchCreate().setName("feature").call();
            git.checkout().setName("feature").call();
            // Use a different file on the feature branch to avoid merge conflicts.
            shaC = commit(git, "feature.txt", "c", "C on feature").getId().getName();

            git.checkout().setName("main").call();
            // Another file on main so the merge produces a clean merge commit.
            shaD = commit(git, "main.txt", "d", "D on main").getId().getName();

            Ref featureRef = git.getRepository().findRef("feature");
            assertNotNull(featureRef);

            MergeResult mergeResult = git.merge()
                    .include(featureRef)
                    .setFastForward(MergeCommand.FastForwardMode.NO_FF)
                    .setMessage("merge feature")
                    .call();

            assertTrue(mergeResult.getMergeStatus().isSuccessful());

            shaMerge = git.getRepository().resolve("HEAD").getName();
            assertNotEquals(shaD, shaMerge);
        }

        JGitGitIndexer indexer = new JGitGitIndexer();
        Repository repo = indexer.findRepo(repoDir);

        List<Branch> branches = indexer.findAllBranches(repo);
        Branch main = branches.stream().filter(b -> "main".equals(b.getName())).findFirst().orElseThrow();
        Branch feature = branches.stream().filter(b -> "feature".equals(b.getName())).findFirst().orElseThrow();

        List<Commit> mainCommits = indexer.traverseBranch(repo, main);
        indexer.traverseBranch(repo, feature);

        GitDepsTreeBuilder builder = new GitDepsTreeBuilder();
        GitDepsTree tree = builder.build(repo, mainCommits);

        assertEquals(5, tree.getNodes().size());
        assertEquals(5, tree.getRows());
        assertTrue(tree.getColumns() >= 2);

        GitTreeNode featureNode = tree.getNodes().stream()
                .filter(n -> shaC.equals(n.getCommitSha()))
                .findFirst()
                .orElseThrow();
        assertTrue(featureNode.getBranchNames().contains("feature"));

        List<GitTreeEdge> fromMerge = tree.getEdges().stream()
                .filter(e -> shaMerge.equals(e.getFromCommitSha()))
                .toList();
        assertEquals(2, fromMerge.size());
        System.out.println("Test repo dir: " + repoDir.toAbsolutePath());

        assertTrue(fromMerge.stream().anyMatch(e -> e.getType() == EdgeType.FIRST_PARENT && shaD.equals(e.getToCommitSha())));
        assertTrue(fromMerge.stream().anyMatch(e -> e.getType() == EdgeType.MERGE_PARENT && shaC.equals(e.getToCommitSha())));

        Commit mergeCommit = mainCommits.stream().filter(c -> shaMerge.equals(c.getSha())).findFirst().orElseThrow();
        assertEquals(List.of(shaD, shaC), mergeCommit.getParentShasOrdered());

        assertTrue(mainCommits.stream().anyMatch(c -> shaA.equals(c.getSha())));
        assertTrue(mainCommits.stream().anyMatch(c -> shaB.equals(c.getSha())));
    }

    private Path createPersistentRepoDir() throws Exception {
        Path testClassesDir = Path.of(
                GitDepsTreeBuilderTest.class.getProtectionDomain().getCodeSource().getLocation().toURI()
        );

        Path moduleDir = testClassesDir;
        while (moduleDir != null && !Files.exists(moduleDir.resolve("pom.xml"))) {
            moduleDir = moduleDir.getParent();
        }
        if (moduleDir == null) {
            throw new IllegalStateException("Could not locate jgit module directory");
        }

        Path baseDir = moduleDir.resolve("target").resolve("test-repos");
        Files.createDirectories(baseDir);

        Path repoDir = baseDir.resolve("repo-" + System.currentTimeMillis());
        Files.createDirectories(repoDir);

        System.out.println("Persistent test repo dir: " + repoDir.toAbsolutePath());
        return repoDir;
    }


    private RevCommit commit(Git git, String path, String content, String message) throws Exception {
        Path p = git.getRepository().getWorkTree().toPath().resolve(path);
        Files.writeString(p, content, StandardCharsets.UTF_8);
        git.add().addFilepattern(path).call();
        return git.commit()
                .setMessage(message)
                .setAuthor("Tester", "tester@example.com")
                .setCommitter("Tester", "tester@example.com")
                .call();
    }
}
