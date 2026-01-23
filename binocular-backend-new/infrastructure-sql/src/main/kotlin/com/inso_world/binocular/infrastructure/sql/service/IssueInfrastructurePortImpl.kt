package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.IssueInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.persistence.entity.IssueEntity
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Milestone
import com.inso_world.binocular.model.Note
import com.inso_world.binocular.model.User
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

@Service
@Validated
internal class IssueInfrastructurePortImpl :
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

    override fun findById(id: String): Issue? {
        TODO("Not yet implemented")
    }

    override fun findByIid(iid: Issue.Id): @Valid Issue? {
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
