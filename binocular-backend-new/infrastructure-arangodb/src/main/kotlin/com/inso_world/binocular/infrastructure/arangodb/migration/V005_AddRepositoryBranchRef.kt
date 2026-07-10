package com.inso_world.binocular.infrastructure.arangodb.migration

import com.arangodb.ArangoDatabase
import com.inso_world.binocular.core.delegates.logger
import org.springframework.stereotype.Component

/**
 * Backfills `repository` `@Ref` fields on commit documents.
 *
 * The current
 * [BranchEntity][com.inso_world.binocular.infrastructure.arangodb.persistence.entity.BranchEntity]
 * requires a non-null `@Ref repository` field, but legacy commit documents
 * pre-date the repository reference and lack it entirely.
 *
 * This migration sets the `repository` field on every commit that is missing it,
 * using the first available repository as the target (legacy data typically
 * belongs to a single repository).
 */
@Component
@Suppress("ktlint:standard:class-naming")
class V005_AddRepositoryBranchRef : Migration {
    companion object {
        private val logger by logger()
    }

    override val version = 1780311796
    override val description = "Add repository reference to branch"

    override fun migrate(db: ArangoDatabase) {
        backfillBranchRepositoryRef(db)
    }

    /**
     * For every branch that has no `repository` field, sets it to the first
     * available repository document.
     *
     * Legacy branches were created before the repository reference was introduced
     * on [BranchEntity][com.inso_world.binocular.infrastructure.arangodb.persistence.entity.BranchEntity],
     * so they need to be linked to an existing repository to prevent a
     * [NullPointerException] during deserialization.
     */
    private fun backfillBranchRepositoryRef(db: ArangoDatabase) {
        val result =
            db
                .query(
                    """
                    LET repo = FIRST(
                        FOR r IN repositories
                        LIMIT 1
                        RETURN r._id
                    )
                    FILTER repo != null
                    FOR c IN branches
                    FILTER c.repository == null
                    UPDATE c WITH { repository: repo } IN branches
                    RETURN 1
                    """.trimIndent(),
                    Int::class.java,
                ).asListRemaining()

        val updatedCount = result.size
        if (updatedCount == 0) {
            logger.info("No branches need repository backfill")
        } else {
            logger.info("Updated {} branch(es) with repository reference", updatedCount)
        }
    }
}
