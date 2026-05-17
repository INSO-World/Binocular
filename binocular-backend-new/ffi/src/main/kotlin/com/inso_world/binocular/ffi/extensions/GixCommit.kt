package com.inso_world.binocular.ffi.extensions

import com.inso_world.binocular.ffi.internal.GixCommit
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Repository

private fun String.validateSha(): String {
    require(this.length == 40) {
        "Invalid SHA '$this': must be exactly 40 characters, got ${this.length}"
    }
    require(this.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) {
        "Invalid SHA '$this': must contain only hexadecimal characters [0-9a-fA-F]"
    }
    return this
}

internal fun GixCommit.toDomain(
    repositoryId: Repository.Id,
    shaIndex: MutableMap<String, Commit>? = null,
    developerRegistry: MutableMap<String, Developer> = mutableMapOf(),
): Commit {
    this.oid.validateSha()

    val authorSignature = this.author.toSignature(developerRegistry)
    val committerSignature = this.committer.toSignature(developerRegistry)

    val existing = shaIndex?.get(this.oid)

    val commit =
        existing ?: Commit(
            sha = this.oid,
            authorSignature = authorSignature,
            committerSignature = if (authorSignature == committerSignature) authorSignature else committerSignature,
            message = this.message,
            repositoryId = repositoryId,
        )

    return commit
}

internal fun Collection<GixCommit>.toDomain(
    repositoryId: Repository.Id,
    existingCommits: Map<String, Commit> = emptyMap(),
    developerRegistry: MutableMap<String, Developer> = mutableMapOf(),
): List<Commit> {
    val bySha = existingCommits.toMutableMap()

    val mappedInOrder: List<Commit> = this.map { vec ->
        val c = vec.toDomain(repositoryId, bySha, developerRegistry)
        bySha.putIfAbsent(c.sha, c)
        c
    }

    this.forEach { vec ->
        val child = bySha.getValue(vec.oid)
        vec.parents.forEach { parentSha ->
            parentSha.validateSha()
            val parent = bySha.getValue(parentSha)
            child.parentIds.add(parent.iid)
            parent.childIds.add(child.iid)
        }
    }

    return mappedInOrder
}
