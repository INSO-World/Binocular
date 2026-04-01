package com.inso_world.binocular.model

import java.util.concurrent.ConcurrentHashMap

/**
 * A mutable **add-only** `Set` for domain objects whose membership is keyed by a
 * stable `uniqueKey` (from [AbstractDomainObject]).
 *
 * ### What it does
 * - **Add-only:** Any form of removal (`remove`, `removeAll`, `retainAll`, `clear`, iterator `remove`)
 *   throws [UnsupportedOperationException]. Use this when edges/links must not be deleted
 *   via the collection API (e.g., history graphs).
 * - **Uniqueness by `uniqueKey`:** Two different instances with the same `uniqueKey`
 *   are treated as the *same* element. The first inserted instance becomes the
 *   **canonical** element stored in the set; later inserts with the same `uniqueKey`
 *   return `false` and do not replace the canonical instance.
 * - **Concurrent-friendly reads/writes:** Backed by a [ConcurrentHashMap]; `add` and
 *   `contains` are safe for concurrent use. The iterator is **weakly consistent**:
 *   it may reflect some—but not necessarily all—concurrent changes.
 *
 * ### Semantics & invariants
 * - **Membership test:** `contains(e)` checks presence by `e.uniqueKey`.
 * - **Set equality/hash semantics:** Inherits default [AbstractMutableSet] behavior.
 *   Equality compares element presence; iteration and `hashCode` derive from the
 *   **stored canonical instances**, not from the keys.
 * - **Iteration order:** Implementation-defined (map value iteration); do not rely on order.
 *
 * ### Performance
 * - `add` / `contains` / `containsAll` / `size` are expected **O(1)** average time.
 * - Memory overhead is one entry per distinct `uniqueKey`.
 *
 * ### Requirements / pitfalls
 * - `uniqueKey` **must be immutable and stable** for the lifetime of the element in the set.
 *   Mutating it after insertion breaks membership guarantees.
 * - If two different objects share a `uniqueKey`, only the *first* one added will be retained.
 *   The set will **not** swap the stored instance if a later, “newer” instance with the same key is added.
 * - This class does not synchronize multi-step operations (e.g., “check then add” outside `add`);
 *   coordinate externally if you need atomic higher-level workflows.
 *
 * ### Examples
 * ```kotlin
 * val set = NonRemovingMutableSet<MyEntity>()
 * val a1 = MyEntity(id = "A")         // uniqueKey == "A"
 * val a2 = MyEntity(id = "A")         // different instance, same key
 *
 * check(set.add(a1))                   // true — inserted, a1 is canonical
 * check(!set.add(a2))                  // false — same key, ignored; a1 stays canonical
 * check(a1 in set)                     // true
 * // set.remove(a1)                    // throws UnsupportedOperationException
 * ```
 */
open class NonRemovingMutableSet<T : AbstractDomainObject<*, *>>(
    protected val backing: ConcurrentHashMap<Any, AbstractDomainObject<*, *>> = ConcurrentHashMap()
) : AbstractMutableSet<T>() {

    override val size: Int get() = backing.size

    // allow reads and adds
    override fun contains(element: T): Boolean = backing.keys.contains(element.uniqueKey)
    override fun containsAll(elements: Collection<T>): Boolean = backing.keys.containsAll(elements.map { it.uniqueKey })
    override fun isEmpty(): Boolean = backing.isEmpty()
    override fun add(element: T): Boolean {
        val key = element.uniqueKey as Any
        if (backing.containsKey(key)) {
            return false
        } else {
            backing.computeIfAbsent(key) { element }
            return true
        }
    }

    // block any form of removal
    override fun clear(): Unit = fail()
    override fun remove(element: T): Boolean = fail()
    override fun removeAll(elements: Collection<T>): Boolean = fail()
    override fun retainAll(elements: Collection<T>): Boolean = fail()

    // iterator that cannot remove
    override fun iterator(): MutableIterator<T> {
        val it = backing.values.iterator()
        return object : MutableIterator<T> {
            override fun hasNext(): Boolean = it.hasNext()

            @Suppress("UNCHECKED_CAST")
            override fun next(): T = it.next() as T
            override fun remove(): Unit = fail()
        }
    }

    // nice string; equals/hashCode come from AbstractMutableSet (set semantics)
    override fun toString(): String = backing.values.toString()

    private fun <T> fail(): T = throw UnsupportedOperationException("Removing objects is not allowed.")
}

