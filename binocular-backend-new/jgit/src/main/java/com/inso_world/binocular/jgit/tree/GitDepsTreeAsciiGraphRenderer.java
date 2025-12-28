package com.inso_world.binocular.jgit.tree;

import com.inso_world.binocular.model.Commit;
import com.inso_world.binocular.model.User;
import com.inso_world.binocular.model.git.GitDepsTree;
import com.inso_world.binocular.model.git.GitTreeEdge;
import com.inso_world.binocular.model.git.GitTreeNode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Renders a GitDepsTree as an ASCII graph similar in spirit to `git log --graph`.
 * Uses only ASCII characters to avoid encoding issues on Windows terminals.
 */
public class GitDepsTreeAsciiGraphRenderer {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT);

    public String render(GitDepsTree tree, List<Commit> commits) {
        Map<String, Commit> bySha = new HashMap<>();
        if (commits != null) {
            for (Commit c : commits) {
                if (c != null && c.getSha() != null) {
                    bySha.put(c.getSha(), c);
                }
            }
        }
        return render(tree, bySha);
    }

    public String render(GitDepsTree tree, Map<String, Commit> commitsBySha) {
        if (tree == null || tree.getNodes() == null || tree.getNodes().isEmpty()) {
            return "empty tree\n";
        }

        List<GitTreeNode> nodes = new ArrayList<>(tree.getNodes());
        nodes.sort(Comparator.comparingInt(GitTreeNode::getRowIndex)
                .thenComparingInt(GitTreeNode::getColumnIndex));

        int rows = tree.getRows();
        if (rows <= 0) {
            rows = nodes.stream().mapToInt(GitTreeNode::getRowIndex).max().orElse(0) + 1;
        }
        int cols = tree.getColumns();
        if (cols <= 0) {
            cols = nodes.stream().mapToInt(GitTreeNode::getColumnIndex).max().orElse(0) + 1;
        }
        cols = Math.max(cols, 1);

        Map<String, Integer> rowBySha = new HashMap<>();
        Map<String, Integer> colBySha = new HashMap<>();
        Map<Integer, List<GitTreeNode>> nodesByRow = new HashMap<>();
        for (GitTreeNode n : nodes) {
            rowBySha.put(n.getCommitSha(), n.getRowIndex());
            colBySha.put(n.getCommitSha(), n.getColumnIndex());
            nodesByRow.computeIfAbsent(n.getRowIndex(), k -> new ArrayList<>()).add(n);
        }

        boolean[][] active = new boolean[rows][cols];
        for (GitTreeNode n : nodes) {
            int r = n.getRowIndex();
            int c = n.getColumnIndex();
            if (r >= 0 && r < rows && c >= 0 && c < cols) {
                active[r][c] = true;
            }
        }

        List<GitTreeEdge> edges = tree.getEdges() == null ? List.of() : tree.getEdges();
        for (GitTreeEdge e : edges) {
            Integer childRow = rowBySha.get(e.getFromCommitSha());
            Integer parentRow = rowBySha.get(e.getToCommitSha());
            Integer parentCol = colBySha.get(e.getToCommitSha());
            if (childRow == null || parentRow == null || parentCol == null) continue;
            if (parentRow <= childRow) continue;

            for (int r = childRow + 1; r < parentRow; r++) {
                if (r >= 0 && r < rows && parentCol >= 0 && parentCol < cols) {
                    active[r][parentCol] = true;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("rows=").append(rows).append(" cols=").append(cols).append("\n");
        sb.append("Legend: '*' Commit, '|' Lane, '/' and '\\\\' Parent connections\n\n");

        int width = Math.max(1, cols * 2 - 1);

        for (int r = 0; r < rows; r++) {
            List<GitTreeNode> rowNodes = nodesByRow.getOrDefault(r, List.of());
            rowNodes.sort(Comparator.comparingInt(GitTreeNode::getColumnIndex));

            char[] commitLine = blank(width);
            for (int c = 0; c < cols; c++) {
                if (active[r][c]) {
                    commitLine[c * 2] = '|';
                }
            }
            for (GitTreeNode n : rowNodes) {
                int pos = clamp(n.getColumnIndex() * 2, 0, width - 1);
                commitLine[pos] = '*';
            }

            GitTreeNode mainNode = rowNodes.isEmpty() ? null : rowNodes.get(0);
            String info = mainNode == null ? "" : formatInfo(mainNode, commitsBySha);

            sb.append(String.format(Locale.ROOT, "%4d ", r))
                    .append(new String(commitLine))
                    .append("  ")
                    .append(info)
                    .append("\n");

            if (r == rows - 1) {
                continue;
            }

            boolean hasOutgoing = false;
            char[] connLine = blank(width);

            for (int c = 0; c < cols; c++) {
                if (active[r][c]) {
                    connLine[c * 2] = '|';
                }
            }

            for (GitTreeNode n : rowNodes) {
                String from = n.getCommitSha();
                int childCol = n.getColumnIndex();
                int childPos = clamp(childCol * 2, 0, width - 1);

                for (GitTreeEdge e : edges) {
                    if (!from.equals(e.getFromCommitSha())) continue;

                    Integer parentRow = rowBySha.get(e.getToCommitSha());
                    Integer parentCol = colBySha.get(e.getToCommitSha());
                    if (parentRow == null || parentCol == null) continue;
                    if (parentRow <= r) continue;

                    hasOutgoing = true;
                    int parentPos = clamp(parentCol * 2, 0, width - 1);

                    if (parentCol == childCol) {
                        connLine[childPos] = '|';
                        connLine[parentPos] = '|';
                    } else if (parentCol > childCol) {
                        if (childPos + 1 < width) connLine[childPos + 1] = '\\';
                        connLine[parentPos] = '|';
                    } else {
                        if (childPos - 1 >= 0) connLine[childPos - 1] = '/';
                        connLine[parentPos] = '|';
                    }
                }
            }

            if (hasOutgoing) {
                sb.append("     ").append(new String(connLine)).append("\n");
            }
        }

        return sb.toString();
    }

    private static String formatInfo(GitTreeNode node, Map<String, Commit> commitsBySha) {
        String sha = node.getCommitSha();
        Commit c = commitsBySha == null ? null : commitsBySha.get(sha);

        String shaS = shortSha(sha);
        String dec = decorations(node.getBranchNames());
        String author = authorPart(c);
        String date = datePart(c);
        String msg = messagePart(c);

        String out = shaS;
        if (!dec.isEmpty()) out += " " + dec;
        if (!author.isEmpty()) out += "  " + author;
        if (!date.isEmpty()) out += "  " + date;
        if (!msg.isEmpty()) out += "  " + msg;
        return out;
    }

    private static String decorations(Set<String> branchNames) {
        if (branchNames == null || branchNames.isEmpty()) return "";
        String joined = branchNames.stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.joining(","));
        return "[" + joined + "]";
    }

    private static String authorPart(Commit c) {
        if (c == null) return "";
        User a = c.getAuthor();
        if (a != null && !isBlank(a.getName())) {
            return a.getName();
        }
        User comm = c.getCommitter();
        if (comm != null && !isBlank(comm.getName())) {
            return comm.getName();
        }
        return "";
    }

    private static String datePart(Commit c) {
        if (c == null) return "";
        LocalDateTime dt = c.getCommitDateTime();
        if (dt == null) dt = c.getAuthorDateTime();
        if (dt == null) return "";
        return DT.format(dt);
    }

    private static String messagePart(Commit c) {
        if (c == null) return "";
        String msg = c.getMessage();
        if (isBlank(msg)) return "";
        String first = msg.split("\\\\R", 2)[0];
        return truncate(first.trim(), 90);
    }

    private static String shortSha(String sha) {
        if (sha == null) return "null";
        return sha.length() > 7 ? sha.substring(0, 7) : sha;
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, Math.max(0, max - 3)) + "...";
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static char[] blank(int width) {
        char[] arr = new char[width];
        Arrays.fill(arr, ' ');
        return arr;
    }

    private static int clamp(int v, int min, int max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }
}
