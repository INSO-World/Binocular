package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node

import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.IDao
import com.inso_world.binocular.model.vcs.Remote

/**
 * ArangoDB-specific interface for Remote DAO.
 */
internal interface IRemoteDao : IDao<Remote, String>
