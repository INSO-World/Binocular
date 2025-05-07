package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.persistence.dao.interfaces.IAccountDao
import com.inso_world.binocular.web.persistence.repository.arangodb.AccountRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class AccountDao(
  @Autowired private val accountRepository: AccountRepository
) : ArangoDbDao<Account, String>(), IAccountDao {

  init {
    this.setClazz(Account::class.java)
    this.setRepository(accountRepository)
  }
}
