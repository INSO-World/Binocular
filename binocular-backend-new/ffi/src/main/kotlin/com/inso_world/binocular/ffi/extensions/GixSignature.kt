package com.inso_world.binocular.ffi.extensions

import com.inso_world.binocular.ffi.internal.GixSignature
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User

/**
 * Map an FFI Git signature (`GixSignature`) into a domain [User], **preserving identity**.
 *
 * ### Semantics
 * - **Identity-preserving:** Finds the canonical [User] in `repository.user` by business key
 *   `(repository.iid, name.trim())`. If found, returns it; otherwise creates a new [User].
 * - **Email enrichment (idempotent):** If the FFI signature provides a non-blank email and the
 *   domain user’s email is currently `null`, the email is assigned (setter enforces non-blank).
 * - **Repository registration:** A newly constructed [User] self-registers into `repository.user`
 *   inside its `init` block (add-only, de-duplicated).
 *
 * ### Guarantees & constraints
 * - Business key uses `name.trim()`; equality/hash are identity-based (see [User.uniqueKey]).
 * - Setting a blank email is rejected by the domain model’s setter.
 *
 * @param repository Owning [Repository] that scopes the user identity.
 * @return The canonical [User] instance for the given repository and signature name.
 */
internal fun GixSignature.toDomain(
    repository: Repository,
): User {
    val normalizedName = this.name.trim()
    val existing = repository.user.firstOrNull { it.uniqueKey.name == normalizedName }

    // Extract and normalize email from FFI
    val ffiEmail = this.email.trim()

    if (existing != null) {
        // Enrich email once, if domain doesn't have it yet
        if (existing.email == null && ffiEmail.isNotBlank()) {
            existing.email = ffiEmail
        }
        return existing
    }

    // Create new; User.init will register into repository.user
    return User(
        name = normalizedName,
        repository = repository,
    ).also { user ->
        if (ffiEmail.isNotBlank()) {
            user.email = ffiEmail
        }
    }
}
