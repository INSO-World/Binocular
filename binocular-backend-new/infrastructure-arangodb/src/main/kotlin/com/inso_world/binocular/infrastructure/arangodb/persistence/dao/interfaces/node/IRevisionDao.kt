package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node

import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.IDao
import com.inso_world.binocular.model.Revision

/**
 * ArangoDB-specific interface for Revision DAO.
 */
internal interface IRevisionDao : IDao<Revision, String>
