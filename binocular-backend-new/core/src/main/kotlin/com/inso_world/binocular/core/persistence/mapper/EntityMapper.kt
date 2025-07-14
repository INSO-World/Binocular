package com.inso_world.binocular.core.persistence.mapper

import jakarta.validation.Valid

/**
 * Generic interface for mapping between domain models and database entities.
 *
 * @param D The domain model type
 * @param E The database entity type
 */
interface EntityMapper<D, E> {
    /**
     * Converts a domain model to a database entity
     */
    fun toEntity(
        @Valid domain: D,
    ): @Valid E

    /**
     * Converts a database entity to a domain model
     */
    fun toDomain(
        @Valid entity: E,
    ): @Valid D

    /**
     * Converts a list of database entities to a list of domain models
     */
    fun toDomainList(
        @Valid entities: Iterable<E>,
    ): @Valid List<D> = entities.map { toDomain(it) }
}
