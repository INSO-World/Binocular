package com.inso_world.binocular.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Domain model for a Note, representing a comment or note in a Git repository.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
@OptIn(ExperimentalUuidApi::class)
data class Note(
    var id: String? = null,
    var body: String,
    var createdAt: String,
    var updatedAt: String,
    var system: Boolean = true,
    var resolvable: Boolean = false,
    var confidential: Boolean = false,
    var internal: Boolean = false,
    var imported: Boolean = false,
    var importedFrom: String,
    // Relationships
    val issues: MutableSet<Issue> = NonRemovingMutableSet(),
    val mergeRequests: MutableSet<MergeRequest> = NonRemovingMutableSet(),
) : AbstractDomainObject<Note.Id, Note.Key>(
    Id(Uuid.random())
) {
    @JvmInline
    value class Id(val value: Uuid)

    private val _accounts: MutableSet<Account> = mutableSetOf()

    val accounts: MutableSet<Account> =
        object : MutableSet<Account> by _accounts {
            override fun add(element: Account): Boolean {
                val added = _accounts.add(element)
                if (added) {
                    element.notes.add(this@Note)
                }
                return added
            }

            override fun addAll(elements: Collection<Account>): Boolean {
                var anyAdded = false
                for (element in elements) {
                    if (add(element)) anyAdded = true
                }
                return anyAdded
            }
        }

    data class Key(val body: String, val createdAt: String) // value object for lookups

    override val uniqueKey: Key
        get() = Key(body ?: "", createdAt ?: "")
}
