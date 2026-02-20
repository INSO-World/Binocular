package com.inso_world.binocular.infrastructure.arangodb.migration

import com.arangodb.ArangoDatabase
import com.inso_world.binocular.core.delegates.logger
import org.springframework.stereotype.Component

/**
 * Backfills the `head` `@Ref` field on branch documents.
 *
 * The current
 * [BranchEntity][com.inso_world.binocular.infrastructure.arangodb.persistence.entity.BranchEntity]
 * requires a non-null `@Ref head` field pointing to a
 * [CommitEntity][com.inso_world.binocular.infrastructure.arangodb.persistence.entity.CommitEntity],
 * but legacy branch documents only stored the commit SHA in the deprecated
 * `latestCommit` field.
 *
 * This migration resolves each branch's `latestCommit` SHA to the corresponding
 * commit document ID and sets the `head` reference.
 */
@Component
@Suppress("ktlint:standard:class-naming")
class V006_AddHeadBranchRef : Migration {
    companion object {
        private val logger by logger()
    }

    override val version = 1771592191
    override val description = "Add head commit reference to branch based on latestCommit"

    override fun migrate(db: ArangoDatabase) {
        backfillHeadBranchRef(db)
    }

    /**
     * For every branch that has a `latestCommit` SHA but no `head` reference,
     * looks up the commit by SHA and sets the `head` field to its document ID.
     *
     * Legacy branches stored only the commit SHA in `latestCommit` before the
     * `@Ref head` field was introduced on
     * [BranchEntity][com.inso_world.binocular.infrastructure.arangodb.persistence.entity.BranchEntity].
     */
    private fun backfillHeadBranchRef(db: ArangoDatabase) {
        val result =
            db
                .query(
                    """
                    FOR b IN branches
                    FILTER b.latestCommit != null AND b.head == null
                    LET commit = FIRST(
                        FOR c IN commits
                        FILTER c.sha == b.latestCommit
                        LIMIT 1
                        RETURN c._id
                    )
                    FILTER commit != null
                    UPDATE b WITH { head: commit } IN branches
                    RETURN 1
                    """.trimIndent(),
                    Int::class.java,
                ).asListRemaining()

        val updatedCount = result.size
        if (updatedCount == 0) {
            logger.info("No branches need head commit backfill")
        } else {
            logger.info("Updated {} branch(es) with head commit reference", updatedCount)
        }
    }
}
