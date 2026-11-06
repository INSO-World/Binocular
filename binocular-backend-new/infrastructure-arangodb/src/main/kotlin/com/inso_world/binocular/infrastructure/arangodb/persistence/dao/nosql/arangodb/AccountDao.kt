package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IAccountDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.AccountEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.AccountMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.AccountRepository
import com.inso_world.binocular.model.Account
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of IAccountDao using the MappedArangoDbDao approach.
 *
 * This class extends MappedArangoDbDao to leverage the entity mapping pattern,
 * which provides a clean separation between domain models (Account) and
 * database-specific entities (AccountEntity).
 *
 * This is an example of the second approach described in ArangoDbDao:
 * - Using entity mapping with a separate mapper class
 * - Extending MappedArangoDbDao instead of ArangoDbDao
 * - Implementing the specific interface (IAccountDao)
 */
@Repository
internal class AccountDao(
    @Autowired accountRepository: AccountRepository,
    @Autowired accountMapper: AccountMapper,
) : MappedArangoDbDao<Account, AccountEntity, String>(accountRepository, accountMapper),
    IAccountDao
