package com.inso_world.binocular.core.persistence.proxy

/**
 * Interface for creating proxy objects that lazily load relationships.
 * This allows domain objects to have "virtual" relationships that are loaded
 * only when accessed, providing a consistent API regardless of the database implementation.
 */
interface LazyReference<T : Any> {
    fun get(): T
}

interface RelationshipProxyFactory {
    /**
     * Creates a lazy-loaded list that only loads its contents when accessed.
     *
     * @param loader A function that loads the list contents when needed
     * @return A proxy list that delegates to the loaded list when accessed
     */
    fun <T> createLazyList(loader: () -> List<T>): List<T>

    fun <T> createLazySet(loader: () -> MutableSet<T>): MutableSet<T>

    /**
     * Creates a lazy-loaded list that only loads its contents when accessed,
     * and maps the loaded entities to domain objects using the provided mapper function.
     *
     * @param loader A function that loads the entity list contents when needed
     * @param mapper A function that maps an entity to a domain object
     * @return A proxy list that delegates to the loaded and mapped list when accessed
     */
    fun <E, D> createLazyMappedList(
        loader: () -> List<E>,
        mapper: (E) -> D,
    ): List<D>

    /**
     * Creates a lazy-loaded reference that only loads its value when accessed.
     *
     * @param loader A function that loads the value when needed
     * @return A proxy object that delegates to the loaded value when accessed
     */
    fun <T : Any> createLazyReference(loader: () -> T): LazyReference<T>
}
