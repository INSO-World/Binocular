package com.inso_world.binocular.infrastructure.arangodb.service

import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.IDao
import java.io.Serializable

internal abstract class AbstractInfrastructurePort<T : Any, I : Serializable> {
    internal lateinit var dao: IDao<T, I>
}
