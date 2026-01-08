package com.inso_world.binocular.core.integration.base

import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Build
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Job
import com.inso_world.binocular.model.Mention
import com.inso_world.binocular.model.MergeRequest
import com.inso_world.binocular.model.Milestone
import com.inso_world.binocular.model.Module
import com.inso_world.binocular.model.Note
import com.inso_world.binocular.model.Platform
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Stats
import com.inso_world.binocular.model.User
import java.time.LocalDateTime

/**
 * Provides test data for web module tests.
 * This class contains all the test entities and their relationships,
 * making the web module tests independent of infrastructure-specific test data.
 */
@Deprecated(
    "Objects are singletons, every change is propagated over tests. Instead, use the MockTestDataProvider class and create a new object before each test rung to have a clean state",
    replaceWith = ReplaceWith("com.inso_world.binocular.core.data.MockTestDataProvider")
)
object TestDataProvider {
    val testAccounts =
        listOf(
            Account(
                "1",
                Platform.GitHub,
                "user1",
                "User One",
                "https://example.com/avatars/user1.png",
                "https://github.com/user1",
            ),
            Account(
                "2",
                Platform.GitLab,
                "user2",
                "User Two",
                "https://example.com/avatars/user2.png",
                "https://gitlab.com/user2",
            ),
        )

    private val mainBranch = Branch("1", "main", true, true, "abc1230000000000000000000000000000000000")
    private val newFeatureBranch =
        Branch("2", "feature/new-feature", true, false, "def4560000000000000000000000000000000000")
    val testBranches =
        listOf(mainBranch, newFeatureBranch)

    val testCommits =
        listOf(
            run {
                val cmt =
                    Commit(
                        "1",
                        "abc1230000000000000000000000000000000000",
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        "First commit",
                        null,
                        "https://example.com/commit/abc1230000000000000000000000000000000000",
                        "main",
                        Stats(10, 5),
                    )
                mainBranch.commits.add(cmt)
                cmt
            },
            run {
                val cmt =
                    Commit(
                        "2",
                        "def4560000000000000000000000000000000000",
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        "Second commit",
                        null,
                        "https://example.com/commit/def4560000000000000000000000000000000000",
                        "main",
                        Stats(7, 3),
                    )
                mainBranch.commits.add(cmt)
                cmt
            },
        )

    val testBuilds =
        listOf(
            Build(
                "1",
                "abc1230000000000000000000000000000000000",
                "main",
                "success",
                "v0.0.1-rc",
                "user1",
                "User One",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                120,
                listOf(
                    Job(
                        "job1",
                        "test",
                        "success",
                        "test",
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        "https://example.com/jobs/job1",
                    ),
                ),
                "https://example.com/builds/1",
            ),
            Build(
                "2",
                "def4560000000000000000000000000000000000",
                "feature/new-feature",
                "failed",
                "v1.0.0",
                "user2",
                "User Two",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                180,
                listOf(
                    Job(
                        "job2",
                        "build",
                        "failed",
                        "build",
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        "https://example.com/jobs/job2",
                    ),
                ),
                "https://example.com/builds/2",
            ),
        )

    val testFiles =
        listOf(
            run {
                val file =
                    File(
                        "1",
                        "src/main/kotlin/com/example/Main.kt",
                        mutableSetOf(),
                    )
                file.webUrl = "https://example.com/files/Main.kt"
                return@run file
            },
            run {
                val file =
                    File(
                        "2",
                        "src/main/kotlin/com/example/Utils.kt",
                        mutableSetOf(),
                    )
                file.webUrl = "https://example.com/files/Utils.kt"
                return@run file
            },
        )

    val testIssues =
        listOf(
            Issue(
                "1",
                101,
                "Fix bug in login flow",
                "Users are unable to log in...",
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                listOf("bug", "high-priority"),
                "open",
                "https://example.com/issues/101",
                listOf(
                    Mention(
                        "abc1230000000000000000000000000000000000",
                        LocalDateTime.now(),
                        false,
                    ),
                ),
            ),
            Issue(
                "2",
                102,
                "Add new feature",
                "Implement profile customization",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                listOf("enhancement", "feature"),
                "closed",
                "https://example.com/issues/102",
                listOf(
                    Mention(
                        "def4560000000000000000000000000000000000",
                        LocalDateTime.now(),
                        true,
                    ),
                ),
            ),
        )

    val testMergeRequests =
        listOf(
            MergeRequest(
                "1",
                201,
                "Implement user authentication",
                "Add JWT auth",
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                null,
                listOf("feature", "security"),
                "open",
                "https://example.com/merge_requests/201",
                listOf(
                    Mention(
                        "abc1230000000000000000000000000000000000",
                        LocalDateTime.now(),
                        false,
                    ),
                ),
            ),
            MergeRequest(
                "2",
                202,
                "Fix CSS",
                "Fix responsive design",
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                listOf("bug", "ui"),
                "merged",
                "https://example.com/merge_requests/202",
                listOf(
                    Mention(
                        "def4560000000000000000000000000000000000",
                        LocalDateTime.now(),
                        true,
                    ),
                ),
            ),
        )

    val testModules =
        listOf(
            Module("1", "src/main/kotlin/com/example/core"),
            Module("2", "src/main/kotlin/com/example/api"),
        )

    val testProjects =
        listOf(
            Project(id = "1", name = "proj-pg-0"),
            Project(id = "2", name = "proj-pg-1"),
            Project(id = "3", name = "proj-pg-2"),
            Project(id = "4", name = "proj-pg-3"),
            Project(id = "5", name = "proj-pg-4"),
            Project(id = "6", name = "proj-for-repos"),
        )

    val testRepositories =
        listOf(
            Repository(id = "r1", localPath = "repo-pg-0", project = testProjects.last()),
            Repository(id = "r2", localPath = "repo-pg-1", project = testProjects.last()),
            Repository(id = "r3", localPath = "repo-pg-2", project = testProjects.last()),
            Repository(id = "r4", localPath = "repo-pg-3", project = testProjects.last()),
            Repository(id = "r5", localPath = "repo-pg-4", project = testProjects.last()),
            Repository(id = "r6", localPath = "repo-pg-5", project = testProjects.last()),
            Repository(id = "r7", localPath = "repo-pg-6", project = testProjects.last()),
        )

    val testNotes =
        listOf(
            Note(
                "1",
                "This is a comment",
                "2023-01-01T10:00:00Z",
                "2023-01-01T10:00:00Z",
                false,
                true,
                false,
                false,
                false,
                "gitlab",
            ),
            Note(
                "2",
                "This is a system note",
                "2023-01-02T11:00:00Z",
                "2023-01-02T11:00:00Z",
                true,
                false,
                false,
                true,
                false,
                "github",
            ),
        )

    val testUsers =
        listOf(
            User("1", "John Doe", "john.doe@example.com"),
            User("2", "Jane Smith", "jane.smith@example.com"),
        )

    val testMilestones =
        listOf(
            Milestone(
                id = "1",
                iid = 201,
                title = "Release 1.0",
                description = "First stable release",
                createdAt = "2023-01-01T10:00:00Z",
                updatedAt = "2023-01-10T15:30:00Z",
                startDate = "2023-01-15T00:00:00Z",
                dueDate = "2023-02-15T00:00:00Z",
                state = "active",
                expired = false,
                webUrl = "https://example.com/milestones/1",
            ),
            Milestone(
                id = "2",
                iid = 202,
                title = "Release 2.0",
                description = "Second major release",
                createdAt = "2023-02-01T10:00:00Z",
                updatedAt = "2023-02-10T15:30:00Z",
                startDate = "2023-02-15T00:00:00Z",
                dueDate = "2023-03-15T00:00:00Z",
                state = "active",
                expired = false,
                webUrl = "https://example.com/milestones/2",
            ),
        )

}
