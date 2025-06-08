package com.inso_world.binocular.web

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.entity.Branch
import com.inso_world.binocular.web.entity.Build
import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.Mention
import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.entity.Platform
import com.inso_world.binocular.web.entity.Stats
import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.persistence.repository.arangodb.AccountRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.BranchRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.BuildRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.CommitRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.FileRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.IssueRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.MergeRequestRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.ModuleRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.NoteRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester
import org.springframework.graphql.test.tester.GraphQlTester
import java.util.Date

/**
 * Base test class that sets up test data in the database.
 * This class can be extended by test classes that need database test data.
 */
@SpringBootTest
@AutoConfigureGraphQlTester
abstract class BaseDbTest {

    @Autowired
    protected lateinit var graphQlTester: GraphQlTester

    @Autowired
    protected lateinit var commitRepository: CommitRepository

    @Autowired
    protected lateinit var accountRepository: AccountRepository

    @Autowired
    protected lateinit var branchRepository: BranchRepository

    @Autowired
    protected lateinit var buildRepository: BuildRepository

    @Autowired
    protected lateinit var fileRepository: FileRepository

    @Autowired
    protected lateinit var issueRepository: IssueRepository

    @Autowired
    protected lateinit var mergeRequestRepository: MergeRequestRepository

    @Autowired
    protected lateinit var moduleRepository: ModuleRepository

    @Autowired
    protected lateinit var noteRepository: NoteRepository

    @Autowired
    protected lateinit var userRepository: UserRepository

    /**
     * Test data that will be created in the database.
     */
    protected val testAccounts = listOf(
        Account(
            id = "1",
            platform = Platform.GitHub,
            login = "user1",
            name = "User One",
            avatarUrl = "https://example.com/avatars/user1.png",
            url = "https://github.com/user1"
        ),
        Account(
            id = "2",
            platform = Platform.GitLab,
            login = "user2",
            name = "User Two",
            avatarUrl = "https://example.com/avatars/user2.png",
            url = "https://gitlab.com/user2"
        )
    )

    /**
     * Test data that will be created in the database.
     */
    protected val testCommits = listOf(
        Commit(
            id = "1",
            sha = "abc123",
            date = Date(),
            message = "First commit",
            webUrl = "https://example.com/commit/abc123",
            branch = "main",
            stats = Stats(additions = 10, deletions = 5)
        ),
        Commit(
            id = "2",
            sha = "def456",
            date = Date(),
            message = "Second commit",
            webUrl = "https://example.com/commit/def456",
            branch = "main",
            stats = Stats(additions = 7, deletions = 3)
        )
    )

    /**
     * Test data that will be created in the database.
     */
    protected val testBranches = listOf(
        Branch(
            id = "1",
            branch = "main",
            active = true,
            tracksFileRenames = true,
            latestCommit = "abc123"
        ),
        Branch(
            id = "2",
            branch = "feature/new-feature",
            active = true,
            tracksFileRenames = false,
            latestCommit = "def456"
        )
    )

    /**
     * Test data that will be created in the database.
     */
    protected val testBuilds = listOf(
        Build(
            arangoId = "1",
            id = 1L,
            sha = "abc123",
            ref = "main",
            status = "success",
            tag = null,
            user = "user1",
            userFullName = "User One",
            createdAt = Date(),
            updatedAt = Date(),
            startedAt = Date(),
            finishedAt = Date(),
            committedAt = Date(),
            duration = 120,
            jobs = listOf(
                Build.Job(
                    id = "job1",
                    name = "test",
                    status = "success",
                    stage = "test",
                    createdAt = Date(),
                    finishedAt = Date(),
                    webUrl = "https://example.com/jobs/job1"
                )
            ),
            webUrl = "https://example.com/builds/1"
        ),
        Build(
            arangoId = "2",
            id = 2L,
            sha = "def456",
            ref = "feature/new-feature",
            status = "failed",
            tag = "v1.0.0",
            user = "user2",
            userFullName = "User Two",
            createdAt = Date(),
            updatedAt = Date(),
            startedAt = Date(),
            finishedAt = Date(),
            committedAt = Date(),
            duration = 180,
            jobs = listOf(
                Build.Job(
                    id = "job2",
                    name = "build",
                    status = "failed",
                    stage = "build",
                    createdAt = Date(),
                    finishedAt = Date(),
                    webUrl = "https://example.com/jobs/job2"
                )
            ),
            webUrl = "https://example.com/builds/2"
        )
    )

    /**
     * Test data that will be created in the database.
     */
    protected val testFiles = listOf(
        File(
            id = "1",
            path = "src/main/kotlin/com/example/Main.kt",
            webUrl = "https://example.com/files/Main.kt",
            maxLength = 1000
        ),
        File(
            id = "2",
            path = "src/main/kotlin/com/example/Utils.kt",
            webUrl = "https://example.com/files/Utils.kt",
            maxLength = 500
        )
    )

    /**
     * Test data that will be created in the database.
     */
    protected val testIssues = listOf(
        Issue(
            id = "1",
            iid = 101,
            title = "Fix bug in login flow",
            description = "Users are unable to log in when using special characters in their password",
            createdAt = Date(),
            updatedAt = Date(),
            closedAt = null,
            labels = listOf("bug", "high-priority"),
            state = "open",
            webUrl = "https://example.com/issues/101",
            mentions = listOf(
                Mention(
                    commit = "abc123",
                    createdAt = Date(),
                    closes = false
                )
            )
        ),
        Issue(
            id = "2",
            iid = 102,
            title = "Add new feature for user profiles",
            description = "Implement the ability for users to customize their profiles",
            createdAt = Date(),
            updatedAt = Date(),
            closedAt = Date(),
            labels = listOf("enhancement", "feature"),
            state = "closed",
            webUrl = "https://example.com/issues/102",
            mentions = listOf(
                Mention(
                    commit = "def456",
                    createdAt = Date(),
                    closes = true
                )
            )
        )
    )

    /**
     * Test data that will be created in the database.
     */
    protected val testMergeRequests = listOf(
        MergeRequest(
            id = "1",
            iid = 201,
            title = "Implement user authentication",
            description = "Add JWT-based authentication for user login",
            createdAt = Date().toString(),
            updatedAt = Date().toString(),
            closedAt = null,
            labels = listOf("feature", "security"),
            state = "open",
            webUrl = "https://example.com/merge_requests/201",
            mentions = listOf(
                Mention(
                    commit = "abc123",
                    createdAt = Date(),
                    closes = false
                )
            )
        ),
        MergeRequest(
            id = "2",
            iid = 202,
            title = "Fix CSS styling issues",
            description = "Fix responsive design issues on mobile devices",
            createdAt = Date().toString(),
            updatedAt = Date().toString(),
            closedAt = Date().toString(),
            labels = listOf("bug", "ui"),
            state = "merged",
            webUrl = "https://example.com/merge_requests/202",
            mentions = listOf(
                Mention(
                    commit = "def456",
                    createdAt = Date(),
                    closes = true
                )
            )
        )
    )

    /**
     * Test data that will be created in the database.
     */
    protected val testModules = listOf(
        Module(
            id = "1",
            path = "src/main/kotlin/com/example/core"
        ),
        Module(
            id = "2",
            path = "src/main/kotlin/com/example/api"
        )
    )

    /**
     * Test data that will be created in the database.
     */
    protected val testNotes = listOf(
        Note(
            id = "1",
            body = "This is a comment on an issue",
            createdAt = "2023-01-01T10:00:00Z",
            updatedAt = "2023-01-01T10:00:00Z",
            system = false,
            resolvable = true,
            confidential = false,
            internal = false,
            imported = false,
            importedFrom = "gitlab"
        ),
        Note(
            id = "2",
            body = "This is a system note",
            createdAt = "2023-01-02T11:00:00Z",
            updatedAt = "2023-01-02T11:00:00Z",
            system = true,
            resolvable = false,
            confidential = false,
            internal = true,
            imported = false,
            importedFrom = "github"
        )
    )

    /**
     * Test data that will be created in the database.
     */
    protected val testUsers = listOf(
        User(
            id = "1",
            gitSignature = "John Doe <john.doe@example.com>"
        ),
        User(
            id = "2",
            gitSignature = "Jane Smith <jane.smith@example.com>"
        )
    )

    /**
     * Set up test data in the database before each test.
     */
    @BeforeEach
    fun setupTestData() {
        // Clear existing data
        commitRepository.deleteAll()
        accountRepository.deleteAll()
        branchRepository.deleteAll()
        buildRepository.deleteAll()
        fileRepository.deleteAll()
        issueRepository.deleteAll()
        mergeRequestRepository.deleteAll()
        moduleRepository.deleteAll()
        noteRepository.deleteAll()
        userRepository.deleteAll()

        // Create test data
        testCommits.forEach { commit ->
            commitRepository.save(commit)
        }

        testAccounts.forEach { account ->
            accountRepository.save(account)
        }

        testBranches.forEach { branch ->
            branchRepository.save(branch)
        }

        testBuilds.forEach { build ->
            buildRepository.save(build)
        }

        testFiles.forEach { file ->
            fileRepository.save(file)
        }

        testIssues.forEach { issue ->
            issueRepository.save(issue)
        }

        testMergeRequests.forEach { mergeRequest ->
            mergeRequestRepository.save(mergeRequest)
        }

        testModules.forEach { module ->
            moduleRepository.save(module)
        }

        testNotes.forEach { note ->
            noteRepository.save(note)
        }

        testUsers.forEach { user ->
            userRepository.save(user)
        }
    }
}
