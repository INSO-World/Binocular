package com.inso_world.binocular.jgit.tree;

import com.inso_world.binocular.model.Branch;
import com.inso_world.binocular.model.Commit;
import com.inso_world.binocular.model.Repository;
import com.inso_world.binocular.model.git.EdgeType;
import com.inso_world.binocular.model.git.GitDepsTree;
import com.inso_world.binocular.model.git.GitTreeEdge;
import com.inso_world.binocular.model.git.GitTreeNode;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Builds a simple lane based git dependency graph (nodes with row and column indices).
 *
 * Input: commits must contain parent relationships (Commit.parents).
 */
@Service
public class GitDepsTreeBuilder {

    public GitDepsTree build(Repository repository, List<Commit> commits, List<Branch> branches) {
        Objects.requireNonNull(repository, "repository");
        Objects.requireNonNull(commits, "commits");
        Objects.requireNonNull(branches, "branches");

        List<Commit> ordered = new ArrayList<>(commits);
        ordered.sort(Comparator
                .comparing(Commit::getCommitDateTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Commit::getSha));

        Set<String> remaining = new HashSet<>();
        for (Commit c : ordered) {
            remaining.add(c.getSha());
        }

        Map<String, Set<String>> branchNamesByCommit = indexBranchNames(branches, commits);

        List<String> lanes = new ArrayList<>();
        List<GitTreeNode> nodes = new ArrayList<>();
        List<GitTreeEdge> edges = new ArrayList<>();

        int maxColumns = 0;
        int row = 0;
        for (Commit commit : ordered) {
            String sha = commit.getSha();

            int col = lanes.indexOf(sha);
            if (col < 0) {
                lanes.add(0, sha);
                col = 0;
            }

            Set<String> branchNames = branchNamesByCommit.getOrDefault(sha, Set.of());
            nodes.add(new GitTreeNode(sha, row, col, branchNames));

            List<String> parentShas = new ArrayList<>(commit.getParentShas());
            parentShas.sort(String::compareTo);

            // create edges
            for (int i = 0; i < parentShas.size(); i++) {
                String pSha = parentShas.get(i);
                EdgeType type = (i == 0) ? EdgeType.FIRST_PARENT : EdgeType.MERGE_PARENT;
                edges.add(new GitTreeEdge(sha, pSha, type));
            }

            // update lanes for next row
            remaining.remove(sha);

            if (parentShas.isEmpty()) {
                lanes.remove(col);
            } else {
                String firstParentSha = parentShas.get(0);
                lanes.set(col, firstParentSha);

                for (int i = 1; i < parentShas.size(); i++) {
                    String pSha = parentShas.get(i);
                    if (!lanes.contains(pSha)) {
                        lanes.add(pSha);
                    }
                }
            }

            // drop finished lanes
            lanes.removeIf(l -> !remaining.contains(l));

            // dedupe while keeping order
            LinkedHashSet<String> uniq = new LinkedHashSet<>(lanes);
            lanes.clear();
            lanes.addAll(uniq);

            maxColumns = Math.max(maxColumns, lanes.size());
            row++;
        }

        return new GitDepsTree(nodes, edges, Math.max(maxColumns, 1), nodes.size());
    }

    private static Map<String, Set<String>> indexBranchNames(List<Branch> branches, List<Commit> allCommits) {
        Map<String, Set<String>> out = new HashMap<>();
        Map<String, Commit> commitsBySha = new HashMap<>();
        for (Commit c : allCommits) {
            commitsBySha.put(c.getSha(), c);
        }

        for (Branch b : branches) {
            String bName = b.getName();
            // TODO
            out.computeIfAbsent(b.getHeadSha(), k -> new LinkedHashSet<>()).add(bName);
        }
        return out;
    }
}
