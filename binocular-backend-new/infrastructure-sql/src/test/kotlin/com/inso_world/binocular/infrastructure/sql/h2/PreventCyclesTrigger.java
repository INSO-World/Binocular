package com.inso_world.binocular.infrastructure.sql.h2;

import org.h2.api.Trigger;
import java.sql.*;

public class PreventCyclesTrigger implements Trigger {
    private Long parentId;
    private Long childId;

    public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type) {
        // initialization code
    }

    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
        this.childId = (Long) newRow[1];  // child_id is the second column
        this.parentId = (Long) newRow[0]; // parent_id is the first column

        String childSha = getCommitSha(conn, this.childId);
        String parentSha = getCommitSha(conn, this.parentId);

        // Check direct self-loop
        if (this.childId.equals(this.parentId)) {
            throw new SQLException("Cyclic dependency detected: Commit " + childSha + " cannot be its own parent");
        }

        // Check for cycles by verifying if the child can reach the parent
//        checkCycles(conn, this.childId, this.parentId, childSha, parentSha);
    }

    private String getCommitSha(Connection conn, Long commitId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT sha FROM commits WHERE id = ?")) {
            stmt.setLong(1, commitId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                } else {
                    throw new SQLException("Commit with id " + commitId + " not found");
                }
            }
        }
    }

    private void checkCycles(Connection conn, Long startId, Long targetId, String childSha, String parentSha) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                // Find all reachable commits from the start point
                "WITH RECURSIVE reachable_commits(source_id, target_id, depth, path, sha_path) AS (" +
                        // Base case: direct relationships (path from child_id to parent_id)
                        "SELECT cp.parent_id as source_id, cp.child_id as target_id, 1 as depth, " +
                        "ARRAY[cp.parent_id, cp.child_id] as path, " +
                        "ARRAY[p.sha, c.sha] as sha_path " +
                        "FROM commit_parents cp " +
                        "JOIN commits p ON cp.parent_id = p.id " +
                        "JOIN commits c ON cp.child_id = c.id " +
                        "WHERE cp.parent_id = ? " +
                        "UNION ALL " +
                        // Recursive case: follow relationships
                        "SELECT r.source_id, cp.child_id, r.depth + 1, " +
                        "r.path || cp.child_id, " +
                        "r.sha_path || c.sha " +
                        "FROM reachable_commits r " +
                        "JOIN commit_parents cp ON r.target_id = cp.parent_id " +
                        "JOIN commits c ON cp.child_id = c.id " +
                        "WHERE NOT cp.child_id = ANY(r.path) " + // Prevent infinite recursion
                        "AND r.depth < 100" + // Reasonable depth limit
                        ") " +
                        // Find paths that would create a cycle
                        "SELECT sha_path FROM reachable_commits " +
                        "WHERE target_id = ? " +
                        "LIMIT 1"
        )) {
            stmt.setLong(1, startId);
            stmt.setLong(2, targetId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Array shaPathArray = rs.getArray(1);
                    String[] shaPath = (String[]) shaPathArray.getArray();
                    String cyclePath = String.join(" → ", shaPath) + " → " + childSha;
                    
                    throw new SQLException(
                        String.format("Cyclic dependency detected: Adding commit relationship %s → %s would create cycle: %s",
                            parentSha, childSha, cyclePath)
                    );
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to validate commit relationship: " + e.getMessage(), e);
        }
    }

    public void close() {
        // cleanup code
    }

    public void remove() {
        // cleanup code
    }
}
