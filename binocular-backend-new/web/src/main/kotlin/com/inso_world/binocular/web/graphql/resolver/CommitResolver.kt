package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.web.entity.*
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitBuildConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitCommitConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitFileConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitModuleConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitUserConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueCommitConnectionRepository
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class CommitResolver(
  private val commitBuildConnectionRepository: CommitBuildConnectionRepository,
  private val commitCommitConnectionRepository: CommitCommitConnectionRepository,
  private val commitFileConnectionRepository: CommitFileConnectionRepository,
  private val commitModuleConnectionRepository: CommitModuleConnectionRepository,
  private val commitUserConnectionRepository: CommitUserConnectionRepository,
  private val issueCommitConnectionRepository: IssueCommitConnectionRepository
) {
    @SchemaMapping(typeName = "Commit", field = "builds")
    fun builds(commit: Commit): List<Build> {
        val id = commit.id ?: return emptyList()
        // Get all connections for this commit and extract the builds
        return commitBuildConnectionRepository.findBuildsByCommit(id)
    }

    @SchemaMapping(typeName = "Commit", field = "files")
    fun files(commit: Commit): List<File> {
        val id = commit.id ?: return emptyList()
        // Get all connections for this commit and extract the files
        return commitFileConnectionRepository.findFilesByCommit(id)
    }

    @SchemaMapping(typeName = "Commit", field = "modules")
    fun modules(commit: Commit): List<Module> {
        val id = commit.id ?: return emptyList()
        // Get all connections for this commit and extract the modules
        return commitModuleConnectionRepository.findModulesByCommit(id)
    }

    @SchemaMapping(typeName = "Commit", field = "users")
    fun users(commit: Commit): List<User> {
        val id = commit.id ?: return emptyList()
        // Get all connections for this commit and extract the users
        return commitUserConnectionRepository.findUsersByCommit(id)
    }

    @SchemaMapping(typeName = "Commit", field = "issues")
    fun issues(commit: Commit): List<Issue> {
        val id = commit.id ?: return emptyList()
        // Get all connections for this commit and extract the issues
        return issueCommitConnectionRepository.findIssuesByCommit(id)
    }

    @SchemaMapping(typeName = "Commit", field = "parents")
    fun parents(commit: Commit): List<Commit> {
        val id = commit.id ?: return emptyList()
        // Get all connections for this commit and extract the parent commits
        return commitCommitConnectionRepository.findParentCommitsByChildCommit(id)
    }

    @SchemaMapping(typeName = "Commit", field = "children")
    fun children(commit: Commit): List<Commit> {
        val id = commit.id ?: return emptyList()
        // Get all connections for this commit and extract the child commits
        return commitCommitConnectionRepository.findChildCommitsByParentCommit(id)
    }
}
