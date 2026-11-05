package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node

import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.IDao
import com.inso_world.binocular.model.Developer

/**
 * ArangoDB-specific interface for Developer DAO.
 */
internal interface IDeveloperDao : IDao<Developer, String>
