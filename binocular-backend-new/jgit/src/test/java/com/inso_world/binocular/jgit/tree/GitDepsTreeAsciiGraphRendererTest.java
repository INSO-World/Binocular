package com.inso_world.binocular.jgit.tree;

import com.inso_world.binocular.core.unit.base.BaseUnitTest;
import com.inso_world.binocular.model.Commit;
import com.inso_world.binocular.model.Developer;
import com.inso_world.binocular.model.Project;
import com.inso_world.binocular.model.Repository;
import com.inso_world.binocular.model.Signature;
import com.inso_world.binocular.model.git.EdgeType;
import com.inso_world.binocular.model.git.GitDepsTree;
import com.inso_world.binocular.model.git.GitTreeEdge;
import com.inso_world.binocular.model.git.GitTreeNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GitDepsTreeAsciiGraphRenderer}.
 * Tests ASCII rendering of git dependency trees.
 */
class GitDepsTreeAsciiGraphRendererTest extends BaseUnitTest {

    private GitDepsTreeAsciiGraphRenderer renderer;
    private Repository repository;
    private Project project;
    private int shaCounter = 0;

    @BeforeEach
    void setUp() {
        renderer = new GitDepsTreeAsciiGraphRenderer();
        project = new Project("test-project");
        repository = new Repository("/test/path", project);
        shaCounter = 0;
    }

    /**
     * Generates a unique 40-char hex SHA for testing.
     */
    private String generateSha() {
        shaCounter++;
        return String.format("%040x", shaCounter);
    }

    @Nested
    class NullAndEmptyInput {

        @Test
        void render_nullTree_returnsEmptyTreeMessage() {
            String result = renderer.render(null, (Map<String, Commit>) null);

            assertEquals("empty tree\n", result, "Null tree should render as 'empty tree'");
        }

        @Test
        void render_treeWithEmptyNodes_returnsEmptyTreeMessage() {
            GitDepsTree tree = new GitDepsTree(List.of(), List.of(), 1, 1);

            String result = renderer.render(tree, (Map<String, Commit>) null);

            assertEquals("empty tree\n", result);
        }

        @Test
        void render_withNullCommitsList_handlesGracefully() {
            String sha = generateSha();
            GitTreeNode node = new GitTreeNode(sha, 0, 0, Set.of());
            GitDepsTree tree = new GitDepsTree(List.of(node), List.of(), 1, 1);

            String result = renderer.render(tree, (List<Commit>) null);

            assertNotNull(result);
            assertTrue(result.contains(sha.substring(0, 7)), "Should contain short SHA");
        }

        @Test
        void render_withEmptyCommitsList_handlesGracefully() {
            String sha = generateSha();
            GitTreeNode node = new GitTreeNode(sha, 0, 0, Set.of());
            GitDepsTree tree = new GitDepsTree(List.of(node), List.of(), 1, 1);

            String result = renderer.render(tree, List.of());

            assertNotNull(result);
            assertTrue(result.contains(sha.substring(0, 7)), "Should contain short SHA");
        }
    }

    @Nested
    class SingleNode {

        @Test
        void render_singleNode_showsCommitMarker() {
            String sha = generateSha();
            GitTreeNode node = new GitTreeNode(sha, 0, 0, Set.of());
            GitDepsTree tree = new GitDepsTree(List.of(node), List.of(), 1, 1);

            String result = renderer.render(tree, (Map<String, Commit>) null);

            assertTrue(result.contains("*"), "Should contain commit marker '*'");
            assertTrue(result.contains(sha.substring(0, 7)), "Should contain shortened SHA");
        }

        @Test
        void render_singleNode_showsRowNumber() {
            String sha = generateSha();
            GitTreeNode node = new GitTreeNode(sha, 0, 0, Set.of());
            GitDepsTree tree = new GitDepsTree(List.of(node), List.of(), 1, 1);

            String result = renderer.render(tree, (Map<String, Commit>) null);

            assertTrue(result.contains("   0 "), "Should contain row number 0");
        }
    }

    @Nested
    class BranchNames {

        @Test
        void render_nodeWithBranchName_showsBranchInBrackets() {
            String sha = generateSha();
            GitTreeNode node = new GitTreeNode(sha, 0, 0, Set.of("main"));
            GitDepsTree tree = new GitDepsTree(List.of(node), List.of(), 1, 1);

            String result = renderer.render(tree, (Map<String, Commit>) null);

            assertTrue(result.contains("[main]"), "Should show branch name in brackets");
        }

        @Test
        void render_nodeWithMultipleBranches_showsAllSorted() {
            String sha = generateSha();
            GitTreeNode node = new GitTreeNode(sha, 0, 0, Set.of("develop", "main", "feature"));
            GitDepsTree tree = new GitDepsTree(List.of(node), List.of(), 1, 1);

            String result = renderer.render(tree, (Map<String, Commit>) null);

            // Should be sorted case-insensitively
            assertTrue(result.contains("[develop,feature,main]"),
                    "Should show sorted branch names: " + result);
        }

        @Test
        void render_nodeWithEmptyBranchNames_noBrackets() {
            String sha = generateSha();
            GitTreeNode node = new GitTreeNode(sha, 0, 0, Set.of());
            GitDepsTree tree = new GitDepsTree(List.of(node), List.of(), 1, 1);

            String result = renderer.render(tree, (Map<String, Commit>) null);

            assertFalse(result.contains("[]"), "Should not show empty brackets");
        }
    }

    @Nested
    class CommitInfo {

        @Test
        void render_withCommitInfo_showsAuthorName() {
            String sha = generateSha();
            GitTreeNode node = new GitTreeNode(sha, 0, 0, Set.of());
            GitDepsTree tree = new GitDepsTree(List.of(node), List.of(), 1, 1);

            Commit commit = createCommit(sha, "Test Author", "test@example.com",
                    LocalDateTime.of(2024, 1, 15, 14, 30), "Test commit message");
            Map<String, Commit> commits = Map.of(sha, commit);

            String result = renderer.render(tree, commits);

            assertTrue(result.contains("Test Author"), "Should show author name");
        }

        @Test
        void render_withCommitInfo_showsDateTime() {
            String sha = generateSha();
            GitTreeNode node = new GitTreeNode(sha, 0, 0, Set.of());
            GitDepsTree tree = new GitDepsTree(List.of(node), List.of(), 1, 1);

            Commit commit = createCommit(sha, "Test Author", "test@example.com",
                    LocalDateTime.of(2024, 1, 15, 14, 30), "Test commit message");
            Map<String, Commit> commits = Map.of(sha, commit);

            String result = renderer.render(tree, commits);

            assertTrue(result.contains("2024-01-15 14:30"), "Should show formatted date time");
        }

        @Test
        void render_withCommitInfo_showsMessage() {
            String sha = generateSha();
            GitTreeNode node = new GitTreeNode(sha, 0, 0, Set.of());
            GitDepsTree tree = new GitDepsTree(List.of(node), List.of(), 1, 1);

            Commit commit = createCommit(sha, "Test Author", "test@example.com",
                    LocalDateTime.of(2024, 1, 15, 14, 30), "Test commit message");
            Map<String, Commit> commits = Map.of(sha, commit);

            String result = renderer.render(tree, commits);

            assertTrue(result.contains("Test commit message"), "Should show commit message");
        }

        @Test
        void render_longMessage_isTruncated() {
            String sha = generateSha();
            GitTreeNode node = new GitTreeNode(sha, 0, 0, Set.of());
            GitDepsTree tree = new GitDepsTree(List.of(node), List.of(), 1, 1);

            String longMessage = "A".repeat(200);
            Commit commit = createCommit(sha, "Test Author", "test@example.com",
                    LocalDateTime.of(2024, 1, 15, 14, 30), longMessage);
            Map<String, Commit> commits = Map.of(sha, commit);

            String result = renderer.render(tree, commits);

            assertTrue(result.contains("..."), "Long message should be truncated with ...");
        }
    }

    @Nested
    class EdgeRendering {

        @Test
        void render_verticalEdge_showsPipe() {
            String sha1 = generateSha();
            String sha2 = generateSha();
            GitTreeNode node1 = new GitTreeNode(sha1, 0, 0, Set.of());
            GitTreeNode node2 = new GitTreeNode(sha2, 1, 0, Set.of());
            GitTreeEdge edge = new GitTreeEdge(sha1, sha2, EdgeType.FIRST_PARENT);
            GitDepsTree tree = new GitDepsTree(List.of(node1, node2), List.of(edge), 1, 2);

            String result = renderer.render(tree, (Map<String, Commit>) null);

            assertTrue(result.contains("|"), "Should show pipe for vertical edge");
        }

        @Test
        void render_diagonalEdgeRight_showsBackslash() {
            String sha1 = generateSha();
            String sha2 = generateSha();
            GitTreeNode node1 = new GitTreeNode(sha1, 0, 0, Set.of());
            GitTreeNode node2 = new GitTreeNode(sha2, 1, 1, Set.of());
            GitTreeEdge edge = new GitTreeEdge(sha1, sha2, EdgeType.MERGE_PARENT);
            GitDepsTree tree = new GitDepsTree(List.of(node1, node2), List.of(edge), 2, 2);

            String result = renderer.render(tree, (Map<String, Commit>) null);

            assertTrue(result.contains("\\"), "Should show backslash for diagonal right edge");
        }

        @Test
        void render_diagonalEdgeLeft_showsSlash() {
            String sha1 = generateSha();
            String sha2 = generateSha();
            GitTreeNode node1 = new GitTreeNode(sha1, 0, 1, Set.of());
            GitTreeNode node2 = new GitTreeNode(sha2, 1, 0, Set.of());
            GitTreeEdge edge = new GitTreeEdge(sha1, sha2, EdgeType.FIRST_PARENT);
            GitDepsTree tree = new GitDepsTree(List.of(node1, node2), List.of(edge), 2, 2);

            String result = renderer.render(tree, (Map<String, Commit>) null);

            assertTrue(result.contains("/"), "Should show slash for diagonal left edge");
        }
    }

    @Nested
    class MultipleRows {

        @Test
        void render_multipleRows_showsAllRows() {
            List<GitTreeNode> nodes = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                nodes.add(new GitTreeNode(generateSha(), i, 0, Set.of()));
            }
            GitDepsTree tree = new GitDepsTree(nodes, List.of(), 1, 5);

            String result = renderer.render(tree, (Map<String, Commit>) null);

            for (int i = 0; i < 5; i++) {
                assertTrue(result.contains(String.format("%4d ", i)), "Should contain row " + i);
            }
        }
    }

    @Nested
    class ColumnsAndLanes {

        @Test
        void render_multipleColumns_showsCorrectWidth() {
            String sha1 = generateSha();
            String sha2 = generateSha();
            GitTreeNode node1 = new GitTreeNode(sha1, 0, 0, Set.of());
            GitTreeNode node2 = new GitTreeNode(sha2, 0, 2, Set.of());
            GitDepsTree tree = new GitDepsTree(List.of(node1, node2), List.of(), 3, 1);

            String result = renderer.render(tree, (Map<String, Commit>) null);

            // Both nodes should be marked with *
            long starCount = result.chars().filter(c -> c == '*').count();
            assertTrue(starCount >= 2, "Should show at least 2 commit markers");
        }

        @Test
        void render_activeLanes_showPipes() {
            String sha1 = generateSha();
            String sha2 = generateSha();
            GitTreeNode node1 = new GitTreeNode(sha1, 0, 0, Set.of());
            GitTreeNode node2 = new GitTreeNode(sha2, 2, 0, Set.of());
            GitTreeEdge edge = new GitTreeEdge(sha1, sha2, EdgeType.FIRST_PARENT);
            GitDepsTree tree = new GitDepsTree(List.of(node1, node2), List.of(edge), 1, 3);

            String result = renderer.render(tree, (Map<String, Commit>) null);

            // The lane should show pipes for row 1 (between nodes)
            assertTrue(result.contains("|"), "Should show pipe for active lane");
        }
    }

    @Nested
    class TreeDimensions {

        @Test
        void render_zeroRows_calculatesFromNodes() {
            String sha = generateSha();
            GitTreeNode node = new GitTreeNode(sha, 5, 0, Set.of());
            GitDepsTree tree = new GitDepsTree(List.of(node), List.of(), 1, 0);

            String result = renderer.render(tree, (Map<String, Commit>) null);

            assertTrue(result.contains("rows=6"), "Should calculate rows from node indices");
        }

        @Test
        void render_zeroColumns_calculatesFromNodes() {
            String sha = generateSha();
            GitTreeNode node = new GitTreeNode(sha, 0, 3, Set.of());
            GitDepsTree tree = new GitDepsTree(List.of(node), List.of(), 0, 1);

            String result = renderer.render(tree, (Map<String, Commit>) null);

            assertTrue(result.contains("cols=4"), "Should calculate columns from node indices");
        }
    }

    @Nested
    class Header {

        @Test
        void render_includesRowsAndColsHeader() {
            String sha = generateSha();
            GitTreeNode node = new GitTreeNode(sha, 0, 0, Set.of());
            GitDepsTree tree = new GitDepsTree(List.of(node), List.of(), 3, 5);

            String result = renderer.render(tree, (Map<String, Commit>) null);

            assertTrue(result.contains("rows=5"), "Should show rows count");
            assertTrue(result.contains("cols=3"), "Should show columns count");
        }

        @Test
        void render_includesLegend() {
            String sha = generateSha();
            GitTreeNode node = new GitTreeNode(sha, 0, 0, Set.of());
            GitDepsTree tree = new GitDepsTree(List.of(node), List.of(), 1, 1);

            String result = renderer.render(tree, (Map<String, Commit>) null);

            assertTrue(result.contains("Legend"), "Should include legend");
        }
    }

    @Nested
    class ShortSha {

        @Test
        void render_shortSha_truncatesToSevenChars() {
            String sha = generateSha();
            GitTreeNode node = new GitTreeNode(sha, 0, 0, Set.of());
            GitDepsTree tree = new GitDepsTree(List.of(node), List.of(), 1, 1);

            String result = renderer.render(tree, (Map<String, Commit>) null);

            assertTrue(result.contains(sha.substring(0, 7)), "Should show first 7 chars of SHA");
            assertFalse(result.contains(sha), "Should not show full SHA in output line (except maybe header)");
        }

        @Test
        void render_shaLessThanSeven_showsFull() {
            String sha = "abc";
            GitTreeNode node = new GitTreeNode(sha, 0, 0, Set.of());
            GitDepsTree tree = new GitDepsTree(List.of(node), List.of(), 1, 1);

            String result = renderer.render(tree, (Map<String, Commit>) null);

            assertTrue(result.contains("abc"), "Should show short SHA as-is");
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void render_edgeWithMissingSha_isIgnored() {
            String sha = generateSha();
            GitTreeNode node = new GitTreeNode(sha, 0, 0, Set.of());
            GitTreeEdge edge = new GitTreeEdge(sha, "nonexistent12345678901234567890123456", EdgeType.FIRST_PARENT);
            GitDepsTree tree = new GitDepsTree(List.of(node), List.of(edge), 1, 1);

            String result = renderer.render(tree, (Map<String, Commit>) null);

            assertNotNull(result, "Should handle edge referencing non-existent node");
        }
    }

    @Nested
    class RenderWithCommitList {

        @Test
        void render_withCommitList_mapsCorrectly() {
            String sha = generateSha();
            GitTreeNode node = new GitTreeNode(sha, 0, 0, Set.of());
            GitDepsTree tree = new GitDepsTree(List.of(node), List.of(), 1, 1);

            Commit commit = createCommit(sha, "Author", "email@test.com",
                    LocalDateTime.of(2024, 1, 15, 10, 0), "Message");
            List<Commit> commits = List.of(commit);

            String result = renderer.render(tree, commits);

            assertNotNull(result);
            assertTrue(result.contains("Author"), "Should show author from commit list");
        }
    }

    @Nested
    class DetailedRendering {

        @Test
        void render_multiLaneGraph_correctStructure() {
            // Create a complex graph with multiple lanes
            String sha1 = generateSha();
            String sha2 = generateSha();
            String sha3 = generateSha();
            String sha4 = generateSha();

            // Row 0: sha4 (col 0)
            // Row 1: sha3 (col 0), sha2 (col 1)
            // Row 2: sha1 (col 0)
            GitTreeNode n4 = new GitTreeNode(sha4, 0, 0, Set.of());
            GitTreeNode n3 = new GitTreeNode(sha3, 1, 0, Set.of());
            GitTreeNode n2 = new GitTreeNode(sha2, 1, 1, Set.of());
            GitTreeNode n1 = new GitTreeNode(sha1, 2, 0, Set.of());

            GitTreeEdge e1 = new GitTreeEdge(sha4, sha3, EdgeType.FIRST_PARENT);
            GitTreeEdge e2 = new GitTreeEdge(sha4, sha2, EdgeType.MERGE_PARENT);
            GitTreeEdge e3 = new GitTreeEdge(sha3, sha1, EdgeType.FIRST_PARENT);
            GitTreeEdge e4 = new GitTreeEdge(sha2, sha1, EdgeType.FIRST_PARENT);

            GitDepsTree tree = new GitDepsTree(List.of(n4, n3, n2, n1), List.of(e1, e2, e3, e4), 2, 3);

            String result = renderer.render(tree, (Map<String, Commit>) null);

            // Verify structure
            assertAll(
                    () -> assertTrue(result.contains("rows=3"), "Should have 3 rows"),
                    () -> assertTrue(result.contains("cols=2"), "Should have 2 cols"),
                    () -> assertTrue(result.chars().filter(c -> c == '*').count() >= 4, "Should have at least 4 commit markers")
            );
        }

        @Test
        void render_verifyLaneConnections() {
            String sha1 = generateSha();
            String sha2 = generateSha();
            String sha3 = generateSha();

            // Linear chain with longer gap
            GitTreeNode n3 = new GitTreeNode(sha3, 0, 0, Set.of());
            GitTreeNode n2 = new GitTreeNode(sha2, 2, 0, Set.of()); // Skip row 1
            GitTreeNode n1 = new GitTreeNode(sha1, 4, 0, Set.of()); // Skip row 3

            GitTreeEdge e1 = new GitTreeEdge(sha3, sha2, EdgeType.FIRST_PARENT);
            GitTreeEdge e2 = new GitTreeEdge(sha2, sha1, EdgeType.FIRST_PARENT);

            GitDepsTree tree = new GitDepsTree(List.of(n3, n2, n1), List.of(e1, e2), 1, 5);

            String result = renderer.render(tree, (Map<String, Commit>) null);

            // Verify pipe characters for lanes in gap rows
            String[] lines = result.split("\n");
            long pipeLines = Arrays.stream(lines)
                    .filter(l -> l.contains("|") && !l.contains("*"))
                    .count();
            assertTrue(pipeLines >= 2, "Should have pipe lines for gaps between commits");
        }

        @Test
        void render_nodesAtDifferentColumns_correctPosition() {
            String sha1 = generateSha();
            String sha2 = generateSha();

            // Two nodes on same row but different columns
            GitTreeNode n1 = new GitTreeNode(sha1, 0, 0, Set.of());
            GitTreeNode n2 = new GitTreeNode(sha2, 0, 2, Set.of());

            GitDepsTree tree = new GitDepsTree(List.of(n1, n2), List.of(), 3, 1);

            String result = renderer.render(tree, (Map<String, Commit>) null);

            // Should have exactly one row with two stars
            String[] lines = result.split("\n");
            long rowsWithTwoStars = Arrays.stream(lines)
                    .filter(l -> {
                        long stars = l.chars().filter(c -> c == '*').count();
                        return stars == 2;
                    })
                    .count();
            assertEquals(1, rowsWithTwoStars, "Should have one row with 2 commit markers");
        }

        @Test
        void render_diagonalEdges_correctCharacters() {
            // Test diagonal edge rendering (\ and /)
            String sha1 = generateSha();
            String sha2 = generateSha();
            String sha3 = generateSha();

            // sha1 at (0,0), sha2 at (1,1), sha3 at (2,0) - creates \ then /
            GitTreeNode n1 = new GitTreeNode(sha1, 0, 0, Set.of());
            GitTreeNode n2 = new GitTreeNode(sha2, 1, 1, Set.of());
            GitTreeNode n3 = new GitTreeNode(sha3, 2, 0, Set.of());

            GitTreeEdge e1 = new GitTreeEdge(sha1, sha2, EdgeType.MERGE_PARENT);
            GitTreeEdge e2 = new GitTreeEdge(sha2, sha3, EdgeType.FIRST_PARENT);

            GitDepsTree tree = new GitDepsTree(List.of(n1, n2, n3), List.of(e1, e2), 2, 3);

            String result = renderer.render(tree, (Map<String, Commit>) null);

            // Should have both diagonal characters
            assertTrue(result.contains("\\"), "Should have backslash for rightward diagonal");
            assertTrue(result.contains("/"), "Should have slash for leftward diagonal");
        }

        @Test
        void render_messageTruncation_atCorrectLength() {
            String sha = generateSha();
            GitTreeNode node = new GitTreeNode(sha, 0, 0, Set.of());
            GitDepsTree tree = new GitDepsTree(List.of(node), List.of(), 1, 1);

            // Message over 90 char limit
            String message100 = "M".repeat(100);
            Commit commit = createCommit(sha, "Author", "email@test.com",
                    LocalDateTime.of(2024, 1, 15, 10, 0), message100);
            Map<String, Commit> commits = Map.of(sha, commit);

            String result = renderer.render(tree, commits);

            // Messages over 90 chars get truncated
            assertFalse(result.contains(message100), "Long message should be truncated");
            assertTrue(result.contains("..."), "Truncated message should end with ...");
        }

        @Test
        void render_verifyRowNumberFormatting() {
            // Test with larger row numbers for formatting verification
            List<GitTreeNode> nodes = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                nodes.add(new GitTreeNode(generateSha(), i, 0, Set.of()));
            }
            GitDepsTree tree = new GitDepsTree(nodes, List.of(), 1, 12);

            String result = renderer.render(tree, (Map<String, Commit>) null);

            // Verify row numbers are right-aligned
            assertTrue(result.contains("  10 "), "Row 10 should be formatted with leading spaces");
            assertTrue(result.contains("  11 "), "Row 11 should be formatted with leading spaces");
        }

        @Test
        void render_maxColumnCalculation() {
            String sha1 = generateSha();
            String sha2 = generateSha();
            String sha3 = generateSha();

            // Three columns
            GitTreeNode n1 = new GitTreeNode(sha1, 0, 0, Set.of());
            GitTreeNode n2 = new GitTreeNode(sha2, 0, 1, Set.of());
            GitTreeNode n3 = new GitTreeNode(sha3, 0, 2, Set.of());

            GitDepsTree tree = new GitDepsTree(List.of(n1, n2, n3), List.of(), 3, 1);

            String result = renderer.render(tree, (Map<String, Commit>) null);

            assertTrue(result.contains("cols=3"), "Should report 3 columns");
        }

        @Test
        void render_emptyMessageNullSafe() {
            String sha = generateSha();
            GitTreeNode node = new GitTreeNode(sha, 0, 0, Set.of());
            GitDepsTree tree = new GitDepsTree(List.of(node), List.of(), 1, 1);

            // Commit with empty string message
            Commit commit = createCommit(sha, "Author", "email@test.com",
                    LocalDateTime.of(2024, 1, 15, 10, 0), "");
            Map<String, Commit> commits = Map.of(sha, commit);

            String result = renderer.render(tree, commits);

            assertNotNull(result);
            assertTrue(result.contains("Author"));
        }

        @Test
        void render_activeEdgesRendered() {
            String sha1 = generateSha();
            String sha2 = generateSha();

            // Simple vertical edge from row 0 to row 3
            GitTreeNode n1 = new GitTreeNode(sha1, 0, 0, Set.of());
            GitTreeNode n2 = new GitTreeNode(sha2, 3, 0, Set.of());
            GitTreeEdge edge = new GitTreeEdge(sha1, sha2, EdgeType.FIRST_PARENT);

            GitDepsTree tree = new GitDepsTree(List.of(n1, n2), List.of(edge), 1, 4);

            String result = renderer.render(tree, (Map<String, Commit>) null);

            // Count pipe characters (should be in rows 1 and 2 connecting the nodes)
            String[] lines = result.split("\n");
            long linesWithJustPipe = Arrays.stream(lines)
                    .filter(l -> l.trim().startsWith("1 ") || l.trim().startsWith("2 "))
                    .filter(l -> l.contains("|"))
                    .count();
            assertTrue(linesWithJustPipe >= 1, "Should have pipes connecting nodes");
        }
    }

    // Helper methods to create test commits

    private Commit createCommit(String sha, String authorName, String email, LocalDateTime dateTime, String message) {
        Developer author = new Developer(authorName, email, repository);
        Signature authorSig = new Signature(author, dateTime);
        return new Commit(sha, authorSig, authorSig, message, repository);
    }
}
