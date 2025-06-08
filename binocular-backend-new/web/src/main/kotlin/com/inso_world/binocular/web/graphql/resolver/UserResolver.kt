package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.web.entity.*
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitUserConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueUserConnectionRepository
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class UserResolver(
    private val commitUserConnectionRepository: CommitUserConnectionRepository,
    private val issueUserConnectionRepository: IssueUserConnectionRepository
) {
    @SchemaMapping(typeName = "User", field = "commits")
    fun commits(user: User): List<Commit> {
        val id = user.id ?: return emptyList()
        // Get all connections for this user and extract the commits
        return commitUserConnectionRepository.findCommitsByUser(id)
    }

    @SchemaMapping(typeName = "User", field = "issues")
    fun issues(user: User): List<Issue> {
        val id = user.id ?: return emptyList()
        // Get all connections for this user and extract the issues
        return issueUserConnectionRepository.findIssuesByUser(id)
    }
}
