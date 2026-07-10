package com.inso_world.binocular.model

import com.inso_world.binocular.model.vcs.ReferenceCategory
import jakarta.validation.constraints.NotBlank
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class Branch(
    @field:NotBlank val name: String,
    @field:NotBlank val fullName: String,
    override val category: ReferenceCategory,
    override val repositoryId: Repository.Id,
    headCommitId: Commit.Id,
) : Reference<Branch.Key>(category, repositoryId),
    Cloneable {
    @JvmInline
    value class Id(
        val value: Uuid
    )

    data class Key(
        val repositoryId: Repository.Id,
        val name: String
    )

    @Deprecated("Avoid using database specific id, use business key", ReplaceWith("iid"))
    var id: String? = null

    @Deprecated("old")
    var active: Boolean = false

    @Deprecated("old")
    var tracksFileRenames: Boolean = false

    @Deprecated("", ReplaceWith("headCommitId"))
    val latestCommit: Commit.Id
        get() = headCommitId

    @Deprecated("legacy, use name property instead", replaceWith = ReplaceWith("name"))
    val branch: String = name

    var headCommitId: Commit.Id = headCommitId

    init {
        require(name.isNotBlank()) { "name must not be blank" }
        require(fullName.isNotBlank()) { "fullName must not be blank" }
    }

    override val uniqueKey: Key
        get() = Key(repositoryId, this.name)

    override fun equals(other: Any?) = super.equals(other)

    override fun hashCode(): Int = super.hashCode()

    override fun toString(): String =
        "Branch(id=$id, iid=$iid, name='$name', fullName='$fullName', category=$category, active=$active, tracksFileRenames=$tracksFileRenames, headCommitId=$headCommitId, repositoryId=$repositoryId)"
}
