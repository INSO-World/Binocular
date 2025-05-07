package com.inso_world.binocular.web.graphql.error

import com.inso_world.binocular.web.exception.NotFoundException
import com.inso_world.binocular.web.exception.ValidationException

/**
 * Utility class for GraphQL validation and precondition checking.
 */
object GraphQLValidationUtils {
    /**
     * Validates pagination parameters.
     *
     * @param page The page number (1-based)
     * @param perPage The number of items per page
     * @throws ValidationException if the parameters are invalid
     */
    fun validatePagination(page: Int?, perPage: Int?) {
        if (page != null && page < 1) {
            throw ValidationException("Page must be greater than or equal to 1", mapOf("parameter" to "page"))
        }

        if (perPage != null && perPage < 1) {
            throw ValidationException("PerPage must be greater than or equal to 1", mapOf("parameter" to "perPage"))
        }
    }

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
