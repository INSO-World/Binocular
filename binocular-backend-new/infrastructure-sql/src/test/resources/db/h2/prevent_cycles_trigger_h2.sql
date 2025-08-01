CREATE TRIGGER IF NOT EXISTS  check_commit_cycle_insert
    BEFORE INSERT
    ON commit_parents
    FOR EACH ROW
CALL "com.inso_world.binocular.infrastructure.sql.h2.PreventCyclesTrigger";

CREATE TRIGGER IF NOT EXISTS check_commit_cycle_update
    BEFORE UPDATE
    ON commit_parents
    FOR EACH ROW
CALL "com.inso_world.binocular.infrastructure.sql.h2.PreventCyclesTrigger";
