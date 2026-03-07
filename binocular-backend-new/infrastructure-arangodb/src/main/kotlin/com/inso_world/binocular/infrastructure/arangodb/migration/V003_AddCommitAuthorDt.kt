package com.inso_world.binocular.infrastructure.arangodb.migration

import com.arangodb.ArangoDatabase
import com.inso_world.binocular.core.delegates.logger
import org.springframework.stereotype.Component

@Component
@Suppress("ktlint:standard:class-naming")
class V003_AddCommitAuthorDt : Migration {
    companion object {
        private val logger by logger()
    }

    override val version = 1770645779
    override val description = "Add authorDateTime field to commits collection"

    override fun migrate(db: ArangoDatabase) {
        // Update all commits that don't have authorDateTime, setting it to their existing date field.
        // DATE_ISO8601 normalises the stored date, then we strip the trailing "Z" so the value
        // is a valid LocalDateTime string (e.g. "2016-11-16T13:22:07.000").
        val result =
            db
                .query(
                    """
                    FOR c IN commits
                    FILTER c.authorDateTime == null AND c.date != null
                    LET iso = DATE_ISO8601(c.date)
                    LET localDt = REGEX_REPLACE(iso, "Z${'$'}", "")
                    UPDATE c WITH { authorDateTime: localDt, commitDateTime: localDt } IN commits
                    RETURN 1
                    """.trimIndent(),
                    Int::class.java,
                ).asListRemaining()

        val updatedCount = result.size
        if (updatedCount == 0) {
            logger.info("No commits need authorDateTime backfill")
        } else {
            logger.info("Updated {} commits with new authorDateTime", updatedCount)
        }
    }
}
