package com.inso_world.binocular.model

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Revision(
    val content: String? = null,
    val commitId: Commit.Id,
    val fileId: File.Id,
) : AbstractDomainObject<Revision.Id, String>(
        Id(Uuid.random())
    ) {
    @JvmInline
    value class Id(
        val value: Uuid
    )

    @Deprecated("Avoid using database specific id, use business key .iid", ReplaceWith("iid"))
    var id: String? = null

    override val uniqueKey: String
        get() = "${commitId.value},${fileId.value}"

    override fun equals(other: Any?) = other is Revision && other.iid == iid

    override fun hashCode(): Int = iid.hashCode()

    @OptIn(ExperimentalEncodingApi::class)
    override fun toString(): String =
        "Revision(iid=$iid, id=$id, content=${
            Base64.Default.encode(
                content?.trim()?.encodeToByteArray() ?: ByteArray(0),
            )
        }, commitId=$commitId, fileId=$fileId)"
}
