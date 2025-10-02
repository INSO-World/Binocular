package base

import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Build
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Mention
import com.inso_world.binocular.model.MergeRequest
import com.inso_world.binocular.model.Milestone
import com.inso_world.binocular.model.Note
import com.inso_world.binocular.model.Platform
import com.inso_world.binocular.model.CommitDiff.Stats
import com.inso_world.binocular.model.FileState
import com.inso_world.binocular.model.User
import java.time.LocalDateTime
import kotlin.run

/**
 * Provides test data for web module tests.
 * This class contains all the test entities and their relationships,
 * making the web module tests independent of infrastructure-specific test data.
 */
object TestDataProvider {
    val testAccounts =
        kotlin.collections.listOf(
            com.inso_world.binocular.model.Account(
                "1",
                com.inso_world.binocular.model.Platform.GitHub,
                "user1",
                "User One",
                "https://example.com/avatars/user1.png",
                "https://github.com/user1",
            ),
            com.inso_world.binocular.model.Account(
                "2",
                com.inso_world.binocular.model.Platform.GitLab,
                "user2",
                "User Two",
                "https://example.com/avatars/user2.png",
                "https://gitlab.com/user2",
            ),
        )

    private val mainBranch = com.inso_world.binocular.model.Branch("1", "main", true, true, "abc123")
    private val newFeatureBranch =
        com.inso_world.binocular.model.Branch("2", "feature/new-feature", true, false, "def456")
    val testBranches =
        kotlin.collections.listOf(mainBranch, newFeatureBranch)

    val testCommits =
        kotlin.collections.listOf(
            run {
                val cmt = com.inso_world.binocular.model.Commit(
                    "1",
                    "abc123",
                    java.time.LocalDateTime.now(),
                    java.time.LocalDateTime.now(),
                    "First commit",
                    null,
                    "https://example.com/commit/abc123",
                    "main",
                    com.inso_world.binocular.model.CommitDiff.Stats(10, 5),
                )
                mainBranch.commits.add(cmt)
                cmt
            },
            run {
                val cmt = com.inso_world.binocular.model.Commit(
                    "2",
                    "def456",
                    java.time.LocalDateTime.now(),
                    java.time.LocalDateTime.now(),
                    "Second commit",
                    null,
                    "https://example.com/commit/def456",
                    "main",
                    com.inso_world.binocular.model.CommitDiff.Stats(7, 3),
                )
                mainBranch.commits.add(cmt)
                cmt
            },
        )

    val testBuilds =
        kotlin.collections.listOf(
            com.inso_world.binocular.model.Build(
                "1",
                "abc123",
                "main",
                "success",
                "v0.0.1-rc",
                "user1",
                "User One",
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                120,
                kotlin.collections.listOf(
                    com.inso_world.binocular.model.Build.Job(
                        "job1",
                        "test",
                        "success",
                        "test",
                        java.time.LocalDateTime.now(),
                        java.time.LocalDateTime.now(),
                        "https://example.com/jobs/job1",
                    ),
                ),
                "https://example.com/builds/1",
            ),
            com.inso_world.binocular.model.Build(
                "2",
                "def456",
                "feature/new-feature",
                "failed",
                "v1.0.0",
                "user2",
                "User Two",
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                180,
                kotlin.collections.listOf(
                    com.inso_world.binocular.model.Build.Job(
                        "job2",
                        "build",
                        "failed",
                        "build",
                        java.time.LocalDateTime.now(),
                        java.time.LocalDateTime.now(),
                        "https://example.com/jobs/job2",
                    ),
                ),
                "https://example.com/builds/2",
            ),
        )

    val testFiles =
        kotlin.collections.listOf(
            run {
                val file = com.inso_world.binocular.model.File(
                    "1",
                    "src/main/kotlin/com/example/Main.kt",
                    kotlin.collections.mutableSetOf()
                )
                file.webUrl = "https://example.com/files/Main.kt"
                return@run file
            },
            run {
                val file = com.inso_world.binocular.model.File(
                    "2",
                    "src/main/kotlin/com/example/Utils.kt",
                    kotlin.collections.mutableSetOf()
                )
                file.webUrl = "https://example.com/files/Utils.kt"
                return@run file
            }
        )

    val testIssues =
        kotlin.collections.listOf(
            com.inso_world.binocular.model.Issue(
                "1",
                101,
                "Fix bug in login flow",
                "Users are unable to log in...",
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                null,
                kotlin.collections.listOf("bug", "high-priority"),
                "open",
                "https://example.com/issues/101",
                kotlin.collections.listOf(
                    com.inso_world.binocular.model.Mention(
                        "abc123",
                        java.time.LocalDateTime.now(),
                        false
                    )
                ),
            ),
            com.inso_world.binocular.model.Issue(
                "2",
                102,
                "Add new feature",
                "Implement profile customization",
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                kotlin.collections.listOf("enhancement", "feature"),
                "closed",
                "https://example.com/issues/102",
                kotlin.collections.listOf(
                    com.inso_world.binocular.model.Mention(
                        "def456",
                        java.time.LocalDateTime.now(),
                        true
                    )
                ),
            ),
        )

    val testMergeRequests =
        kotlin.collections.listOf(
            com.inso_world.binocular.model.MergeRequest(
                "1",
                201,
                "Implement user authentication",
                "Add JWT auth",
                java.time.LocalDateTime.now().toString(),
                java.time.LocalDateTime.now().toString(),
                null,
                kotlin.collections.listOf("feature", "security"),
                "open",
                "https://example.com/merge_requests/201",
                kotlin.collections.listOf(
                    com.inso_world.binocular.model.Mention(
                        "abc123",
                        java.time.LocalDateTime.now(),
                        false
                    )
                ),
            ),
            com.inso_world.binocular.model.MergeRequest(
                "2",
                202,
                "Fix CSS",
                "Fix responsive design",
                java.time.LocalDateTime.now().toString(),
                java.time.LocalDateTime.now().toString(),
                java.time.LocalDateTime.now().toString(),
                kotlin.collections.listOf("bug", "ui"),
                "merged",
                "https://example.com/merge_requests/202",
                kotlin.collections.listOf(
                    com.inso_world.binocular.model.Mention(
                        "def456",
                        java.time.LocalDateTime.now(),
                        true
                    )
                ),
            ),
        )

    val testModules =
        kotlin.collections.listOf(
            com.inso_world.binocular.model.Module("1", "src/main/kotlin/com/example/core"),
            com.inso_world.binocular.model.Module("2", "src/main/kotlin/com/example/api"),
        )

    val testNotes =
        kotlin.collections.listOf(
            com.inso_world.binocular.model.Note(
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
            com.inso_world.binocular.model.Note(
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
        kotlin.collections.listOf(
            com.inso_world.binocular.model.User("1", "John Doe", "john.doe@example.com"),
            com.inso_world.binocular.model.User("2", "Jane Smith", "jane.smith@example.com"),
        )

    val testMilestones =
        kotlin.collections.listOf(
            com.inso_world.binocular.model.Milestone(
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
            com.inso_world.binocular.model.Milestone(
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
