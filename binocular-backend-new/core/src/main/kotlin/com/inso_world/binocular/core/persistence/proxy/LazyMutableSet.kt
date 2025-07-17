package com.inso_world.binocular.core.persistence.proxy

class LazyMutableSet<T>(
    private val loader: () -> Iterable<T>,
    private val postProcessing: (LazyMutableSet<T>) -> Unit,
) : MutableSet<T> {
    @Volatile
    private var initialized = false
    private lateinit var target: MutableSet<T>

    /**
     * Initializes the list by calling the loader function if not already initialized.
     * This method is synchronized to ensure the loader is called only once.
     */
    private fun initialize() {
        if (!initialized) {
            synchronized(this) {
                if (!initialized) {
                    target = loader().toMutableSet()
                    initialized = true
                    postProcessing(this)
                }
            }
        }
    }

    override fun contains(element: T): Boolean {
        initialize()
        return target.contains(element)
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        initialize()
        return target.containsAll(elements)
    }

    override fun isEmpty(): Boolean {
        initialize()
        return target.isEmpty()
    }

    override fun iterator(): MutableIterator<T> {
        initialize()
        return target.iterator()
    }

    override fun add(element: T): Boolean {
        initialize()
        return target.add(element)
    }

    override fun remove(element: T): Boolean {
        initialize()
        return target.remove(element)
    }

    override fun addAll(elements: Collection<T>): Boolean {
        initialize()
        return target.addAll(elements)
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        initialize()
        return target.removeAll(elements)
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        initialize()
        return target.retainAll(elements)
    }

    override fun clear() {
        initialize()
        target.clear()
    }

    override val size: Int
        get() {
            initialize()
            return target.size
        }
}
