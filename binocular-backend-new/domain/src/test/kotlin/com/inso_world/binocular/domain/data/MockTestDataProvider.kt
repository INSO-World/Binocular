package com.inso_world.binocular.domain.data

import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import com.inso_world.binocular.model.Stats
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
                Repository(localPath = "repo-pg-0", project = project)
            },
            run {
                val project = projectsByName.getValue("proj-pg-1")
                Repository(localPath = "repo-pg-1", project = project)
            },
            run {
                val project = projectsByName.getValue("proj-pg-2")
                Repository(localPath = "repo-pg-2", project = project)
            },
            run {
                val project = projectsByName.getValue("proj-pg-3")
                Repository(localPath = "repo-pg-3", project = project)
            },
            run {
                val project = projectsByName.getValue("proj-pg-4")
                Repository(localPath = "repo-pg-4", project = project)
            },
            run {
                val project = projectsByName.getValue("proj-for-repos")
                Repository(localPath = "repo-pg-5", project = project)
            },
            run {
                val project = projectsByName.getValue("proj-pg-5")
                Repository(localPath = "repo-empty", project = project)
            },
        )
    val repositoriesByPath = testRepositories.associateBy { requireNotNull(it.localPath) }

    // Legacy User support (deprecated)
    @Deprecated("Use developers instead")
    val users: List<User> =
        listOf(
            User(name = "User A", repository = repository).apply { this.email = "a@test.com" },
            User(name = "User B", repository = repository).apply { this.email = "b@test.com" },
            User(name = "User C", repository = repository).apply { this.email = "c@test.com" },
            User(name = "User D", repository = repository).apply { this.email = "d@test.com" },
            User(name = "Author Only", repository = repository).apply { this.email = "author@test.com" },
        )

    @Deprecated("Use developerByEmail instead")
    val userByEmail = users.associateBy { requireNotNull(it.email) }

    // New Developer-based test data
    val developers: List<Developer> =
        listOf(
            Developer(name = "User A", email = "a@test.com", repository = repository).apply { this.id = "d1" },
            Developer(name = "User B", email = "b@test.com", repository = repository).apply { this.id = "d2" },
            Developer(name = "User C", email = "c@test.com", repository = repository).apply { this.id = "d3" },
            Developer(name = "User D", email = "d@test.com", repository = repository).apply { this.id = "d4" },
            Developer(name = "Author Only", email = "author@test.com", repository = repository).apply { this.id = "d5" },
        )
    val developerByEmail = developers.associateBy { it.email }

    val commits: List<Commit> =
        listOf(
            run {
                val sha = "a".repeat(40)
                val dev = developerByEmail.getValue("a@test.com")
                val timestamp = LocalDateTime.now().minusSeconds(1)
                Commit(
                    sha = sha,
                    message = "msg1",
                    authorSignature = Signature(developer = dev, timestamp = timestamp),
                    repository = repository,
                ).apply {
                    this.id = "1"
                    this.webUrl = "https://example.com/commit/$sha"
                    this.stats = Stats(10, 5)
                }
            },
            run {
                val dev = developerByEmail.getValue("b@test.com")
                val timestamp = LocalDateTime.now().minusSeconds(1)
                val sha = "b".repeat(40)
                Commit(
                    sha = sha,
                    message = "msg2",
                    authorSignature = Signature(developer = dev, timestamp = timestamp),
                    repository = repository,
                ).apply {
                    this.id = "2"
                    this.webUrl = "https://example.com/commit/$sha"
                    this.stats = Stats(7, 3)
                }
            },
            run {
                val dev = developerByEmail.getValue("c@test.com")
                val timestamp = LocalDateTime.now().minusSeconds(1)
                val sha = "c".repeat(40)
                Commit(
                    sha = sha,
                    message = "msg1",
                    authorSignature = Signature(developer = dev, timestamp = timestamp),
                    repository = repository,
                ).apply {
                    this.id = "3"
                    this.webUrl = "https://example.com/commit/$sha"
                    this.stats = Stats(7, 3)
                    this.branch = "main"
                }
            },
            run {
                val dev = developerByEmail.getValue("d@test.com")
                val timestamp = LocalDateTime.now().minusSeconds(1)
                val sha = "d".repeat(40)
                Commit(
                    sha = sha,
                    message = "msg-d",
                    authorSignature = Signature(developer = dev, timestamp = timestamp),
                    repository = repository,
                ).apply {
                    this.id = "4"
                    this.webUrl = "https://example.com/commit/$sha"
                    this.stats = Stats(7, 3)
                    this.branch = "main"
                }
            },
        )
    val commitBySha = commits.associateBy(Commit::sha)

    val branches =
        listOf(
            Branch(
                fullName = "refs/heads/main",
                name = "main",
                repository = repository,
                head = commitBySha.getValue("a".repeat(40)),
                category = ReferenceCategory.LOCAL_BRANCH,
            ).apply {
                this.id = "1"
                @Suppress("DEPRECATION")
                this.active = true
                @Suppress("DEPRECATION")
                this.tracksFileRenames = true
                with(this.head) {
                    // this is a hacky solution but back propagates the branch name on creation to the legacy field
                    this.branch = this@apply.name
                }
            },
            Branch(
                fullName = "refs/heads/feature/new-feature",
                name = "feature/new-feature",
                repository = repository,
                head = commitBySha.getValue("b".repeat(40)),
                category = ReferenceCategory.LOCAL_BRANCH,
            ).apply {
                this.id = "2"
                @Suppress("DEPRECATION")
                this.active = true
                @Suppress("DEPRECATION")
                this.tracksFileRenames = false
                with(this.head) {
                    // this is a hacky solution but back propagates the branch name on creation to the legacy field
                    this.branch = this@apply.name
                }
            },
        )

    val branchByName = branches.associateBy(Branch::name)
}
