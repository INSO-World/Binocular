package com.inso_world.binocular.core.extension

import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.model.NonRemovingMutableSet
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

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

fun MappingContext.reset() {

    for (prop in arrayOf("d2e", "e2d", "e2dByObjectIdentity")) {
        val field = this::class.memberProperties
            .first { property -> property.name == prop }
            .apply { isAccessible = true }
            .getter.call(this)
        (field as ConcurrentHashMap<*, *>).clear()
    }
}
