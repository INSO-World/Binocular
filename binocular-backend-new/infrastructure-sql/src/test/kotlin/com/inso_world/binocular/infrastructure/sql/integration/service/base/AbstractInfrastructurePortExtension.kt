package com.inso_world.binocular.infrastructure.sql.integration.service.base

import com.inso_world.binocular.infrastructure.sql.service.AbstractInfrastructurePort
import org.springframework.transaction.annotation.Transactional
import java.io.Serializable

@Transactional
internal fun <D : Any, E : Any, I : Serializable> AbstractInfrastructurePort<D, E, I>.deleteAllEntities() {
    this.dao.deleteAll()
}
