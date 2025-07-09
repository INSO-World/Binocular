package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.service.IssueInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.persistence.entity.IssueEntity
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Milestone
import com.inso_world.binocular.model.Note
import com.inso_world.binocular.model.User
import org.springframework.stereotype.Service

@Service
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
}
