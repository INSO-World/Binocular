package com.inso_world.binocular.ffi.extensions

import com.inso_world.binocular.internal.BinocularSig
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User

internal fun BinocularSig.toDomain(
    repository: Repository,
//    usersByKey: MutableMap<String, User>,
): User {
//    val key = userKey(repository)
//    return usersByKey.getOrPut(key) {
    return User(
        name = this.name.toString(),
        email = this.email.toString(),
        repository = repository,
    )
//    }
}

/** Stable identity key for users per domain’s uniqueKey() contract. */
internal fun BinocularSig.userKey(repository: Repository): String = "${repository.localPath},${this.email}"
