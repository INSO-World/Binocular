package com.inso_world.binocular.core.integration.base

import com.inso_world.binocular.data.MockTestDataProvider
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Build
import com.inso_world.binocular.model.Job
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Mention
import com.inso_world.binocular.model.MergeRequest
import com.inso_world.binocular.model.Milestone
import com.inso_world.binocular.model.Module
import com.inso_world.binocular.model.Note
import com.inso_world.binocular.model.Platform
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
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
    private val project = Project(name = "proj-pg-0")
    private val repository = Repository(localPath = "repo-pg-0", project = project)

    private val mockTestDataProvider = MockTestDataProvider(repository)

    val testAccounts =
        listOf(
            Account(
                "1",
                "MDQ9JXMlcjY5MoB7Nah4",
                Platform.GitHub,
                "user1",
                "User One",
                "https://example.com/avatars/user1.png",
                "https://github.com/user1",
                project = project
            ),
            Account(
                "2",
                "MDQ9JXjIMjY5MoB7Nah4",
                Platform.GitLab,
                "user2",
                "User Two",
                "https://example.com/avatars/user2.png",
                "https://gitlab.com/user2",
                project = project
            ),
        )

//    private val mainBranch = Branch("main","abc123", repository = repository).apply {
//        active = true
//        tracksFileRenames = true
//        this.id = "1"
//    }
//    private val newFeatureBranch =
//        Branch( "feature/new-feature", true, false, "def456", repository = repository).apply {
//            this.id = "2"
//        }
    val testBranches = mockTestDataProvider.branches
//        listOf(mainBranch, newFeatureBranch)

    val testCommits = mockTestDataProvider.commits
//        listOf(
//            run {
//                val cmt =
//                    Commit(
//                        "abc123",
//                        LocalDateTime.now(),
//                        LocalDateTime.now(),
//                        "First commit",
//                        repository = repository,
//                    ).apply {
//                        this.id = "1"
//                        this.webUrl = "https://example.com/commit/abc123"
//                        this.branch = "main"
//                        this.stats = Stats(10, 5)
//                    }
//                cmt
//            },
//            run {
//                val cmt =
//                    Commit(
//                        "def456",
//                        java.time.LocalDateTime.now(),
//                        java.time.LocalDateTime.now(),
//                        "Second commit",
//                        repository = repository
//                    ).apply {
//                        this.id = "2"
//                        this.webUrl = "https://example.com/commit/def456"
//                        this.branch = "main"
//                        this.stats = Stats(7, 3)
//                    }
//                cmt
//            },
//        )

    val testBuilds =
        listOf(
            Build(
                "1",
                "abc123",
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
                "def456",
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
                        "src/main/kotlin/com/example/Main.kt",
                        mutableSetOf(),
                    ).apply {
                        this.id = "1"
                        this.webUrl = "https://example.com/files/Main.kt"
                    }
                return@run file
            },
            run {
                val file =
                    File(
                        "src/main/kotlin/com/example/Utils.kt",
                        mutableSetOf(),
                    ).apply {
                        this.id = "2"
                        this.webUrl = "https://example.com/files/Utils.kt"
                    }
                return@run file
            },
        )

    val testIssues =
        listOf(
            Issue(
                "1",
                101,
                "asdfasdf293487",
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
                        "abc123",
                        LocalDateTime.now(),
                        false,
                    ),
                ),
                project = project
            ),
            Issue(
                "2",
                102,
                "ölkjo342",
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
                        "def456",
                        java.time.LocalDateTime.now(),
                        true,
                    ),
                ),
                project = project
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
                        "abc123",
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
                        "def456",
                        java.time.LocalDateTime.now(),
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
            Project(name = "proj-pg-0").apply { this.id = "1" },
            Project(name = "proj-pg-1").apply { this.id = "2" },
            Project(name = "proj-pg-2").apply { this.id = "3" },
            Project(name = "proj-pg-3").apply { this.id = "4" },
            Project(name = "proj-pg-4").apply { this.id = "5" },
            Project(name = "proj-for-repos").apply {this.id = "6" },
        )

    val testRepositories =
        listOf(
            Repository(localPath = "repo-pg-0", project = testProjects.last()).apply {
                this.id = "r1"
            },
            Repository(localPath = "repo-pg-1", project = testProjects.last()).apply {
                this.id = "r2"
            },
            Repository(localPath = "repo-pg-2", project = testProjects.last()).apply {
                this.id = "r3"
            },
            Repository(localPath = "repo-pg-3", project = testProjects.last()).apply {
                this.id = "r4"
            },
            Repository(localPath = "repo-pg-4", project = testProjects.last()).apply {
                this.id = "r5"
            },
            Repository(localPath = "repo-pg-5", project = testProjects.last()).apply {
                this.id = "r6"
            },
            Repository(localPath = "repo-pg-6", project = testProjects.last()).apply {
                this.id = "r7"
            },
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
            User("John Doe", repository = repository).apply {
                this.id = "1"
                this.email = "john.doe@example.com"
            },
            User("Jane Smith", repository = repository).apply {
                this.id = "2"
                this.email = "jane.smith@example.com"
            },
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
