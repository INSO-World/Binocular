package com.inso_world.binocular.core.persistence.mapper.context

import com.inso_world.binocular.model.AbstractDomainObject
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * Per-mapping-session, bidirectional identity map used while converting between *domain* objects
 * (D : AbstractDomainObject<*, K>) and *persistence entities* (E).
 *
 * ─ Domain ➜ Entity: keyed by (domainClass, domain.uniqueKey : K)
 * ─ Entity ➜ Domain: keyed by (entityClass, entity.id)
 *
 * This design prevents key collisions across different types that share the same id/value.
 * Both caches are thread-safe but intended to be short-lived (scoped per @MappingSession).
 *
 * Overwrite policy: first-write-wins.
 */
@Component
@Scope(value = "mapping", proxyMode = ScopedProxyMode.TARGET_CLASS)
open class MappingContext {

    // ---------- Keys ----------
    // For entities without ids, use object identity
    private data class EntityObjectKey(val type: KClass<*>, val objectId: Int)
    private data class DomainKey(val type: KClass<*>, val key: Any)
    private data class EntityKey(val type: KClass<*>, val id: Any)

    // ---------- Caches ----------
    private val d2e = ConcurrentHashMap<DomainKey, Any>()
    private val e2d = ConcurrentHashMap<EntityKey, Any>()

    // Fallback for unpersisted entities
    private val e2dByObjectIdentity = ConcurrentHashMap<EntityObjectKey, Any>()

    // ====================== Domain -> Entity ======================

    /**
     * Returns the previously remembered entity for this domain object, if any.
     * Uses (domain::class, domain.uniqueKey) as the cache key.
     */
    @Suppress("UNCHECKED_CAST")
    open fun <K : Any, D : AbstractDomainObject<*, K>, E : Any> findEntity(domain: D): E? =
        d2e[DomainKey(domain::class, domain.uniqueKey)] as? E

    // ====================== Entity -> Domain ======================

    /**
     * Returns the previously remembered domain object for this entity, if any.
     * Uses (entity::class, entity.id) as the cache key.
     */
    @Suppress("UNCHECKED_CAST")
    open fun <D : Any, E : Any> findDomain(entity: E): D? {
        // 1. Try using database id first
        resolveEntityId(entity)?.let { id ->
            return e2d[EntityKey(entity::class, id)] as? D
        }

        // 2. Fallback to object identity for unpersisted entities
        val objKey = EntityObjectKey(entity::class, System.identityHashCode(entity))
        return e2dByObjectIdentity[objKey] as? D
    }

    // ========================= Remember ===========================

    /**
     * Remember the association between a domain object and an entity.
     * - Domain side: keyed by (domain class, business key K).
     * - Entity side: keyed by (entity class, technical id), if an id could be resolved.
     * First-write-wins.
     */
    open fun <K : Any, D : AbstractDomainObject<*, K>, E : Any> remember(domain: D, entity: E) {
        // Always remember domain -> entity
        d2e.computeIfAbsent(DomainKey(domain::class, domain.uniqueKey)) { entity }
        // Try to remember entity -> domain using database id
        resolveEntityId(entity)?.let { id ->
            e2d.computeIfAbsent(EntityKey(entity::class, id)) { domain }
        } ?: run {
            // Fallback: use object identity for unpersisted entities
            val objKey = EntityObjectKey(entity::class, System.identityHashCode(entity))
            e2dByObjectIdentity.computeIfAbsent(objKey) { domain }
        }
        require(d2e.size == (e2d.size + e2dByObjectIdentity.size)) {
            "Context sizes do not match: ${d2e.size} != (${e2d.size} + ${e2dByObjectIdentity.size})"
        }
    }

    // ====================== Id resolution =========================

    private fun resolveEntityId(e: Any): Any? {
        readKProperty(e, "uniqueKey")?.let { return it }

        // 1) Try Kotlin property named "id"
        readKProperty(e, "id")?.let { return it }

        // 2) Try Java field named "id"
        readField(e, "id")?.let { return it }

        // 3) Try property/field annotated with Id (jakarta/javax/spring-data)
        val idAnnotations = setOf(
            "jakarta.persistence.Id", "javax.persistence.Id", "org.springframework.data.annotation.Id"
        )

        // Kotlin properties with Id annotation
        e::class.memberProperties.firstOrNull { p ->
            p.annotations.any { it.annotationClass.qualifiedName in idAnnotations }
        }?.let { p ->
            p.isAccessible = true
            return p.getter.call(e)
        }

        // Java fields with Id annotation
        e.javaClass.declaredFields.firstOrNull { f ->
            f.annotations.any { it.annotationClass.qualifiedName in idAnnotations }
        }?.let { f ->
            f.isAccessible = true
            return f.get(e)
        }

        return null
    }

    private fun readKProperty(obj: Any, name: String): Any? = runCatching {
        val prop = obj::class.memberProperties.firstOrNull { it.name == name } ?: return null
        prop.isAccessible = true
        prop.getter.call(obj)
    }.getOrNull()

    private fun readField(obj: Any, name: String): Any? = runCatching {
        val field = obj.javaClass.declaredFields.firstOrNull { it.name == name } ?: return null
        field.isAccessible = true
        field.get(obj)
    }.getOrNull()
}

