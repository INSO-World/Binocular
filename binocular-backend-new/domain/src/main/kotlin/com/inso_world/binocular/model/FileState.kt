package com.inso_world.binocular.model

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

data class FileState(
    var id: String? = null,
    val content: String? = null,
    val commit: Commit,
    val file: File,
) : AbstractDomainObject() {
    override fun uniqueKey(): String = "${commit.sha},${file.path}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileState

        if (id != other.id) return false
        if (content != other.content) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (content?.hashCode() ?: 0)
        return result
    }

    @OptIn(ExperimentalEncodingApi::class)
    override fun toString(): String =
        "FileState(id=$id, content=${Base64.Default.encode(
            content?.trim()?.encodeToByteArray() ?: ByteArray(0),
        )}, commit=${commit.sha}, file=${file.path})"
}
