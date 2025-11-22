package com.inso_world.binocular.core.service

import com.inso_world.binocular.core.persistence.model.Page
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable

/**
 * Interface for BinocularService.
 * Provides methods to retrieve elements of type `T` and their related entities.
 */
interface BinocularInfrastructurePort<T> {
    /**
     * Find all users with pagination.
     *
     * @return Page of users
     */
    fun findAll(): Iterable<@Valid T>

    /**
     * Find all users with pagination.
     *
     * @param pageable Pagination information
     * @return Page of users
     */
    fun findAll(pageable: Pageable): Page<@Valid T>

    /**
     * Find a user by ID.
     *
     * @param id The ID of the user to find
     * @return The user if found, null otherwise
     */
    fun findById(id: String): @Valid T?

    fun create(
        @Valid value: T,
    ): @Valid T

    fun update(
        @Valid value: T,
    ): @Valid T

    /**
     * Save multiple entities
     */
    fun saveAll(
        @Valid values: Collection<@Valid T>,
    ): Iterable<@Valid T>

    fun delete(
        @Valid value: T,
    ) {
        throw UnsupportedOperationException("DELETE operations are not yet supported")
    }

    fun deleteById(id: String) {
        throw UnsupportedOperationException("DELETE operations are not yet supported")
    }

    fun deleteAll() {
        throw UnsupportedOperationException("DELETE operations are not yet supported")
    }
}
