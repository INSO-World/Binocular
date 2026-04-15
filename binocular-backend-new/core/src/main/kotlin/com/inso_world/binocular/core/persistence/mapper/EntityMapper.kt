package com.inso_world.binocular.core.persistence.mapper

import jakarta.validation.Valid

/**
 * Generic interface for bidirectional mapping between domain models and persistence-layer entities.
 *
 * This interface is a core component of the hexagonal architecture, enabling the infrastructure layer
 * to convert between rich domain models (from the `domain` module) and database-specific entities
 * (e.g., JPA entities, ArangoDB documents).
 *
 * ## Semantics
 * - Mappers are stateless and thread-safe
 * - All mapping operations validate their inputs and outputs using Jakarta Bean Validation
 * - Identity mapping: `toDomain(toEntity(domain))` should be logically equivalent to `domain`
 * - Mappers should handle circular references and graph structures correctly
 *
 * ## Invariants & Requirements
 * - Domain models (`D`) must be immutable or follow immutable semantics
 * - Entity types (`E`) are typically mutable JPA/ArangoDB entities with persistence annotations
 * - Implementations must preserve domain invariants during conversion
 * - Lazy-loaded relationships should be handled via [RelationshipProxyFactory] to avoid N+1 queries
 *
 * ## Trade-offs & Guidance
 * - **Performance**: Use [toDomainList] for batch conversions as it may leverage optimizations
 *   like [MappingContext] to track already-mapped objects and avoid duplicate work
 * - **Graph Mapping**: For complex object graphs with bidirectional relationships, use [MappingContext]
 *   within your mapper implementation to maintain object identity and prevent infinite loops
 * - **Lazy Loading**: When mapping relationships, prefer creating lazy proxies via [RelationshipProxyFactory]
 *   rather than eagerly loading entire object graphs
 *
 * @param D The domain model type (from the `domain` module)
 * @param E The database entity type (from an infrastructure module)
 *
 * ## Example
 * ```kotlin
 * class CommitMapper(
 *     private val proxyFactory: RelationshipProxyFactory,
 *     private val repositoryDao: RepositoryDao
 * ) : EntityMapper<Commit, CommitEntity> {
 *
 *     override fun toEntity(domain: Commit): CommitEntity =
 *         CommitEntity(
 *             id = domain.sha,
 *             message = domain.message,
 *             authorDate = domain.date
 *         )
 *
 *     override fun toDomain(entity: CommitEntity): Commit =
 *         Commit(
 *             sha = entity.id,
 *             message = entity.message,
 *             date = entity.authorDate,
 *             repository = proxyFactory.createLazyReference {
 *                 // Lazy-load repository when accessed
 *                 repositoryDao.findById(entity.repositoryId)
 *             }
 *         )
 * }
 * ```
 *
 * @see RelationshipProxyFactory
 * @see MappingContext
 */
interface EntityMapper<D, E> {
    /**
     * Converts a domain model to a database entity.
     *
     * This is typically used when persisting or updating domain objects. The resulting entity
     * should be suitable for persistence via a DAO or repository.
     *
     * ## Semantics
     * - Creates a new entity instance; does not mutate the input domain model
     * - May not populate relationship fields if they require database IDs not yet available
     * - Validates the domain model before conversion
     *
     * @param domain The domain model to convert (must be valid)
     * @return A validated database entity ready for persistence
     * @throws jakarta.validation.ValidationException if domain validation fails
     */
    fun toEntity(
        @Valid domain: D
    ): @Valid E

    /**
     * Converts a database entity to a domain model.
     *
     * This is typically used when loading data from the database. The resulting domain model
     * should be a complete, valid domain object that can be used by the application layer.
     *
     * ## Semantics
     * - Creates a new domain model instance; does not mutate the input entity
     * - Should create lazy proxies for relationships to avoid N+1 queries
     * - Validates the entity before conversion and the resulting domain model
     *
     * @param entity The database entity to convert (must be valid)
     * @return A validated domain model
     * @throws jakarta.validation.ValidationException if entity or domain validation fails
     */
    fun toDomain(
        @Valid entity: E,
    ): @Valid D

    /**
     * Converts multiple database entities to domain models in a single batch operation.
     *
     * This method may provide optimizations over calling [toDomain] individually, such as:
     * - Using [MappingContext] to track already-mapped objects and maintain identity
     * - Batching database queries for lazy-loaded relationships
     * - Reducing memory allocations for temporary objects
     *
     * The default implementation creates a fresh [MappingContext] and maps each entity sequentially.
     * Override this method if your mapper requires custom batch-loading logic.
     *
     * @param entities An iterable of database entities to convert
     * @return A list of validated domain models in the same order as the input
     * @throws jakarta.validation.ValidationException if any entity or domain validation fails
     */
    fun toDomainList(
        @Valid entities: Iterable<E>,
    ): @Valid List<D> = entities.map { toDomain(it) }
}
