package com.inso_world.binocular.infrastructure.arangodb.service

import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.AccountInfrastructurePort
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.IIssueAccountConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.IMergeRequestAccountConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.INoteAccountConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IAccountDao
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.MergeRequest
import com.inso_world.binocular.model.Note
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
internal class AccountInfrastructurePortImpl : AccountInfrastructurePort {
    @Autowired
    private lateinit var accountDao: IAccountDao

    @Autowired
    private lateinit var issueAccountConnectionRepository: IIssueAccountConnectionDao

    @Autowired
    private lateinit var mergeRequestAccountConnectionRepository: IMergeRequestAccountConnectionDao

    @Autowired
    private lateinit var noteAccountConnectionRepository: INoteAccountConnectionDao
    var logger: Logger = LoggerFactory.getLogger(AccountInfrastructurePortImpl::class.java)

    override fun findAll(pageable: Pageable): Page<Account> {
        logger.trace("Getting all accounts with pageable: page=${pageable.pageNumber}, size=${pageable.pageSize}")
        return accountDao.findAll(pageable)
    }

    override fun findById(id: String): Account? {
        logger.trace("Getting account by id: $id")
        return accountDao.findById(id)
    }

    override fun findByIid(iid: Account.Id): @Valid Account? {
        TODO("Not yet implemented")
    }

    override fun findIssuesByAccountId(accountId: String): List<Issue> {
        logger.trace("Getting issues for account: $accountId")
        return issueAccountConnectionRepository.findIssuesByAccount(accountId)
    }

    override fun findMergeRequestsByAccountId(accountId: String): List<MergeRequest> {
        logger.trace("Getting merge requests for account: $accountId")
        return mergeRequestAccountConnectionRepository.findMergeRequestsByAccount(accountId)
    }

    override fun findNotesByAccountId(accountId: String): List<Note> {
        logger.trace("Getting notes for account: $accountId")
        return noteAccountConnectionRepository.findNotesByAccount(accountId)
    }

    override fun findAll(): Iterable<Account> = accountDao.findAll()

    override fun create(entity: Account): Account = this.accountDao.save(entity)

    override fun saveAll(entities: Collection<Account>): Iterable<Account> = this.accountDao.saveAll(entities)

    override fun delete(entity: Account) {
        this.accountDao.delete(entity)
    }

    override fun update(entity: Account): Account = this.accountDao.update(entity)

    override fun deleteById(id: String) {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        this.accountDao.deleteAll()
    }
}
