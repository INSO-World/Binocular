-- prevent_cycles_trigger.sql

-- 1) Create or replace the cycle‑check function
CREATE
OR REPLACE FUNCTION prevent_cycles()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
  -- A) direct self‑loop
  IF
NEW.commit_id = NEW.parent_id THEN
    RAISE EXCEPTION 'Cycle detected: commit % cannot be its own parent', NEW.commit_id;
END IF;

  -- B) any indirect cycle?
  IF
EXISTS (
    WITH RECURSIVE ancestors(commit_id, parent_id) AS (
      -- start from the would‑be parent
      SELECT commit_id, parent_id
      FROM commit_parents
      WHERE commit_id = NEW.parent_id
      UNION ALL
      -- walk up the tree
      SELECT cp.commit_id, cp.parent_id
      FROM commit_parents cp
      JOIN ancestors a ON cp.commit_id = a.parent_id
    )
    -- if, somewhere in that chain, we find NEW.commit_id as a parent, it's a cycle
    SELECT 1
    FROM ancestors
    WHERE parent_id = NEW.commit_id
  ) THEN
    RAISE EXCEPTION 'Cycle detected: adding % → % would create a cycle',
                    NEW.parent_id, NEW.commit_id;
END IF;

RETURN NEW;
END;
$$;

-- 2) Attach it as a BEFORE trigger on your edge table
CREATE TRIGGER check_commit_cycle
    BEFORE INSERT OR
UPDATE
    ON commit_parents
    FOR EACH ROW
    EXECUTE FUNCTION prevent_cycles();
