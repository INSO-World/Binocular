package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.IssueInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.persistence.entity.IssueEntity
import com.inso_world.binocular.infrastructure.sql.persistence.dao.IssueDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.IssueLinkDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.NoteDao
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Milestone
import com.inso_world.binocular.model.Note
import com.inso_world.binocular.model.User
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Project
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import java.time.LocalDateTime

@Service
@Profile("postgres")
@Validated
internal class IssueInfrastructurePortImpl(
    private val issueDao: IssueDao,
    private val linkDao: IssueLinkDao,
    private val noteDao: NoteDao,
) :
    AbstractInfrastructurePort<Issue, IssueEntity, Long>(Long::class),
    IssueInfrastructurePort {

    override fun findAccountsByIssueId(issueId: String): List<Account> =
        linkDao.findAccountIdsByIssueId(issueId).map { Account(id = it) }

    override fun findCommitsByIssueId(issueId: String): List<Commit> =
        linkDao.findCommitIdsByIssueId(issueId).map { cid ->
            Commit(
                id = cid,
                sha = "0".repeat(40),
                commitDateTime = LocalDateTime.now(),
                repository = Repository(localPath = "unknown", project = Project(name = "unknown"))
            )
        }

    override fun findMilestonesByIssueId(issueId: String): List<Milestone> =
        linkDao.findMilestoneIdsByIssueId(issueId).map { Milestone(id = it) }

    override fun findNotesByIssueId(issueId: String): List<Note> =
        linkDao.findNoteIdsByIssueId(issueId)
            .mapNotNull { nid -> noteDao.findById(nid) }

    override fun findUsersByIssueId(issueId: String): List<User> =
        linkDao.findUserIdsByIssueId(issueId).map { User(id = it) }

    override fun findById(id: String): Issue? = issueDao.findById(id)

    override fun update(value: Issue): Issue = issueDao.update(value)

    override fun delete(value: Issue) {
        value.id?.let { deleteById(it) }
    }

    override fun updateAndFlush(value: Issue): Issue = update(value)

    override fun create(value: Issue): Issue = issueDao.create(value)

    override fun saveAll(values: Collection<Issue>): Iterable<Issue> = values.onEach { create(it) }

    override fun findAll(): Iterable<Issue> = issueDao.findAll()

    override fun findAll(pageable: Pageable): Page<Issue> {
        val total = issueDao.count()
        if (total == 0L) return Page(emptyList(), 0, pageable)
        val content = issueDao.findAll(pageable)
        return Page(content, total, pageable)
    }

    override fun deleteById(id: String) {
        linkDao.deleteLinksByIssueId(id)
        issueDao.deleteById(id)
    }

    override fun deleteAll() {
        linkDao.deleteAllLinks()
        issueDao.deleteAll()
    }
}
