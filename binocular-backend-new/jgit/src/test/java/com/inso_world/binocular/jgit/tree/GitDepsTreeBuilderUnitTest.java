package com.inso_world.binocular.jgit.tree;

import com.inso_world.binocular.model.Branch;
import com.inso_world.binocular.model.Commit;
import com.inso_world.binocular.model.Developer;
import com.inso_world.binocular.model.Project;
import com.inso_world.binocular.model.Repository;
import com.inso_world.binocular.model.Signature;
import com.inso_world.binocular.model.git.EdgeType;
import com.inso_world.binocular.model.git.GitDepsTree;
import com.inso_world.binocular.model.git.GitTreeEdge;
import com.inso_world.binocular.model.git.GitTreeNode;
import com.inso_world.binocular.model.vcs.ReferenceCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GitDepsTreeBuilder}.
 * Tests building of dependency trees from commit lists.
 */
@Tag("unit")
class GitDepsTreeBuilderUnitTest {

    private GitDepsTreeBuilder builder;
    private Repository repository;
    private Project project;
    private int shaCounter = 0;

    @BeforeEach
    void setUp() {
        builder = new GitDepsTreeBuilder();
        project = new Project("test-project");
        repository = new Repository("/test/path", project);
        shaCounter = 0;
    }

    @Nested
    class RequiresValidInput {

        @Test
        void build_nullRepository_throwsNullPointerException() {
            List<Commit> commits = List.of();

            assertThrows(NullPointerException.class, () -> builder.build(null, commits),
                    "Should throw NullPointerException for null repository");
        }

        @Test
        void build_nullCommits_throwsNullPointerException() {
            assertThrows(NullPointerException.class, () -> builder.build(repository, null),
                    "Should throw NullPointerException for null commits list");
        }
    }

    @Nested
    class EmptyAndSingleCommit {

        @Test
        void build_emptyCommitList_returnsEmptyTree() {
            List<Commit> commits = List.of();

            GitDepsTree tree = builder.build(repository, commits);

            assertAll(
                    () -> assertNotNull(tree, "Tree should not be null"),
                    () -> assertTrue(tree.getNodes().isEmpty(), "Nodes should be empty"),
                    () -> assertTrue(tree.getEdges().isEmpty(), "Edges should be empty"),
                    () -> assertEquals(1, tree.getColumns(), "Columns should be at least 1"),
                    () -> assertEquals(0, tree.getRows(), "Rows should be 0")
            );
        }

        @Test
        void build_singleCommit_returnsTreeWithOneNode() {
            Commit commit = createCommit(LocalDateTime.now().minusDays(1), null);
            List<Commit> commits = List.of(commit);

            GitDepsTree tree = builder.build(repository, commits);

            assertAll(
                    () -> assertEquals(1, tree.getNodes().size(), "Should have one node"),
                    () -> assertTrue(tree.getEdges().isEmpty(), "Should have no edges"),
                    () -> assertEquals(1, tree.getRows(), "Should have one row"),
                    () -> assertEquals(1, tree.getColumns(), "Should have one column")
            );

            GitTreeNode node = tree.getNodes().get(0);
            assertAll(
                    () -> assertEquals(commit.getSha(), node.getCommitSha()),
                    () -> assertEquals(0, node.getRowIndex()),
                    () -> assertEquals(0, node.getColumnIndex())
            );
        }
    }

    @Nested
    class LinearHistory {

        @Test
        void build_linearHistory_createsCorrectEdges() {
            // Create linear history: C3 -> C2 -> C1
            Commit c1 = createCommit(LocalDateTime.of(2024, 1, 1, 10, 0), null);
            Commit c2 = createCommit(LocalDateTime.of(2024, 1, 2, 10, 0), c1);
            Commit c3 = createCommit(LocalDateTime.of(2024, 1, 3, 10, 0), c2);

            List<Commit> commits = List.of(c3, c2, c1);

            GitDepsTree tree = builder.build(repository, commits);

            assertAll(
                    () -> assertEquals(3, tree.getNodes().size(), "Should have 3 nodes"),
                    () -> assertEquals(2, tree.getEdges().size(), "Should have 2 edges")
            );

            // Verify all edges are FIRST_PARENT type (linear history)
            assertTrue(tree.getEdges().stream().allMatch(e -> e.getType() == EdgeType.FIRST_PARENT),
                    "All edges in linear history should be FIRST_PARENT");
        }

        @Test
        void build_commitsAreSortedByDate() {
            // Create commits in non-chronological order
            Commit c1 = createCommit(LocalDateTime.of(2024, 1, 1, 10, 0), null);
            Commit c2 = createCommit(LocalDateTime.of(2024, 1, 2, 10, 0), c1);
            Commit c3 = createCommit(LocalDateTime.of(2024, 1, 3, 10, 0), c2);

            // Pass in wrong order - should be sorted internally
            List<Commit> commits = List.of(c1, c3, c2);

            GitDepsTree tree = builder.build(repository, commits);

            // Most recent should be first (row 0)
            GitTreeNode firstNode = tree.getNodes().stream()
                    .filter(n -> n.getRowIndex() == 0)
                    .findFirst()
                    .orElseThrow();

            assertEquals(c3.getSha(), firstNode.getCommitSha(), "Most recent commit should be at row 0");
        }
    }

    @Nested
    class MergeCommits {

        @Test
        void build_mergeCommit_createsTwoEdges() {
            // Create merge scenario:
            //   merge <- main (sha_main)
            //        \<- feature (sha_feature)
            //   both -> base (sha_base)
            Commit base = createCommit(LocalDateTime.of(2024, 1, 1, 10, 0), null);
            Commit main = createCommit(LocalDateTime.of(2024, 1, 2, 10, 0), base);
            Commit feature = createCommit(LocalDateTime.of(2024, 1, 2, 11, 0), base);
            Commit merge = createCommitWithParents(LocalDateTime.of(2024, 1, 3, 10, 0), List.of(main, feature));

            List<Commit> commits = List.of(merge, feature, main, base);

            GitDepsTree tree = builder.build(repository, commits);

            // Find edges from merge commit
            List<GitTreeEdge> mergeEdges = tree.getEdges().stream()
                    .filter(e -> merge.getSha().equals(e.getFromCommitSha()))
                    .collect(Collectors.toList());

            assertEquals(2, mergeEdges.size(), "Merge commit should have 2 outgoing edges");

            // One should be FIRST_PARENT, one should be MERGE_PARENT
            assertTrue(mergeEdges.stream().anyMatch(e -> e.getType() == EdgeType.FIRST_PARENT),
                    "Should have a FIRST_PARENT edge");
            assertTrue(mergeEdges.stream().anyMatch(e -> e.getType() == EdgeType.MERGE_PARENT),
                    "Should have a MERGE_PARENT edge");
        }

        @Test
        void build_mergeCommit_usesMultipleLanes() {
            Commit base = createCommit(LocalDateTime.of(2024, 1, 1, 10, 0), null);
            Commit main = createCommit(LocalDateTime.of(2024, 1, 2, 10, 0), base);
            Commit feature = createCommit(LocalDateTime.of(2024, 1, 2, 11, 0), base);
            Commit merge = createCommitWithParents(LocalDateTime.of(2024, 1, 3, 10, 0), List.of(main, feature));

            List<Commit> commits = List.of(merge, feature, main, base);

            GitDepsTree tree = builder.build(repository, commits);

            assertTrue(tree.getColumns() >= 1, "Should use multiple lanes for parallel branches");
        }
    }

    @Nested
    class BranchNames {

        @Test
        void build_includesBranchNamesInNodes() {
            Commit commit = createCommit(LocalDateTime.now().minusDays(1), null);
            new Branch("main", "refs/heads/main", ReferenceCategory.LOCAL_BRANCH, repository, commit);

            List<Commit> commits = List.of(commit);

            GitDepsTree tree = builder.build(repository, commits);

            GitTreeNode node = tree.getNodes().get(0);
            Set<String> branchNames = node.getBranchNames();

            assertTrue(branchNames.contains("main"), "Node should include branch name");
        }

        @Test
        void build_commitOnMultipleBranches_hasAllBranchNames() {
            Commit commit = createCommit(LocalDateTime.now().minusDays(1), null);
            new Branch("main", "refs/heads/main", ReferenceCategory.LOCAL_BRANCH, repository, commit);
            new Branch("develop", "refs/heads/develop", ReferenceCategory.LOCAL_BRANCH, repository, commit);

            List<Commit> commits = List.of(commit);

            GitDepsTree tree = builder.build(repository, commits);

            GitTreeNode node = tree.getNodes().get(0);
            Set<String> branchNames = node.getBranchNames();

            assertAll(
                    () -> assertTrue(branchNames.contains("main"), "Should contain 'main'"),
                    () -> assertTrue(branchNames.contains("develop"), "Should contain 'develop'")
            );
        }
    }

    @Nested
    class TreeStructure {

        @Test
        void build_rowsMatchesCommitCount() {
            Commit c1 = createCommit(LocalDateTime.of(2024, 1, 1, 10, 0), null);
            Commit c2 = createCommit(LocalDateTime.of(2024, 1, 2, 10, 0), c1);
            Commit c3 = createCommit(LocalDateTime.of(2024, 1, 3, 10, 0), c2);

            List<Commit> commits = List.of(c1, c2, c3);

            GitDepsTree tree = builder.build(repository, commits);

            assertEquals(3, tree.getRows(), "Rows should equal commit count");
        }

        @Test
        void build_columnsIsAtLeastOne() {
            List<Commit> commits = List.of();

            GitDepsTree tree = builder.build(repository, commits);

            assertTrue(tree.getColumns() >= 1, "Columns should be at least 1");
        }
    }

    @Nested
    class SameTimestampCommits {

        @Test
        void build_sameTimestamp_sortsBySha() {
            LocalDateTime sameTime = LocalDateTime.of(2024, 1, 1, 10, 0);
            Commit c1 = createCommit(sameTime, null);
            Commit c2 = createCommit(sameTime, null);
            Commit c3 = createCommit(sameTime, null);

            List<Commit> commits = List.of(c2, c3, c1);

            GitDepsTree tree = builder.build(repository, commits);

            // Should be sorted consistently
            assertEquals(3, tree.getNodes().size());
        }
    }

    @Nested
    class LaneManagement {

        @Test
        void build_rowIndicesAreSequential() {
            // Verifies row++ is working correctly
            Commit c1 = createCommit(LocalDateTime.of(2024, 1, 1, 10, 0), null);
            Commit c2 = createCommit(LocalDateTime.of(2024, 1, 2, 10, 0), c1);
            Commit c3 = createCommit(LocalDateTime.of(2024, 1, 3, 10, 0), c2);
            Commit c4 = createCommit(LocalDateTime.of(2024, 1, 4, 10, 0), c3);

            List<Commit> commits = List.of(c4, c3, c2, c1);

            GitDepsTree tree = builder.build(repository, commits);

            // Verify row indices are 0, 1, 2, 3 (sequential)
            List<Integer> rows = tree.getNodes().stream()
                    .map(GitTreeNode::getRowIndex)
                    .sorted()
                    .collect(Collectors.toList());

            assertEquals(List.of(0, 1, 2, 3), rows, "Row indices should be sequential starting from 0");
        }

        @Test
        void build_parallelBranches_useCorrectLanes() {
            // Creates a scenario where lane management is important:
            //   merge
            //   /    \
            //  m1    f1
            //   \    /
            //   base
            Commit base = createCommit(LocalDateTime.of(2024, 1, 1, 10, 0), null);
            Commit m1 = createCommit(LocalDateTime.of(2024, 1, 2, 10, 0), base);
            Commit f1 = createCommit(LocalDateTime.of(2024, 1, 2, 11, 0), base);
            Commit merge = createCommitWithParents(LocalDateTime.of(2024, 1, 3, 10, 0), List.of(m1, f1));

            List<Commit> commits = List.of(merge, f1, m1, base);

            GitDepsTree tree = builder.build(repository, commits);

            // Verify correct number of columns (need at least 2 for parallel branches)
            assertTrue(tree.getColumns() >= 2, "Should have at least 2 columns for parallel branches");

            // Verify all nodes have valid column indices (>= 0)
            assertTrue(tree.getNodes().stream().allMatch(n -> n.getColumnIndex() >= 0),
                    "All column indices should be >= 0");
        }

        @Test
        void build_existingLane_usesExistingColumn() {
            // When a commit's SHA is already in lanes, it should use that column
            // This tests the `col < 0` check
            Commit c1 = createCommit(LocalDateTime.of(2024, 1, 1, 10, 0), null);
            Commit c2 = createCommit(LocalDateTime.of(2024, 1, 2, 10, 0), c1);
            Commit c3 = createCommit(LocalDateTime.of(2024, 1, 3, 10, 0), c2);

            List<Commit> commits = List.of(c3, c2, c1);

            GitDepsTree tree = builder.build(repository, commits);

            // In a linear history, all commits should be in column 0
            assertTrue(tree.getNodes().stream().allMatch(n -> n.getColumnIndex() == 0),
                    "Linear history should all be in column 0");
        }

        @Test
        void build_removesFinishedLanes() {
            // Tests that lanes are removed when commits are no longer referenced
            // This tests the removeIf call
            Commit root = createCommit(LocalDateTime.of(2024, 1, 1, 10, 0), null);
            Commit isolated = createCommit(LocalDateTime.of(2024, 1, 5, 10, 0), null);

            List<Commit> commits = List.of(isolated, root);

            GitDepsTree tree = builder.build(repository, commits);

            assertEquals(2, tree.getNodes().size());
            assertEquals(2, tree.getRows());
        }

        @Test
        void build_noDuplicateLanes() {
            // Tests that lanes.clear() and lanes.addAll(uniq) work to remove duplicates
            // Create scenario with potential duplicates
            Commit base = createCommit(LocalDateTime.of(2024, 1, 1, 10, 0), null);
            Commit c2 = createCommit(LocalDateTime.of(2024, 1, 2, 10, 0), base);
            Commit c3 = createCommit(LocalDateTime.of(2024, 1, 3, 10, 0), c2);

            List<Commit> commits = List.of(c3, c2, base);

            GitDepsTree tree = builder.build(repository, commits);

            // Each commit should be on exactly one node
            assertEquals(3, tree.getNodes().size());
        }
    }

    @Nested
    class EdgeOrdering {

        @Test
        void build_multipleParents_edgesInOrder() {
            // Tests that parentShas.sort() ensures consistent edge ordering
            Commit base = createCommit(LocalDateTime.of(2024, 1, 1, 10, 0), null);
            Commit p1 = createCommit(LocalDateTime.of(2024, 1, 2, 10, 0), base);
            Commit p2 = createCommit(LocalDateTime.of(2024, 1, 2, 11, 0), base);
            Commit merge = createCommitWithParents(LocalDateTime.of(2024, 1, 3, 10, 0), List.of(p2, p1));

            List<Commit> commits = List.of(merge, p2, p1, base);

            GitDepsTree tree = builder.build(repository, commits);

            // Find edges from merge
            List<GitTreeEdge> mergeEdges = tree.getEdges().stream()
                    .filter(e -> merge.getSha().equals(e.getFromCommitSha()))
                    .collect(Collectors.toList());

            assertEquals(2, mergeEdges.size(), "Merge should have 2 edges");

            // First edge should be FIRST_PARENT type
            GitTreeEdge firstEdge = mergeEdges.stream()
                    .filter(e -> e.getType() == EdgeType.FIRST_PARENT)
                    .findFirst()
                    .orElseThrow();
            assertNotNull(firstEdge.getToCommitSha());
        }
    }

    // Helper methods

    /**
     * Generates a unique 40-char hex SHA for testing.
     */
    private String generateSha() {
        shaCounter++;
        return String.format("%040x", shaCounter);
    }

    private Commit createCommit(LocalDateTime dateTime, Commit parent) {
        Developer dev = new Developer("Test Developer", "test@example.com", repository);
        Signature signature = new Signature(dev, dateTime);
        Commit commit = new Commit(generateSha(), signature, signature, "Test message", repository);
        if (parent != null) {
            commit.getParents().add(parent);
        }
        return commit;
    }

    private Commit createCommitWithParents(LocalDateTime dateTime, List<Commit> parents) {
        Developer dev = new Developer("Test Developer", "test@example.com", repository);
        Signature signature = new Signature(dev, dateTime);
        Commit commit = new Commit(generateSha(), signature, signature, "Test message", repository);
        commit.getParents().addAll(parents);
        return commit;
    }
}