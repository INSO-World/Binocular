package com.inso_world.binocular.infrastructure.arangodb.migration

import com.arangodb.ArangoDatabase
import com.inso_world.binocular.core.delegates.logger
import org.springframework.stereotype.Component

/**
 * Backfills `repository` `@Ref` fields on user documents.
 *
 * The current
 * [UserEntity][com.inso_world.binocular.infrastructure.arangodb.persistence.entity.UserEntity]
 * requires a non-null `@Ref repository` field, but legacy user documents
 * pre-date the repository reference and lack it entirely.
 *
 * This migration sets the `repository` field on every user that is missing it,
 * using the first available repository as the target (legacy data typically
 * belongs to a single repository).
 */
@Component
@Suppress("ktlint:standard:class-naming")
class V005_AddRepositoryUserRef : Migration {
    companion object {
        private val logger by logger()
    }

    override val version = 1780313638

    override val description = "Add repository reference to user"

    override fun migrate(db: ArangoDatabase) {
        backfillUserRepositoryRef(db)
    }

    /**
     * For every user that has no `repository` field, sets it to the first
     * available repository document.
     *
     * Legacy users were created before the repository reference was introduced
     * on [UserEntity][com.inso_world.binocular.infrastructure.arangodb.persistence.entity.UserEntity],
     * so they need to be linked to an existing repository to prevent a
     * [NullPointerException] during deserialization.
     */
    private fun backfillUserRepositoryRef(db: ArangoDatabase) {
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
                    FOR c IN users
                    FILTER c.repository == null
                    UPDATE c WITH { repository: repo } IN users
                    RETURN 1
                    """.trimIndent(),
                    Int::class.java,
                ).asListRemaining()

        val updatedCount = result.size
        if (updatedCount == 0) {
            logger.info("No users need repository backfill")
        } else {
            logger.info("Updated {} user(s) with repository reference", updatedCount)
        }
    }
}
