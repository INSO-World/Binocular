package com.inso_world.binocular.core.extension

import com.inso_world.binocular.model.NonRemovingMutableSet

/**
 * Test-only extension to clear the internal backing storage of [NonRemovingMutableSet].
 *
 * Uses reflection because `backing` is not publicly accessible.
 */
fun NonRemovingMutableSet<*>.reset() {
    val field = NonRemovingMutableSet::class.java.getDeclaredField("backing")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    val map = field.get(this) as MutableMap<Any, Any>
    map.clear()
}
