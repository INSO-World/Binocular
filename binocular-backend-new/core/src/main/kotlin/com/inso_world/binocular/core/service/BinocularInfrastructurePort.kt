package com.inso_world.binocular.core.service

import com.inso_world.binocular.core.persistence.model.Page
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

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
    @Transactional(readOnly = true)
    fun findAll(): Iterable<@Valid T>

    /**
     * Find all users with pagination.
     *
     * @param pageable Pagination information
     * @return Page of users
     */
    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable): Page<@Valid T>

    /**
     * Find a user by ID.
     *
     * @param id The ID of the user to find
     * @return The user if found, null otherwise
     */
    @Transactional(readOnly = true)
    fun findById(id: String): @Valid T?

    @Transactional
    fun create(
        @Valid value: T,
    ): @Valid T

    @Transactional
    fun update(
        @Valid value: T,
    ): @Valid T

    @Transactional
    fun updateAndFlush(
        @Valid value: T,
    ): @Valid T

    /**
     * Save multiple entities
     */
    @Transactional
    fun saveAll(
        @Valid values: Collection<@Valid T>,
    ): Iterable<@Valid T>

    @Transactional
    fun delete(
        @Valid value: T,
    )

    @Transactional
    fun deleteById(id: String)

    @Transactional
    fun deleteAll()
}
