package com.inso_world.binocular.web.persistence.mapper

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
    fun toEntity(domain: D): E

    /**
     * Converts a database entity to a domain model
     */
    fun toDomain(entity: E): D

    /**
     * Converts a list of database entities to a list of domain models
     */
    fun toDomainList(entities: Iterable<E>): List<D> {
        return entities.map { toDomain(it) }
    }
}
