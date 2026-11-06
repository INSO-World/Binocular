package com.inso_world.binocular.ffi.extensions

import com.inso_world.binocular.ffi.internal.GixSignature
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature

/**
 * Map an FFI Git signature (`GixSignature`) into a domain [Developer] and [Signature], **preserving identity**.
 *
 * ### Semantics
 * - **Identity-preserving:** Finds the canonical [Developer] in `repository.developers` by
 *   git signature (`"Name <email>"`). If found, returns it; otherwise creates a new [Developer].
 * - **Repository registration:** A newly constructed [Developer] self-registers into
 *   `repository.developers` inside its `init` block (add-only, de-duplicated).
 * - **Signature creation:** Wraps the developer with the FFI timestamp into a domain [Signature].
 *
 * ### Guarantees & constraints
 * - Requires non-blank `name` and `email` (enforced by [Developer]).
 * - Business key uses trimmed name/email; equality/hash are identity-based (see [Developer.uniqueKey]).
 *
 * @param repository Owning [Repository] that scopes the developer identity.
 * @return The canonical [Developer] instance for the given repository and signature name.
 */
internal fun GixSignature.toDeveloper(repository: Repository): Developer {
    val nameTrimmed = this.name.trim()
    val emailTrimmed = this.email.trim()
    require(nameTrimmed.isNotBlank()) { "Signature name must not be blank" }
    require(emailTrimmed.isNotBlank()) { "Signature email must not be blank" }

    val gitSignature = "${nameTrimmed} <${emailTrimmed}>"
    val existing = repository.developers.firstOrNull { it.gitSignature == gitSignature }
    if (existing != null) {
        return existing
    }

    // Create new; Developer.init will register into repository.developers
    return Developer(
        name = nameTrimmed,
        email = emailTrimmed,
        repository = repository,
    )
}

/**
 * Convert to a domain [Signature] (Developer + timestamp).
 */
internal fun GixSignature.toSignature(repository: Repository): Signature =
    Signature(
        developer = this.toDeveloper(repository),
        timestamp = this.time.toLocalDateTime()
    )
