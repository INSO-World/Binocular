package com.inso_world.binocular.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.Objects
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Domain model for an Account, representing a user account from a platform like GitHub or GitLab.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
@OptIn(ExperimentalUuidApi::class)
data class Account(

    @field:NotBlank
    val gid: String,
    @field:NotNull
    val platform: Platform,
    @field:NotBlank
    val login: String,
    @field:NotNull
    val projects: MutableSet<Project> = mutableSetOf(),

) : AbstractDomainObject<Account.Id, Account.Key>(
    Id(Uuid.random())
) {
    var name: String? = null
    var avatarUrl: String? = null
    var url: String? = null

    // TODO MRs and notes (those are still like in the old implementation)
    var mergeRequests: Set<MergeRequest> = emptySet()
    var notes: Set<Note> = emptySet()

    @Deprecated("Avoid using database specific id, use business key", ReplaceWith("iid"))
    var id: String? = null

    @JvmInline
    value class Id(val value: Uuid)

    data class Key(val platform: Platform, val gid: String) // value object for lookups

    val issues: MutableSet<Issue> = object : NonRemovingMutableSet<Issue>() {
        override fun add(element: Issue): Boolean {
            require(element.project in this@Account.projects) {
                "Issue.project (${element.project}) doesn't match any of the account's projects (${this@Account.projects})."
            }
            val added = super.add(element)
            return added
        }

        override fun addAll(elements: Collection<Issue>): Boolean {
            var anyAdded = false
            for (element in elements) {
                if (add(element)) anyAdded = true
            }
            return anyAdded
        }
    }

//    private val _issues: MutableSet<Issue> = mutableSetOf()
//    val issues: MutableSet<Issue> =
//        object: MutableSet<Issue> by _issues {
//            override fun add(element: Issue): Boolean {
//                val added = _issues.add(element)
//                if (added) {
//                    element.accounts.add(this@Account)
//                }
//                return added
//            }
//        }

    override val uniqueKey: Key
        get() = Key(platform, gid.trim())

    override fun hashCode(): Int = super.hashCode()
    override fun equals(other: Any?) = super.equals(other)

    fun format(): String {
        return "Account(id=$gid, login=$login, name=$name)"
    }

}
