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
    fun findAll(): Iterable<T>

    /**
     * Find all users with pagination.
     *
     * @param pageable Pagination information
     * @return Page of users
     */
    fun findAll(pageable: Pageable): Page<T>

    /**
     * Find a user by ID.
     *
     * @param id The ID of the user to find
     * @return The user if found, null otherwise
     */
    fun findById(id: String): T?

    fun save(
        @Valid entity: T,
    ): T

    /**
     * Save multiple entities
     */
    fun saveAll(
        @Valid entities: Collection<T>,
    ): Iterable<T>
}
