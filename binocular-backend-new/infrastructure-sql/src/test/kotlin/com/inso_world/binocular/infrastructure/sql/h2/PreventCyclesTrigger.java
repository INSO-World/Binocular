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

        // Check direct self-loop
        if (this.childId.equals(this.parentId)) {
            throw new SQLException("Cyclic dependency detected: Commit " + this.childId + " cannot be its own parent");
        }

        // Check for cycles in both directions
        checkCycles(conn, this.parentId, this.childId);
        checkCycles(conn, this.childId, this.parentId);
    }

    private void checkCycles(Connection conn, Long startId, Long targetId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                // Find all reachable commits from the start point
                "WITH RECURSIVE reachable_commits(source_id, target_id, depth, path) AS (" +
                        // Base case: direct relationships
                        "SELECT parent_id as source_id, child_id as target_id, 1 as depth, " +
                        "ARRAY[parent_id, child_id] as path " +
                        "FROM commit_parents " +
                        "WHERE parent_id = ? " +
                        "UNION ALL " +
                        // Recursive case: follow relationships
                        "SELECT r.source_id, cp.child_id, r.depth + 1, " +
                        "r.path || ARRAY[cp.child_id] " +
                        "FROM reachable_commits r " +
                        "JOIN commit_parents cp ON r.target_id = cp.parent_id " +
                        "WHERE NOT cp.child_id = ANY(r.path) " + // Prevent infinite recursion
                        "AND r.depth < 100" + // Reasonable depth limit
                        ") " +
                        // Find paths that would create a cycle
                        "SELECT path FROM reachable_commits " +
                        "WHERE target_id = ? " +
                        "LIMIT 1"
        )) {
            stmt.setLong(1, startId);
            stmt.setLong(2, targetId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Array pathArray = rs.getArray(1);
                    Long[] path = (Long[]) pathArray.getArray();
                    StringBuilder cycleStr = new StringBuilder();
                    for (int i = 0; i < path.length; i++) {
                        if (i > 0) cycleStr.append(" → ");
                        cycleStr.append(path[i]);
                    }
                    
                    throw new SQLException(
                        String.format("Cyclic dependency detected: Adding commit relationship %d → %d would create cycle: %s",
                            this.parentId, this.childId, cycleStr.toString())
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
