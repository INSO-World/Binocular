package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IAccountDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.AccountEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.AccountMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.AccountRepository
import com.inso_world.binocular.model.Account
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of [IAccountDao].
 *
 * Overrides [create] to bypass [AccountMapper.toDomain]: that method requires the account
 * to already be registered in the [MappingContext] and throws otherwise, making the base
 * class round-trip (save → toDomain) impossible for standalone account saves. Instead,
 * after saving we write the ArangoDB-assigned id back onto the domain object and return it.
 */
@Repository
internal class AccountDao(
    @Autowired accountRepository: AccountRepository,
    @Autowired accountMapper: AccountMapper,
) : MappedArangoDbDao<Account, AccountEntity, String>(accountRepository, accountMapper),
    IAccountDao {
    /**
     * Persists an account and propagates the ArangoDB-assigned id back to the domain object.
     *
     * Skips the [AccountMapper.toDomain] round-trip because that mapper requires the account
     * to be pre-registered in the mapping context. The caller receives the same [entity]
     * instance with [Account.id] updated to the value assigned by the database.
     *
     * @param entity the account to persist
     * @return the same [entity] with its [Account.id] set
     */
    override fun create(entity: Account): Account {
        val savedEntity = repository.save(mapper.toEntity(entity))
        entity.id = savedEntity.id
        return entity
    }
}
