package com.inso_world.binocular.web.graphql.error

import com.inso_world.binocular.web.exception.NotFoundException

/**
 * Utility class for GraphQL validation and precondition checking.
 */
object GraphQLValidationUtils {
    /**
     * Throws a NotFoundException if the entity is null.
     *
     * @param entity The entity to check
     * @param entityType The type of entity (for the error message)
     * @param id The ID of the entity (for the error message)
     * @return The non-null entity
     * @throws NotFoundException if the entity is null
     */
    fun <T> requireEntityExists(entity: T?, entityType: String, id: String): T {
        return entity ?: throw NotFoundException("$entityType not found with id: $id")
    }
}
