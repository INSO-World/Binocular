package com.inso_world.binocular.core.persistence.mapper.context

import com.inso_world.binocular.model.AbstractDomainObject
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import org.springframework.util.ClassUtils
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
    private data class EntityObjectKey(
        val type: KClass<*>,
        val objectId: Int,
    )

    private data class DomainKey(
        val type: KClass<*>,
        val key: Any,
    )

    private data class EntityKey(
        val type: KClass<*>,
        val id: Any,
    )

    // ---------- Caches ----------
    private val d2e = ConcurrentHashMap<DomainKey, Any>()
    private val e2d = ConcurrentHashMap<EntityKey, Any>()

    // Fallback for unpersisted entities
    private val e2dByObjectIdentity = ConcurrentHashMap<EntityObjectKey, Any>()

    // ====================== Proxy handling =========================

    /**
     * Resolves the actual user class for an object, stripping CGLIB/Spring proxy wrappers.
     *
     * Spring's `@Ref(lazy = true)` wraps entities in CGLIB proxies whose `::class` differs
     * from the original entity class. This helper normalizes to the real class so that
     * cache keys remain consistent regardless of whether the entity is proxied or not.
     */
    private fun userClass(obj: Any): KClass<*> = ClassUtils.getUserClass(obj).kotlin

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
     * Uses (entityClass, entity.id) as the cache key.
     */
    @Suppress("UNCHECKED_CAST")
    open fun <D : Any, E : Any> findDomain(entity: E): D? {
        val entityClass = userClass(entity)

        // 1. Try using database id first
        resolveEntityId(entity)?.let { id ->
            (e2d[EntityKey(entityClass, id)] as? D)?.let { return it }
        }

        // 2. Fallback to object identity (entity may have been remembered before it had an id)
        val objKey = EntityObjectKey(entityClass, System.identityHashCode(entity))
        return e2dByObjectIdentity[objKey] as? D
    }

    // ========================= Remember ===========================

    /**
     * Remember the association between a domain object and an entity.
     * - Domain side: keyed by (domain class, business key K).
     * - Entity side: keyed by (entity class, technical id), if an id could be resolved.
     * First-write-wins.
     */
    open fun <K : Any, D : AbstractDomainObject<*, K>, E : Any> remember(
        domain: D,
        entity: E,
    ) {
        val entityClass = userClass(entity)

        // Always remember domain -> entity
        d2e.computeIfAbsent(DomainKey(domain::class, domain.uniqueKey)) { entity }
        // Try to remember entity -> domain using database id
        resolveEntityId(entity)?.let { id ->
            e2d.computeIfAbsent(EntityKey(entityClass, id)) { domain }
            // Entity may have been stored by identity before it had an id (pre-persistence);
            // promote it to the id-based map and clean up the stale identity entry.
            val objKey = EntityObjectKey(entityClass, System.identityHashCode(entity))
            e2dByObjectIdentity.remove(objKey)
        } ?: {
            // Fallback: use object identity for unpersisted entities
            val objKey = EntityObjectKey(entityClass, System.identityHashCode(entity))
            e2dByObjectIdentity.computeIfAbsent(objKey) { domain }
        }
        require(d2e.size == (e2d.size + e2dByObjectIdentity.size)) {
            "Context sizes do not match: ${d2e.size} != (${e2d.size} + ${e2dByObjectIdentity.size})"
        }
    }

    // ====================== Id resolution =========================

    private fun resolveEntityId(e: Any): Any? {
        readKProperty(e, "uniqueKey")?.let { return it }
        readKProperty(e, "iid")?.let { return it }

        // 1) Try Kotlin property named "id"
        readKProperty(e, "id")?.let { return it }

        // 2) Try Java field named "id"
        readField(e, "id")?.let { return it }

        // 3) Try property/field annotated with Id (jakarta/javax/spring-data)
        val idAnnotations =
            setOf(
                "jakarta.persistence.Id",
                "javax.persistence.Id",
                "org.springframework.data.annotation.Id",
            )

        // Kotlin properties with Id annotation
        userClass(e)
            .memberProperties
            .firstOrNull { p ->
                p.annotations.any { it.annotationClass.qualifiedName in idAnnotations }
            }?.let { p ->
                p.isAccessible = true
                return p.getter.call(e)
            }

        // Java fields with Id annotation
        ClassUtils
            .getUserClass(e)
            .declaredFields
            .firstOrNull { f ->
                f.annotations.any { it.annotationClass.qualifiedName in idAnnotations }
            }?.let { f ->
                f.isAccessible = true
                return f.get(e)
            }

        return null
    }

    private fun readKProperty(
        obj: Any,
        name: String,
    ): Any? =
        runCatching {
            val prop = userClass(obj).memberProperties.firstOrNull { it.name == name } ?: return null
            prop.isAccessible = true
            prop.getter.call(obj)
        }.getOrNull()

    private fun readField(
        obj: Any,
        name: String,
    ): Any? =
        runCatching {
            val field = ClassUtils.getUserClass(obj).declaredFields.firstOrNull { it.name == name } ?: return null
            field.isAccessible = true
            field.get(obj)
        }.getOrNull()
}
