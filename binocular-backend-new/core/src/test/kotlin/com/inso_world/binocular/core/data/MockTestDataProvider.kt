package com.inso_world.binocular.core.data

import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import java.time.LocalDateTime

class MockTestDataProvider {
    val testProjects =
        listOf(
            Project(name = "proj-pg-0"),
            Project(name = "proj-pg-1"),
            Project(name = "proj-pg-2"),
            Project(name = "proj-pg-3"),
            Project(name = "proj-pg-4"),
            Project(name = "proj-for-repos"),
        )
    val projectsByName = testProjects.associateBy { requireNotNull(it.name) }

    val testRepositories =
        listOf(
            run {
                val project = requireNotNull(projectsByName["proj-pg-0"])
                val repo = Repository(localPath = "repo-pg-0", project = project)
                project.repo = repo
                repo
            },
            run {
                val project = requireNotNull(projectsByName["proj-pg-1"])
                val repo = Repository(localPath = "repo-pg-1", project = project)
                project.repo = repo
                repo
            },
            run {
                val project = requireNotNull(projectsByName["proj-pg-2"])
                val repo = Repository(localPath = "repo-pg-2", project = project)
                project.repo = repo
                repo
            },
            run {
                val project = requireNotNull(projectsByName["proj-pg-3"])
                val repo = Repository(localPath = "repo-pg-3", project = project)
                project.repo = repo
                repo
            },
            run {
                val project = requireNotNull(projectsByName["proj-pg-4"])
                val repo = Repository(localPath = "repo-pg-4", project = project)
                project.repo = repo
                repo
            },
            run {
                val project = requireNotNull(projectsByName["proj-for-repos"])
                val repo = Repository(localPath = "repo-pg-5", project = project)
                project.repo = repo
                repo
            },
        )
    val repositoriesByPath = testRepositories.associateBy { requireNotNull(it.localPath) }
    private val repository: Repository = requireNotNull(repositoriesByPath["repo-pg-0"])

    val users: List<User> = listOf(
        run {
            val user = User(name = "User A", email = "a@test.com")
            user.repository = repository
            requireNotNull(repository.user.add(user)) {
                "User ${user.name} already added to repository"
            }
            user
        },
        run {
            val user = User(name = "User B", email = "b@test.com")
            user.repository = repository
            requireNotNull(repository.user.add(user)) {
                "User ${user.name} already added to repository"
            }
            user
        },
        run {
            val user = User(name = "Author Only", email = "author@test.com")
            user.repository = repository
            requireNotNull(repository.user.add(user)) {
                "User ${user.name} already added to repository"
            }
            user
        }
    )
    val userByEmail = users.associateBy { requireNotNull(it.email) }

    val commits: List<Commit> = listOf(
        run {
            val cmt = Commit(
                sha = "a".repeat(40),
                message = "msg1",
                commitDateTime = LocalDateTime.now(),
                authorDateTime = LocalDateTime.now(),
                repository = repository,
            )
            cmt.committer = userByEmail["a@test.com"]
            require(repository.commits.add(cmt)) {
                "Commit ${cmt.sha} already added"
            }
            cmt
        },
        run {
            val cmt = Commit(
                sha = "b".repeat(40),
                message = "msg2",
                commitDateTime = LocalDateTime.now(),
                authorDateTime = LocalDateTime.now(),
                repository = repository,
            ) // Empty parents list
            cmt.committer = userByEmail["b@test.com"]
            require(repository.commits.add(cmt)) {
                "Commit ${cmt.sha} already added"
            }
            cmt
        },
        run {
            val cmt = Commit(
                sha = "c".repeat(40),
                message = "msg1",
                commitDateTime = LocalDateTime.now(),
                authorDateTime = LocalDateTime.now(),
                repository = repository,
            )
            cmt.committer = userByEmail["author@test.com"]
            require(repository.commits.add(cmt)) {
                "Commit ${cmt.sha} already added"
            }
            cmt
        },
        run {
            val cmt = Commit(
                sha = "d".repeat(40),
                message = "msg-d",
                commitDateTime = LocalDateTime.now(),
                authorDateTime = LocalDateTime.now(),
                repository = repository,
            )
            cmt.committer = userByEmail["author@test.com"]
            require(repository.commits.add(cmt)) {
                "Commit ${cmt.sha} already added"
            }
            cmt
        }
    )
    val commitBySha = commits.associateBy(Commit::sha)
}
