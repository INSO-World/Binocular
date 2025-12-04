package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.persistence.mapper.context.MappingSession
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.IssueInfrastructurePort
import com.inso_world.binocular.core.service.exception.NotFoundException
import com.inso_world.binocular.infrastructure.sql.mapper.IssueMapper
import com.inso_world.binocular.infrastructure.sql.persistence.dao.IssueDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.ProjectDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.IssueEntity
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Milestone
import com.inso_world.binocular.model.Note
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

@Service
@Validated
internal class IssueInfrastructurePortImpl
    @Autowired constructor(
        private val projectDao: ProjectDao,
        private val issueDao: IssueDao,
        private val issueMapper: IssueMapper,
    ) :
    AbstractInfrastructurePort<Issue, IssueEntity, Long>(Long::class),
    IssueInfrastructurePort {
    override fun findAccountsByIssueId(issueId: String): List<Account> {
        TODO("Not yet implemented")
    }

    override fun findCommitsByIssueId(issueId: String): List<Commit> {
        TODO("Not yet implemented")
    }

    override fun findMilestonesByIssueId(issueId: String): List<Milestone> {
        TODO("Not yet implemented")
    }

    override fun findNotesByIssueId(issueId: String): List<Note> {
        TODO("Not yet implemented")
    }

    override fun findUsersByIssueId(issueId: String): List<User> {
        TODO("Not yet implemented")
    }

    @Transactional(readOnly = true)
    @MappingSession
    override fun findExistingGid(
        ids: List<String>,
        project: Project
    ): Iterable<Issue> {
        val projectEntity = projectDao.findByName(project.name)
            ?: throw NotFoundException("Project ${project.name} not found")

        val projectModel = projectEntity.toDomain()

        return issueDao.findExistingGid(projectEntity, ids)
            .map {
                issueMapper.toDomain(it).also { issue ->
                    projectModel.issues.add(issue)
                }
            }
    }

    override fun findById(id: String): Issue? {
        TODO("Not yet implemented")
    }

    override fun update(value: Issue): Issue {
        TODO("Not yet implemented")
    }

    override fun create(value: Issue): Issue {
        TODO("Not yet implemented")
    }

    override fun saveAll(values: Collection<Issue>): Iterable<Issue> {
        TODO("Not yet implemented")
    }

    override fun findAll(): Iterable<Issue> {
        TODO("Not yet implemented")
    }

    override fun findAll(pageable: Pageable): Page<Issue> {
        TODO("Not yet implemented")
    }
}
