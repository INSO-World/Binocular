package com.inso_world.binocular.model

import jakarta.validation.constraints.NotBlank
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Project(
    @field:NotBlank
    val name: String
) : AbstractDomainObject<Project.Id, Project.Key>(
    Id(Uuid.random())
) {
    @JvmInline
    value class Id(val value: Uuid)

    data class Key(val name: String)

    val issues: MutableSet<Issue> = mutableSetOf()

    val mergeRequests: MutableSet<MergeRequest> = mutableSetOf()

    var description: String? = null

    var repoId: Repository.Id? = null
        set(value) {
            requireNotNull(value) { "Cannot set repoId to null" }
            if (value == this.repoId) {
                return
            }
            if (this.repoId != null) {
                throw IllegalArgumentException("Repository already set for Project $name: $repoId")
            }
            field = value
        }

    @Deprecated("Avoid using database specific id, use business key .iid", ReplaceWith("iid"))
    var id: String? = null

    init {
        require(name.isNotBlank())
    }

    override fun toString(): String = "Project(id=$id, iid=$iid, name='$name', description=$description)"

    override val uniqueKey: Project.Key
        get() = Project.Key(this.name)

    override fun equals(other: Any?) = super.equals(other)
    override fun hashCode(): Int = super.hashCode()
}
