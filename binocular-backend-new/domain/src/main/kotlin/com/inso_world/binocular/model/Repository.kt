package com.inso_world.binocular.model

import com.inso_world.binocular.model.vcs.Remote
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Repository(
    @field:NotBlank
    @field:Size(max = 255)
    val localPath: String,
    val projectId: Project.Id,
) : AbstractDomainObject<Repository.Id, Repository.Key>(
        Id(Uuid.random())
    ) {
    @JvmInline
    value class Id(
        val value: Uuid
    )

    data class Key(
        val projectId: Project.Id,
        val localPath: String
    )

    @Deprecated("Avoid using database specific id, use business key .iid", ReplaceWith("iid"))
    var id: String? = null

    init {
        require(localPath.trim().isNotBlank()) { "localPath cannot be blank." }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(Repository::class.java)

        @JvmStatic
        fun create(
            localPath: String,
            projectId: Project.Id,
        ): Repository = Repository(localPath = localPath, projectId = projectId)
    }

    val commitIds: MutableSet<Commit.Id> = mutableSetOf()

    val branchIds: MutableSet<Branch.Id> = mutableSetOf()

    @Deprecated("Use developers instead", ReplaceWith("developers"))
    val user: MutableSet<User>
        get() = _legacyUsers

    private val _legacyUsers: MutableSet<User> = NonRemovingMutableSet<User>()

    val developers: MutableSet<Developer.Id> = mutableSetOf()

    val remoteIds: MutableSet<Remote.Id> =
        object : AbstractMutableSet<Remote.Id>() {
            val backing =
                java.util.concurrent.ConcurrentHashMap
                    .newKeySet<Remote.Id>()

            override val size: Int get() = backing.size

            override fun isEmpty(): Boolean = backing.isEmpty()

            override fun contains(element: Remote.Id): Boolean = backing.contains(element)

            override fun containsAll(elements: Collection<Remote.Id>): Boolean = backing.containsAll(elements)

            override fun add(element: Remote.Id): Boolean = backing.add(element)

            override fun addAll(elements: Collection<Remote.Id>): Boolean = backing.addAll(elements)

            override fun clear(): Unit = fail()

            override fun remove(element: Remote.Id): Boolean = fail()

            override fun removeAll(elements: Collection<Remote.Id>): Boolean = fail()

            override fun retainAll(elements: Collection<Remote.Id>): Boolean = fail()

            override fun iterator(): MutableIterator<Remote.Id> {
                val it = backing.iterator()
                return object : MutableIterator<Remote.Id> {
                    override fun hasNext(): Boolean = it.hasNext()

                    override fun next(): Remote.Id = it.next()

                    override fun remove(): Unit = fail()
                }
            }

            override fun toString(): String = backing.toString()

            private fun <T> fail(): T = throw UnsupportedOperationException("Removing objects is not allowed.")
        }

    override fun toString(): String = "Repository(id=$id, iid=$iid, localPath='$localPath', projectId=$projectId)"

    override val uniqueKey: Key
        get() = Key(projectId, localPath.trim())

    override fun equals(other: Any?) = super.equals(other)

    override fun hashCode(): Int = super.hashCode()
}
