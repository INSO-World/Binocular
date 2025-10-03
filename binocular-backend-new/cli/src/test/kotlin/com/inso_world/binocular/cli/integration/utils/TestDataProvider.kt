package com.inso_world.binocular.cli.integration.utils

import com.inso_world.binocular.core.index.GitIndexer
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import java.time.LocalDateTime
import kotlin.io.path.Path

internal class RealDataProvider(
    private val idx: GitIndexer,

    ) {
    private lateinit var repo: Repository
    lateinit var project: Project

    fun setUp(
        projectName: String,
        repoPath: String,
        startSha: String? = "HEAD",
        branch: Branch,
    ) {
        project = Project(
            name = projectName,
        )
        repo = idx.findRepo(Path(repoPath).toRealPath())
        project.repo = repo
        repo.project = project

        require(repo.branches.add(branch))
//        val cmt = idx.findCommit(repo, startSha ?: "HEAD")
        idx.traverseBranch(repo,branch)
    }


}

internal class TestDataProvider {
    val project = Project(
        name = "test-project",
    )

    val repository = run {
        val repo = Repository(
            localPath = "test-repository",
            project = project,
        )
        project.repo = repo
        repo
    }

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
