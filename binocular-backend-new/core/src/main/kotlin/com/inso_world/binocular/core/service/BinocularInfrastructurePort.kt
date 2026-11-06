package com.inso_world.binocular.core.service

import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.model.AbstractDomainObject
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable

/**
 * Base infrastructure port interface for all domain aggregate repositories in Binocular.
 *
 * This interface defines the fundamental CRUD operations for persisting and retrieving domain aggregates
 * following the hexagonal architecture pattern. Implementations are responsible for mapping between
 * domain models and persistence-layer entities (e.g., SQL, ArangoDB, RDF).
 *
 * ## Semantics
 * - All operations validate entities using Jakarta Bean Validation (`@Valid`)
 * - Operations returning nullable types return `null` when the entity is not found
 * - All operations are expected to be transactional within the infrastructure layer
 *
 * ## Invariants & Requirements
 * - Type parameter `T` must be a valid domain model from the `domain` module
 * - All input values must pass validation constraints defined in the domain model
 * - Implementations must ensure referential integrity within the persistence layer
 * - DELETE operations are not yet supported and throw `UnsupportedOperationException`
 *
 * ## Trade-offs & Guidance
 * - **Repository vs Aggregate Ports**: For aggregate root operations (e.g., Repository, Project),
 *   prefer using the specific aggregate port (e.g., `RepositoryInfrastructurePort`) over individual
 *   entity ports, as aggregate ports provide operations in the context of the full aggregate boundary
 * - **Pagination**: Use `findAll(Pageable)` for large datasets to avoid memory issues
 * - **Batch Operations**: Use `saveAll()` for bulk inserts to reduce database round-trips
 *
 * @param T The domain model type this port manages
 *
 * ## Example
 * ```kotlin
 * // Implementing a new infrastructure port
 * interface UserInfrastructurePort : BinocularInfrastructurePort<User> {
 *     // Add domain-specific queries here
 *     fun findByEmail(email: String): User?
 * }
 * ```
 *
 * @see RepositoryInfrastructurePort
 * @see ProjectInfrastructurePort
 */
interface BinocularInfrastructurePort<T : AbstractDomainObject<Iid, *>, Iid> {
    /**
     * Retrieves all entities of type [T].
     *
     * **Warning**: This operation loads all entities into memory and should only be used for
     * small datasets. For large datasets, prefer [findAll] with pagination.
     *
     * @return An iterable of all validated entities
     */
    fun findAll(): Iterable<@Valid T>

    /**
     * Retrieves a paginated subset of entities of type [T].
     *
     * This is the recommended way to retrieve large datasets as it limits memory usage
     * and provides efficient database queries.
     *
     * @param pageable Pagination information including page number, size, and sort order
     * @return A page containing the requested entities and pagination metadata
     */
    fun findAll(pageable: Pageable): Page<@Valid T>

    /**
     * Finds a single entity by its unique identifier.
     *
     * @param id The unique identifier of the entity (typically a UUID or composite key string)
     * @return The validated entity if found, `null` otherwise
     */
    @Deprecated("", ReplaceWith("findByIid(iid)"))
    fun findById(id: String): @Valid T? = throw UnsupportedOperationException("use findByIid(...) instead")

    /**
     * Finds a single entity by its technical/aggregate identifier [AbstractDomainObject.iid].
     *
     * ## Implementation Note - Kotlin Value Classes
     * When [Iid] is a Kotlin value class (e.g., `Repository.Id`), implementations require special
     * handling due to JVM name mangling. The mangled method signature prevents Spring AOP's
     * `@annotation` pointcut from matching the `@MappingSession` annotation.
     *
     * **Required Workaround**: Implementations must use the self-injection pattern:
     * 1. Inject `self` reference to the proxied bean instance
     * 2. Override this method with `@JvmName` to declare the mangled signature (optional)
     * 3. Delegate to a non-private (e.g. protected) internal method (e.g., `findByIidInternal`) via `self`
     * 4. Apply `@MappingSession` to the internal method where Spring AOP can intercept it
     *
     * This ensures the mapping session scope is properly established before calling DAOs/assemblers.
     *
     * @param iid The technical/aggregate identifier of the entity (may be a value class)
     * @return The validated entity if found, `null` otherwise
     * @see <a href="https://youtrack.jetbrains.com/issue/KT-31420">KT-31420</a>
     */
    fun findByIid(iid: Iid): @Valid T?

    /**
     * Persists a new entity to the database.
     *
     * ## Semantics
     * - The entity must not already exist (no ID or ID not yet persisted)
     * - Generates and assigns a unique ID if not provided
     * - Validates the entity before persisting
     *
     * @param value The domain model to persist (must be valid according to Jakarta validation)
     * @return The persisted entity with generated ID and any database-generated values
     * @throws jakarta.validation.ValidationException if the entity fails validation
     */
    fun create(
        @Valid value: T,
    ): @Valid T

    /**
     * Updates an existing entity in the database.
     *
     * ## Semantics
     * - The entity must already exist (must have a valid ID)
     * - Updates all fields of the entity
     * - Validates the entity before updating
     *
     * @param value The domain model to update (must be valid according to Jakarta validation)
     * @return The updated entity
     * @throws jakarta.validation.ValidationException if the entity fails validation
     * @throws com.inso_world.binocular.core.service.exception.NotFoundException if entity doesn't exist
     */
    fun update(
        @Valid value: T,
    ): @Valid T

    /**
     * Persists or updates multiple entities in a single batch operation.
     *
     * This is more efficient than calling [create] or [update] individually as it reduces
     * database round-trips. Implementations should use batch insert/update capabilities
     * of the underlying database.
     *
     * @param values Collection of domain models to persist (all must be valid)
     * @return An iterable of the persisted/updated entities
     * @throws jakarta.validation.ValidationException if any entity fails validation
     */
    fun saveAll(
        @Valid values: Collection<@Valid T>,
    ): Iterable<@Valid T>

    /**
     * Deletes an entity from the database.
     *
     * **Status**: Not yet implemented
     *
     * @param value The entity to delete
     * @throws UnsupportedOperationException Always thrown until DELETE operations are implemented
     */
    fun delete(
        @Valid value: T,
    ) {
        throw UnsupportedOperationException("DELETE operations are not yet supported")
    }

    /**
     * Deletes an entity by its unique identifier.
     *
     * **Status**: Not yet implemented
     *
     * @param id The unique identifier of the entity to delete
     * @throws UnsupportedOperationException Always thrown until DELETE operations are implemented
     */
    fun deleteById(id: String) {
        throw UnsupportedOperationException("DELETE operations are not yet supported")
    }

    /**
     * Deletes all entities of type [T] from the database.
     *
     * **Status**: Not yet implemented
     *
     * @throws UnsupportedOperationException Always thrown until DELETE operations are implemented
     */
    fun deleteAll() {
        throw UnsupportedOperationException("DELETE operations are not yet supported")
    }
}
