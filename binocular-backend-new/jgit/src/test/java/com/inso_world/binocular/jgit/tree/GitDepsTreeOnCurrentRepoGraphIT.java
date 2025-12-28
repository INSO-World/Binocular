package com.inso_world.binocular.jgit.tree;

import com.inso_world.binocular.jgit.JGitGitIndexer;
import com.inso_world.binocular.model.Branch;
import com.inso_world.binocular.model.Commit;
import com.inso_world.binocular.model.Repository;
import com.inso_world.binocular.model.git.GitDepsTree;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Integration Test that builds a deps tree for your local repo and writes an ASCII graph file.
 *
 * VM options:
 * -Dbinocular.repoPath=/path/to/repo   (default: user.dir)
 * -Dbinocular.branch=my-branch        (default: main, else master, else first branch)
 * -Dbinocular.commitLimit=250         (default: 250)
 */
public class GitDepsTreeOnCurrentRepoGraphIT {

    @Test
    void writesGraphFileForLocalBranch() throws Exception {
        Path start = Paths.get(System.getProperty("binocular.repoPath", System.getProperty("user.dir")))
                .toAbsolutePath()
                .normalize();

        JGitGitIndexer indexer = new JGitGitIndexer();

        Repository repo;
        try {
            repo = indexer.findRepo(start);
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "No git repo found from: " + start);
            return;
        }

        List<Branch> branches = indexer.findAllBranches(repo);
        Assumptions.assumeTrue(!branches.isEmpty(), "No branches found");

        String desired = System.getProperty("binocular.branch");
        Branch branch = pickBranch(branches, desired);
        Assumptions.assumeTrue(branch != null, "Branch not found: " + desired);

        int limit = parseIntOrDefault(System.getProperty("binocular.commitLimit"), 10000);
        List<Commit> commits = indexer.traverseBranch(repo, branch);
        if (commits.size() > limit) {
            commits = commits.subList(0, limit);
        }

        GitDepsTreeBuilder builder = new GitDepsTreeBuilder();
        GitDepsTree tree = builder.build(repo, commits);

        GitDepsTreeAsciiGraphRenderer renderer = new GitDepsTreeAsciiGraphRenderer();
        String ascii = renderer.render(tree, commits);

        Path projectRoot = guessProjectRoot(start);
        Path outDir = projectRoot.resolve("cli").resolve("target").resolve("git-deps-trees");
        Files.createDirectories(outDir);

        Path outFile = outDir.resolve("deps-tree-graph-" + sanitize(branch.getName()) + ".txt");
        Files.writeString(outFile, ascii, StandardCharsets.UTF_8);

        System.out.println("Wrote graph to: " + outFile.toAbsolutePath());

        Assertions.assertTrue(tree.getNodes() != null && !tree.getNodes().isEmpty(), "Tree has no nodes");
        Assertions.assertTrue(tree.getRows() >= 1, "Rows must be >= 1");
        Assertions.assertTrue(tree.getColumns() >= 1, "Columns must be >= 1");
    }

    private static Branch pickBranch(List<Branch> branches, String desired) {
        if (desired != null && !desired.isBlank()) {
            Optional<Branch> exact = branches.stream().filter(b -> desired.equals(b.getName())).findFirst();
            if (exact.isPresent()) return exact.get();
        }

        Optional<Branch> main = branches.stream().filter(b -> "main".equals(b.getName())).findFirst();
        if (main.isPresent()) return main.get();

        Optional<Branch> master = branches.stream().filter(b -> "master".equals(b.getName())).findFirst();
        if (master.isPresent()) return master.get();

        branches.sort(Comparator.comparing(Branch::getName, Comparator.nullsLast(String::compareToIgnoreCase)));
        return branches.get(0);
    }

    private static int parseIntOrDefault(String s, int def) {
        try {
            if (s == null) return def;
            return Integer.parseInt(s.trim());
        } catch (Exception ignored) {
            return def;
        }
    }

    private static String sanitize(String s) {
        if (s == null || s.isBlank()) return "unknown";
        return s.replaceAll("[^a-zA-Z0-9._-]+", "_");
    }

    private static Path guessProjectRoot(Path start) {
        Path p = start;
        for (int i = 0; i < 14 && p != null; i++) {
            if (Files.exists(p.resolve("pom.xml")) && Files.isDirectory(p.resolve("cli"))) {
                return p;
            }
            p = p.getParent();
        }
        return start;
    }
}
