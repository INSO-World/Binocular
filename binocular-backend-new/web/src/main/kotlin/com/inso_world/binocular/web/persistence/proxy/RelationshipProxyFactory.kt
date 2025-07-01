package com.inso_world.binocular.web.persistence.proxy

/**
 * Interface for creating proxy objects that lazily load relationships.
 * This allows domain objects to have "virtual" relationships that are loaded
 * only when accessed, providing a consistent API regardless of the database implementation.
 */
interface RelationshipProxyFactory {
    /**
     * Creates a lazy-loaded list that only loads its contents when accessed.
     *
     * @param loader A function that loads the list contents when needed
     * @return A proxy list that delegates to the loaded list when accessed
     */
    fun <T> createLazyList(loader: () -> List<T>): List<T>

    /**
     * Creates a lazy-loaded list that only loads its contents when accessed,
     * and maps the loaded entities to domain objects using the provided mapper function.
     *
     * @param loader A function that loads the entity list contents when needed
     * @param mapper A function that maps an entity to a domain object
     * @return A proxy list that delegates to the loaded and mapped list when accessed
     */
    fun <E, D> createLazyMappedList(loader: () -> List<E>, mapper: (E) -> D): List<D>
}
