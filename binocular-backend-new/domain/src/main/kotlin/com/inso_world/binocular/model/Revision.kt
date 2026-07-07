@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package com.inso_world.binocular.model

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Revision(
    val content: String? = null,
    val commitSha: String,
    val filePath: String,
) : AbstractDomainObject<Revision.Id, String>(
    Id(Uuid.random())
) {
    @JvmInline
    value class Id(override val value: Uuid) : DomainId

    // some database dependent id
    @Deprecated("Avoid using database specific id, use business key .iid", ReplaceWith("iid"))
    var id: String? = null

    override val uniqueKey: String
        get() = "$commitSha,$filePath"

    // Entities compare by immutable identity only
    override fun equals(other: Any?) = other is Revision && other.iid == iid
    override fun hashCode(): Int = iid.hashCode()

    @OptIn(ExperimentalEncodingApi::class)
    override fun toString(): String =
        "Revision(iid=$iid, id=$id, content=${
            Base64.Default.encode(
                content?.trim()?.encodeToByteArray() ?: ByteArray(0),
            )
        }, commitSha=$commitSha, filePath=$filePath)"
}
