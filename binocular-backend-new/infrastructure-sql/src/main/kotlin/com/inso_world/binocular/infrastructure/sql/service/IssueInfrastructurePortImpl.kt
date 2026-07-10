package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.core.persistence.mapper.context.MappingSession
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.IssueInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.mapper.IssueMapper
import com.inso_world.binocular.infrastructure.sql.persistence.dao.IssueDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.IssueLinkDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.NoteDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.ProjectDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.IssueEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.toEntity
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Milestone
import com.inso_world.binocular.model.Note
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.User
import com.inso_world.binocular.model.enums.IssueAccountRole
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

@Service
@Profile("postgres")
@Validated
internal class IssueInfrastructurePortImpl
    @Autowired
    constructor(
        private val projectDao: ProjectDao,
        private val issueDao: IssueDao,
        private val issueMapper: IssueMapper,
        private val linkDao: IssueLinkDao,
        private val noteDao: NoteDao,
    ) : AbstractInfrastructurePort<Issue, IssueEntity, Long>(Long::class),
        IssueInfrastructurePort {
        @Autowired
        private lateinit var ctx: MappingContext

        override fun findAccountsByIssueId(issueId: String): List<Account> {
            TODO("Not yet implemented")
        }
//        linkDao.findAccountIdsByIssueId(issueId).map { Account(id = it) }

        override fun findCommitsByIssueId(issueId: String): List<Commit> {
            TODO("Not yet implemented")
        }
//        linkDao.findCommitIdsByIssueId(issueId).map { cid ->
//            Commit(
//                id = cid,
//                sha = "0".repeat(40),
//                commitDateTime = LocalDateTime.now(),
//                repository = Repository(localPath = "unknown", project = Project(name = "unknown"))
//            )
//        }

        override fun findAccountsByIssueId(
            issueId: String,
            role: IssueAccountRole
        ): List<Account> {
            TODO("Not yet implemented")
        }
//    =
//         TODO: role filter
//        findAccountsByIssueId(issueId)

        override fun findMilestonesByIssueId(issueId: String): List<Milestone> {
            TODO("Not yet implemented")
        }
//        linkDao.findMilestoneIdsByIssueId(issueId).map { Milestone(id = it) }

        override fun findNotesByIssueId(issueId: String): List<Note> {
            TODO("Not yet implemented")
        }
//        linkDao.findNoteIdsByIssueId(issueId)
//            .mapNotNull { nid -> noteDao.findById(nid) }

        override fun findUsersByIssueId(issueId: String): List<User> {
            TODO("Not yet implemented")
        }
//        linkDao.findUserIdsByIssueId(issueId).map { User(id = it) }

        @MappingSession
        override fun findAll(): Iterable<Issue> =
            super.findAllEntities().map {
                issueMapper.toDomain(it, it.project.toDomain())
            }

        @MappingSession
        override fun findByIid(iid: Issue.Id): Issue? {
            return null // TODO
        }

        @Transactional(readOnly = true)
        @MappingSession
        override fun findExistingGid(
            ids: List<String>,
            project: Project
        ): Iterable<Issue> =
            this.issueDao
                .findExistingGid(project, ids)
                .map {
                    this.issueMapper.toDomain(it, project)
                }

        override fun findById(id: String): Issue? {
            return null // TODO
        }

        override fun update(value: Issue): Issue {
            val owner =
                projectDao.findByIid(value.project)
                    ?: throw IllegalStateException("Project not found")
            return issueMapper.toDomain(issueDao.update(value.toEntity(owner)), owner.toDomain())
        }

        override fun create(value: Issue): Issue {
            TODO("Not yet implemented")
        }

        override fun saveAll(values: Collection<Issue>): Iterable<Issue> {
            TODO("Not yet implemented")
        }

        override fun delete(value: Issue) {
            value.id?.let { deleteById(it) }
        }

        override fun findAll(pageable: Pageable): Page<Issue> {
            val page = super.findAllEntities(pageable)
            val total = page.totalElements
            if (total == 0L) return Page(emptyList(), 0, pageable)
            val content = page.content.map { issueMapper.toDomain(it, it.project.toDomain()) }
            return Page(content, total, pageable)
        }

        override fun findAll(
            pageable: Pageable,
            since: Long?,
            until: Long?
        ): Page<Issue> {
            TODO("Not yet implemented")
        }

        override fun deleteById(id: String) {
            linkDao.deleteLinksByIssueId(id)
            issueDao.deleteById(id.toLong())
        }

        override fun deleteAll() {
            linkDao.deleteAllLinks()
            issueDao.deleteAll()
        }
    }
