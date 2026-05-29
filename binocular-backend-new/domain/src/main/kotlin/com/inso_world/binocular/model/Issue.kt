package com.inso_world.binocular.model

import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Domain model for an Issue, representing an issue in a Git repository.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
@OptIn(ExperimentalUuidApi::class)
data class Issue(

    @Deprecated("Avoid using database specific id, use business key", ReplaceWith("iid"))
    var id: String? = null,
    var platformIid: Int? = null, // issue number from e.g. GitHub
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
    val project: Project.Id,
    val commits: MutableSet<Commit> = NonRemovingMutableSet(),
    val milestones: MutableSet<Milestone> = NonRemovingMutableSet(),
    val notes: MutableSet<Note> = NonRemovingMutableSet(),
    var users: List<User> = emptyList(),
) : AbstractDomainObject<Issue.Id, Issue.Key>(
    Id(Uuid.random())
)  {
    @JvmInline
    value class Id(val value: Uuid)

    data class Key(val projectId: Project.Id, val gid: String) // value object for lookups

    var author: Account? = null
        set(value) {
            requireNotNull(value) { "Author must not be null" }
            if (value == this.author) {
                return
            }
            if (this.author != null) {
                throw IllegalArgumentException("Author already set for Issue $platformIid: $author")
            }
//            if (this.project !in value.projects) {
//                throw IllegalArgumentException("Author ${value.login} is not in Issues Project: ${this.project.name}")
//            }
            field = value
            value.issues.add(this)
        }

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
        get() = Issue.Key(project, gid)
}
