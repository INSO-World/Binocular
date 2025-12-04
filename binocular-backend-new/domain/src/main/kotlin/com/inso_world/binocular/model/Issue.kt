package com.inso_world.binocular.model

import com.inso_world.binocular.model.Repository.Id
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Domain model for an Issue, representing an issue in a Git repository.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
@OptIn(ExperimentalUuidApi::class)
data class Issue(
    var id: String? = null,
    var platformIid: Int? = null,
    val gid: String,
    var title: String? = null,
    var description: String? = null,
    var createdAt: LocalDateTime? = null,
    var closedAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null,
    var labels: List<String> = emptyList(),
    var state: String? = null,
    var webUrl: String? = null,
    var mentions: List<Mention> = emptyList(),
    // Relationships
    var project: Project,
    // var accounts: List<Account> = emptyList(),
    var commits: List<Commit> = emptyList(),
    var milestones: List<Milestone> = emptyList(),
    var notes: List<Note> = emptyList(),
    var users: List<User> = emptyList(),
) : AbstractDomainObject<Issue.Id, Issue.Key>(
    Id(Uuid.random())
)  {
    @JvmInline
    value class Id(val value: Uuid)

    data class Key(val projectId: Project.Id, val gid: String) // value object for lookups
    private val _accounts: MutableSet<Account> = mutableSetOf()

    val accounts: MutableSet<Account> =
        object: MutableSet<Account> by _accounts {
            override fun add(element: Account): Boolean {
                val added = _accounts.add(element)
                if (added) {
                    element.issues.add(this@Issue)
                }
                return added
            }
        }

    override fun toString(): String {
        return "Issue(no=${iid.toString()}, title=$title, accounts=${accounts.map { it.format() }}"
    }

    override val uniqueKey: Key
        get() = Issue.Key(project.iid, gid)
}
