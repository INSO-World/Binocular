-- prevent_cycles_trigger.sql

-- 1) Create or replace the cycle-check function
CREATE OR REPLACE FUNCTION prevent_cycles()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    cycle_path text[];
    new_child_sha text;
    new_parent_sha text;
BEGIN
    -- Get the SHAs for the new relationship
    SELECT sha INTO new_child_sha FROM commits WHERE id = NEW.child_id;
    SELECT sha INTO new_parent_sha FROM commits WHERE id = NEW.parent_id;

    -- A) Check direct self-loop
    IF NEW.child_id = NEW.parent_id THEN
        RAISE EXCEPTION 'Cyclic dependency detected: Commit % cannot be its own parent', new_child_sha;
    END IF;

    -- C) Check for cycles by verifying if the child can reach the parent
    WITH RECURSIVE reachable_commits(source_id, target_id, depth, path, sha_path) AS (
        -- Base case: direct relationships (path from child_id to parent_id)
        SELECT cp.parent_id as source_id, cp.child_id as target_id, 1 as depth,
               ARRAY[cp.parent_id, cp.child_id] as path,
               ARRAY[p.sha, c.sha]::VARCHAR[] as sha_path
        FROM commit_parents cp
        JOIN commits p ON cp.parent_id = p.id
        JOIN commits c ON cp.child_id = c.id
        WHERE cp.parent_id = NEW.child_id
        UNION ALL
        -- Recursive case: follow relationships
        SELECT r.source_id, cp.child_id, r.depth + 1,
               r.path || cp.child_id,
               (r.sha_path || c.sha)::VARCHAR[]
        FROM reachable_commits r
        JOIN commit_parents cp ON r.target_id = cp.parent_id
        JOIN commits c ON cp.child_id = c.id
        WHERE NOT cp.child_id = ANY(r.path)  -- Prevent infinite recursion
        AND r.depth < 100  -- Reasonable depth limit
    )
    SELECT sha_path INTO cycle_path
    FROM reachable_commits
    WHERE target_id = NEW.parent_id
    LIMIT 1;

    IF FOUND THEN
        RAISE EXCEPTION 'Cyclic dependency detected: Adding commit relationship % → % would create cycle: %',
            new_parent_sha, new_child_sha, array_to_string(cycle_path || new_child_sha, ' → ');
    END IF;

    RETURN NEW;
END;
$$;

-- 2) Attach it as a BEFORE trigger on the edge table
DROP TRIGGER IF EXISTS check_commit_cycle ON commit_parents;
CREATE TRIGGER check_commit_cycle
    BEFORE INSERT OR UPDATE ON commit_parents
    FOR EACH ROW
    EXECUTE FUNCTION prevent_cycles();
