package com.inso_world.binocular.infrastructure.sql.integration.service.base

import com.inso_world.binocular.infrastructure.sql.service.AbstractInfrastructurePort
import org.springframework.aop.framework.Advised
import org.springframework.transaction.annotation.Transactional
import java.io.Serializable

/**
 * Deletes all entities stored by this port.
 *
 * Unwraps a Spring AOP proxy (if the receiver is a proxy) before accessing
 * [AbstractInfrastructurePort.dao], because CGLIB proxies created by
 * `@MappingSession` skip constructor/init — the proxy's copy of the `lateinit`
 * field is never assigned on the proxy instance.
 */
@Suppress("UNCHECKED_CAST")
@Transactional
internal fun <D : Any, E : Any, I : Serializable> AbstractInfrastructurePort<D, E, I>.deleteAllEntities() {
    val target = (this as? Advised)?.targetSource?.target as? AbstractInfrastructurePort<D, E, I>
        ?: this
    target.dao.deleteAll()
}
