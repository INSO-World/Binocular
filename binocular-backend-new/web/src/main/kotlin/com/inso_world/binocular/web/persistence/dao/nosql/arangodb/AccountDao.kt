package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.persistence.dao.interfaces.IAccountDao
import com.inso_world.binocular.web.persistence.entity.arangodb.AccountEntity
import com.inso_world.binocular.web.persistence.mapper.arangodb.AccountMapper
import com.inso_world.binocular.web.persistence.repository.arangodb.AccountRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
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
@Profile("nosql", "arangodb")
class AccountDao(
  @Autowired accountRepository: AccountRepository,
  @Autowired accountMapper: AccountMapper
) : MappedArangoDbDao<Account, AccountEntity, String>(accountRepository, accountMapper), IAccountDao {
}
