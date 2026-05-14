package com.inso_world.binocular.model.exceptions

/**
 * Thrown when a domain entity is associated with a different repository than expected.
 *
 * This replaces the generic [IllegalArgumentException] used for cross-repository
 * consistency checks, providing clearer error semantics in the domain layer.
 *
 * @param message Human-readable description of the mismatch.
 * @param entityName Name of the entity that caused the mismatch (e.g., "Remote", "Commit").
 * @param expectedRepositoryId The repository ID the entity belongs to.
 * @param actualRepositoryId The repository ID where the entity was being added.
 */
class RepositoryMismatchException(
    message: String,
    val entityName: String,
    val expectedRepositoryId: String,
    val actualRepositoryId: String,
) : IllegalArgumentException(message)
