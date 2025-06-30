package com.inso_world.binocular.web.persistence.proxy

import org.springframework.stereotype.Component
import java.util.AbstractList

/**
 * Implementation of RelationshipProxyFactory that creates lazy-loaded list proxies.
 * This component is used to create proxy objects for relationships in domain models,
 * allowing them to be loaded only when accessed.
 */
@Component
class RelationshipProxyFactoryImpl : RelationshipProxyFactory {

    /**
     * Creates a lazy-loaded list that only loads its contents when accessed.
     *
     * @param loader A function that loads the list contents when needed
     * @return A proxy list that delegates to the loaded list when accessed
     */
    override fun <T> createLazyList(loader: () -> List<T>): List<T> {
        return LazyList(loader)
    }

    /**
     * Creates a lazy-loaded list that only loads its contents when accessed,
     * and maps the loaded entities to domain objects using the provided mapper function.
     *
     * @param loader A function that loads the entity list contents when needed
     * @param mapper A function that maps an entity to a domain object
     * @return A proxy list that delegates to the loaded and mapped list when accessed
     */
    override fun <E, D> createLazyMappedList(loader: () -> List<E>, mapper: (E) -> D): List<D> {
        return LazyList { loader().map(mapper) }
    }

    /**
     * A list implementation that lazily loads its contents only when accessed.
     * This class is thread-safe and ensures the loader function is called only once.
     *
     * @param loader A function that loads the list contents when needed
     */
    private class LazyList<T>(private val loader: () -> List<T>) : AbstractList<T>() {
        @Volatile
        private var initialized = false
        private lateinit var target: List<T>

        /**
         * Initializes the list by calling the loader function if not already initialized.
         * This method is synchronized to ensure the loader is called only once.
         */
        private fun initialize() {
            if (!initialized) {
                synchronized(this) {
                    if (!initialized) {
                        target = loader()
                        initialized = true
                    }
                }
            }
        }

        /**
         * Returns the size of the list, initializing it if necessary.
         */
        override val size: Int
            get() {
                initialize()
                return target.size
            }

        /**
         * Returns the element at the specified index, initializing the list if necessary.
         */
        override fun get(index: Int): T {
            initialize()
            return target[index]
        }

        /**
         * Returns whether the list contains the specified element, initializing the list if necessary.
         */
        override fun contains(element: T): Boolean {
            initialize()
            return target.contains(element)
        }

        /**
         * Returns whether the list is empty, initializing it if necessary.
         */
        override fun isEmpty(): Boolean {
            initialize()
            return target.isEmpty()
        }

        /**
         * Returns an iterator over the elements in the list, initializing it if necessary.
         */
        override fun iterator(): MutableIterator<T> {
            initialize()
            return if (target is MutableList) {
                (target as MutableList<T>).iterator()
            } else {
                // Wrap the immutable iterator in a mutable iterator that throws on modification
                object : MutableIterator<T> {
                    private val wrapped = target.iterator()

                    override fun hasNext(): Boolean = wrapped.hasNext()
                    override fun next(): T = wrapped.next()
                    override fun remove() {
                        throw UnsupportedOperationException("Cannot modify a read-only list")
                    }
                }
            }
        }
    }
}
