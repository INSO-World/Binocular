package com.inso_world.binocular.domain.data

import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import com.inso_world.binocular.model.User
import com.inso_world.binocular.model.vcs.ReferenceCategory
import java.time.LocalDateTime

class MockTestDataProvider(
    val repository: Repository,
) {
    val testProjects =
        listOf(
            Project(name = "proj-pg-0"),
            Project(name = "proj-pg-1"),
            Project(name = "proj-pg-2"),
            Project(name = "proj-pg-3"),
            Project(name = "proj-pg-4"),
            Project(name = "proj-pg-5"),
            Project(name = "proj-for-repos"),
        )
    val projectsByName = testProjects.associateBy { requireNotNull(it.name) }

    val testRepositories: List<Repository> =
        listOf(
            run {
                val project = projectsByName.getValue("proj-pg-0")
                Repository(localPath = "repo-pg-0", projectId = project.iid)
            },
            run {
                val project = projectsByName.getValue("proj-pg-1")
                Repository(localPath = "repo-pg-1", projectId = project.iid)
            },
            run {
                val project = projectsByName.getValue("proj-pg-2")
                Repository(localPath = "repo-pg-2", projectId = project.iid)
            },
            run {
                val project = projectsByName.getValue("proj-pg-3")
                Repository(localPath = "repo-pg-3", projectId = project.iid)
            },
            run {
                val project = projectsByName.getValue("proj-pg-4")
                Repository(localPath = "repo-pg-4", projectId = project.iid)
            },
            run {
                val project = projectsByName.getValue("proj-for-repos")
                Repository(localPath = "repo-pg-5", projectId = project.iid)
            },
            run {
                val project = projectsByName.getValue("proj-pg-5")
                Repository(localPath = "repo-empty", projectId = project.iid)
            },
        )
    val repositoriesByPath = testRepositories.associateBy { requireNotNull(it.localPath) }

    @Deprecated("Use developers instead")
    val users: List<User> =
        listOf(
            User(name = "User A", repositoryId = repository.iid).apply { this.email = "a@test.com" },
            User(name = "User B", repositoryId = repository.iid).apply { this.email = "b@test.com" },
            User(name = "User C", repositoryId = repository.iid).apply { this.email = "c@test.com" },
            User(name = "User D", repositoryId = repository.iid).apply { this.email = "d@test.com" },
            User(name = "Author Only", repositoryId = repository.iid).apply { this.email = "author@test.com" },
        )

    @Deprecated("Use developerByEmail instead")
    val userByEmail = users.associateBy { requireNotNull(it.email) }

    // New Developer-based test data
    val developers: List<Developer> =
        listOf(
            Developer(name = "User A", email = "a@test.com", repositoryId = repository.iid),
            Developer(name = "User B", email = "b@test.com", repositoryId = repository.iid),
            Developer(name = "User C", email = "c@test.com", repositoryId = repository.iid),
            Developer(name = "User D", email = "d@test.com", repositoryId = repository.iid),
            Developer(name = "Author Only", email = "author@test.com", repositoryId = repository.iid),
        )
    val developerByEmail = developers.associateBy { it.email }

    val commits: List<Commit> =
        listOf(
            run {
                val dev = developerByEmail.getValue("a@test.com")
                val timestamp = LocalDateTime.now().minusSeconds(1)
                Commit(
                    sha = "a".repeat(40),
                    message = "msg1",
                    authorSignature = Signature(developerId = dev.iid, timestamp = timestamp),
                    repositoryId = repository.iid,
                )
            },
            run {
                val dev = developerByEmail.getValue("b@test.com")
                val timestamp = LocalDateTime.now().minusSeconds(1)
                Commit(
                    sha = "b".repeat(40),
                    message = "msg2",
                    authorSignature = Signature(developerId = dev.iid, timestamp = timestamp),
                    repositoryId = repository.iid,
                )
            },
            run {
                val dev = developerByEmail.getValue("c@test.com")
                val timestamp = LocalDateTime.now().minusSeconds(1)
                Commit(
                    sha = "c".repeat(40),
                    message = "msg1",
                    authorSignature = Signature(developerId = dev.iid, timestamp = timestamp),
                    repositoryId = repository.iid,
                )
            },
            run {
                val dev = developerByEmail.getValue("d@test.com")
                val timestamp = LocalDateTime.now().minusSeconds(1)
                Commit(
                    sha = "d".repeat(40),
                    message = "msg-d",
                    authorSignature = Signature(developerId = dev.iid, timestamp = timestamp),
                    repositoryId = repository.iid,
                )
            },
        )
    val commitBySha = commits.associateBy(Commit::sha)

    val branches =
        listOf(
            Branch(
                fullName = "refs/remotes/origin/feature/test",
                name = "origin/feature/test",
                repositoryId = repository.iid,
                headSha = commitBySha.getValue("a".repeat(40)).sha,
                category = ReferenceCategory.REMOTE_BRANCH,
            ),
            Branch(
                fullName = "refs/remotes/origin/fixme/123",
                name = "origin/fixme/123",
                repositoryId = repository.iid,
                headSha = commitBySha.getValue("a".repeat(40)).sha,
                category = ReferenceCategory.REMOTE_BRANCH,
            ),
        )

    val branchByName = branches.associateBy(Branch::name)
}
