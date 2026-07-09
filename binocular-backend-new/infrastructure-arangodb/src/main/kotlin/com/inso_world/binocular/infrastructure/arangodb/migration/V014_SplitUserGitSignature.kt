package com.inso_world.binocular.infrastructure.arangodb.migration

import com.arangodb.ArangoDatabase
import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.infrastructure.arangodb.InfrastructureConfig
import org.springframework.stereotype.Component

/**
 * Splits the combined `gitSignature` on `users` documents into first-class `name` and `email`
 * fields so that [User.UniqueKey] can key on `(repository.iid, name, email)` instead of
 * `[repository.iid, name]` alone.
 *
 * ### Problem
 * `User.uniqueKey = Key(repository.iid, name)` — coarser than the entity.
 * Real-world data contains developers who appear under one name with several distinct emails
 * (e.g. `Maximilian Zenz` with 4 emails). Each such group collapses into one name-only key
 * (`d2e` +1) while remaining N distinct entities (`e2d` +N), breaking the
 * `MappingContext.remember` invariant.
 *
 * ### Fix
 * For every `users` document:
 * - Parse `name` = text before `<`, `email` = text inside `<...>` (same regexes as the old
 *   `UserEntity.name`/`email` getters).
 * - If `email` is missing or blank, default from the config template by substituting
 *   `{name}` (sanitised) into `binocular.arangodb.migration.defaultUserEmailTemplate`.
 * - Write `name` and `email` fields; **leave `gitSignature` intact** — it is the match key
 *   used by `V004`/`V005` (`FILTER d.gitSignature == u.gitSignature`) and `CommitFileConnectionDao`,
 *   so it must not be removed.
 */
@Component
@Suppress("ktlint:standard:class-naming")
class V014_SplitUserGitSignature(
    private val infraConfig: InfrastructureConfig,
) : Migration {
    companion object {
        private val logger by logger()
        private const val NAME_REGEX = """^(.+?)\s*<"""
        private const val EMAIL_REGEX = """<([^>]+)>$"""
    }

    override val version = 1783700300
    override val description = "Split gitSignature into persisted name and email fields on users collection"

    override fun migrate(db: ArangoDatabase) {
        // Get all user _keys
        val userKeys =
            db
                .query(
                    "FOR u IN users RETURN u._key",
                    String::class.java,
                ).asListRemaining()

        if (userKeys.isEmpty()) {
            logger.info("No users found, skipping migration")
            return
        }

        val namePattern = Regex(NAME_REGEX)
        val emailPattern = Regex(EMAIL_REGEX)
        val defaultTemplate = infraConfig.arangodb.migration.defaultUserEmailTemplate

        val updates = mutableListOf<Map<String, Any>>()

        for (key in userKeys) {
            // Fetch the document
            @Suppress("UNCHECKED_CAST")
            val docs =
                db
                    .query(
                        "FOR u IN users FILTER u._key == @key LIMIT 1 RETURN u",
                        Map::class.java,
                        mapOf("key" to key),
                    ).asListRemaining()

            if (docs.isEmpty()) continue

            val doc = docs[0] as Map<String, Any>
            val gitSig = doc["gitSignature"] as? String ?: ""

            val parsedName =
                namePattern
                    .find(gitSig)
                    ?.groupValues
                    ?.get(1)
                    ?.trim()
            val parsedEmail = emailPattern.find(gitSig)?.groupValues?.get(1)

            val effectiveName = parsedName ?: continue
            val effectiveEmail =
                parsedEmail?.takeIf { it.isNotBlank() }
                    ?: sanitizeAndApplyTemplate(effectiveName, defaultTemplate)

            updates +=
                mapOf(
                    "_key" to key,
                    "name" to effectiveName,
                    "email" to effectiveEmail,
                )
        }

        if (updates.isEmpty()) {
            logger.info("No users need name/email migration")
            return
        }

        // Batch update
        db.query(
            """
            FOR updt IN @updates
            UPDATE updt._key WITH { name: updt.name, email: updt.email } IN users
            """.trimIndent(),
            Void::class.java,
            mapOf("updates" to updates),
        )

        logger.info("Updated {} users with persisted name and email", updates.size)
    }

    /**
     * Sanitise a display name and substitute it into the default email template.
     *
     * Replaces whitespace and non-alphanumeric characters (except dots/hyphens) with underscores,
     * then lowercases the result before plugging into the template.
     */
    private fun sanitizeAndApplyTemplate(
        name: String,
        template: String,
    ): String {
        val sanitised = name.replace(Regex("[^a-zA-Z0-9.-]"), "_").lowercase()
        return template.replace("{name}", sanitised)
    }
}
