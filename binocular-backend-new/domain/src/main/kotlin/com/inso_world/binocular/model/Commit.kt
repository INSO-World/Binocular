package com.inso_world.binocular.model

import com.inso_world.binocular.model.validation.Hexadecimal
import com.inso_world.binocular.model.validation.isHex
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Commit(
    @field:Size(min = 40, max = 40)
    @field:Hexadecimal
    val sha: String,
    @field:NotNull
    @field:Valid
    val authorSignature: Signature,
    @field:Valid
    @field:NotNull
    val committerSignature: Signature = authorSignature,
    val message: String? = null,
    @field:NotNull
    val repositoryId: Repository.Id,
) : AbstractDomainObject<Commit.Id, Commit.Key>(
        Id(Uuid.random())
    ) {
    @JvmInline
    value class Id(
        val value: Uuid
    )

    data class Key(
        val sha: String
    )

    @Deprecated("Avoid using database specific id, use business key", ReplaceWith("iid"))
    var id: String? = null

    var webUrl: String? = null

    @Deprecated("do not use")
    var branch: String? = null
    var stats: Stats? = null

    val authorId: Developer.Id
        get() = authorSignature.developerId

    val committerId: Developer.Id
        get() = committerSignature.developerId

    val authorDateTime: LocalDateTime
        get() = authorSignature.timestamp

    val commitDateTime: LocalDateTime
        get() = committerSignature.timestamp

    init {
        require(sha.length == 40) { "SHA must be 40 hex chars, got ${sha.length}" }
        require(sha.all { it.isHex() }) { "SHA-1 must be hex [0-9a-fA-F]" }
    }

    val parentIds: MutableSet<Commit.Id> = mutableSetOf()

    val childIds: MutableSet<Commit.Id> = mutableSetOf()

    override val uniqueKey: Key
        get() = Key(sha)

    override fun equals(other: Any?) = super.equals(other)

    override fun hashCode(): Int = super.hashCode()

    override fun toString(): String =
        "Commit(id=$id, sha='$sha', authorDateTime=$authorDateTime, commitDateTime=$commitDateTime, message=$message, webUrl=$webUrl, stats=$stats, authorId=$authorId, committerId=$committerId, repositoryId=$repositoryId, childIds=${childIds.map {
            it.value
        }}, parentIds=${parentIds.map { it.value }})"
}
