package com.inso_world.binocular.data

import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import java.time.LocalDateTime

class MockTestDataProvider(
    // repository to be used by child elements of repository (e.g. commits, users, branches...)
    val repository: Repository
) {
    val testProjects = listOf(
        Project(name = "proj-pg-0"),
        Project(name = "proj-pg-1"),
        Project(name = "proj-pg-2"),
        Project(name = "proj-pg-3"),
        Project(name = "proj-pg-4"),
        Project(name = "proj-pg-5"),
        Project(name = "proj-for-repos"),
    )
    val projectsByName = testProjects.associateBy { requireNotNull(it.name) }

    val testRepositories: List<Repository> = listOf(
        run {
            val project = projectsByName.getValue("proj-pg-0")
            val repo = Repository(localPath = "repo-pg-0", project = project)
            repo
        },
        run {
            val project = projectsByName.getValue("proj-pg-1")
            val repo = Repository(localPath = "repo-pg-1", project = project)
            repo
        },
        run {
            val project = projectsByName.getValue("proj-pg-2")
            val repo = Repository(localPath = "repo-pg-2", project = project)
            repo
        },
        run {
            val project = projectsByName.getValue("proj-pg-3")
            val repo = Repository(localPath = "repo-pg-3", project = project)
            repo
        },
        run {
            val project = projectsByName.getValue("proj-pg-4")
            val repo = Repository(localPath = "repo-pg-4", project = project)
            repo
        },
        run {
            val project = projectsByName.getValue("proj-for-repos")
            val repo = Repository(localPath = "repo-pg-5", project = project)
            repo
        },
        run {
            val project = projectsByName.getValue("proj-pg-5")
            val repo = Repository(localPath = "repo-empty", project = project)
            repo
        }
    )
    val repositoriesByPath = testRepositories.associateBy { requireNotNull(it.localPath) }

    val users: List<User> = listOf(run {
        val user = User(name = "User A", repository = repository).apply { this.email = "a@test.com" }
        user
    }, run {
        val user = User(name = "User B", repository = repository).apply { this.email = "b@test.com" }
        user
    }, run {
        val user = User(name = "Author Only", repository = repository).apply { this.email = "author@test.com" }
        user
    })
    val userByEmail = users.associateBy { requireNotNull(it.email) }

    val commits: List<Commit> = listOf(run {
        val cmt = Commit(
            sha = "a".repeat(40),
            message = "msg1",
            commitDateTime = LocalDateTime.now().minusSeconds(1),
            authorDateTime = LocalDateTime.now().minusSeconds(1),
            repository = repository,
        )
        cmt
    }, run {
        val cmt = Commit(
            sha = "b".repeat(40),
            message = "msg2",
            commitDateTime = LocalDateTime.now().minusSeconds(1),
            authorDateTime = LocalDateTime.now().minusSeconds(1),
            repository = repository,
        ) // Empty parents list
        cmt
    }, run {
        val cmt = Commit(
            sha = "c".repeat(40),
            message = "msg1",
            commitDateTime = LocalDateTime.now().minusSeconds(1),
            authorDateTime = LocalDateTime.now().minusSeconds(1),
            repository = repository,
        )
        cmt
    }, run {
        val cmt = Commit(
            sha = "d".repeat(40),
            message = "msg-d",
            commitDateTime = LocalDateTime.now().minusSeconds(1),
            authorDateTime = LocalDateTime.now().minusSeconds(1),
            repository = repository,
        )
        cmt
    })
    val commitBySha = commits.associateBy(Commit::sha)

    val branches = listOf(
        run {
            val branch = Branch(name = "origin/feature/test", repository = repository)
            return@run branch
        },
        run {
            val branch = Branch(name = "origin/fixme/123", repository = repository)
            return@run branch
        })

    val branchByName = branches.associateBy(Branch::name)
}
