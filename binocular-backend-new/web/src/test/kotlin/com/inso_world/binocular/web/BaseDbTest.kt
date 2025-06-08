package com.inso_world.binocular.web

import com.inso_world.binocular.web.entity.*
import com.inso_world.binocular.web.entity.edge.BranchFileConnection
import com.inso_world.binocular.web.persistence.repository.arangodb.*
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.BranchFileConnectionRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester
import org.springframework.boot.test.context.SpringBootTest
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

    @Autowired
    protected lateinit var branchFileConnectionRepository: BranchFileConnectionRepository

    protected val testAccounts = listOf(
        Account("1", Platform.GitHub, "user1", "User One", "https://example.com/avatars/user1.png", "https://github.com/user1"),
        Account("2", Platform.GitLab, "user2", "User Two", "https://example.com/avatars/user2.png", "https://gitlab.com/user2")
    )

    protected val testCommits = listOf(
        Commit("1", "abc123", Date(), "First commit", "https://example.com/commit/abc123", "main", Stats(10, 5)),
        Commit("2", "def456", Date(), "Second commit", "https://example.com/commit/def456", "main", Stats(7, 3))
    )

    protected val testBranches = listOf(
        Branch("1", "main", true, true, "abc123"),
        Branch("2", "feature/new-feature", true, false, "def456")
    )

    protected val testBuilds = listOf(
        Build(
            "1", "abc123", "main", "success", null, "user1", "User One",
            Date(), Date(), Date(), Date(), Date(), 120, listOf(
                Build.Job("job1", "test", "success", "test", Date(), Date(), "https://example.com/jobs/job1")
            ),
            "https://example.com/builds/1"
        ),
        Build(
            "2", "def456", "feature/new-feature", "failed", "v1.0.0", "user2", "User Two",
            Date(), Date(), Date(), Date(), Date(), 180, listOf(
                Build.Job("job2", "build", "failed", "build", Date(), Date(), "https://example.com/jobs/job2")
            ),
            "https://example.com/builds/2"
        )
    )

    protected val testFiles = listOf(
        File("1", "src/main/kotlin/com/example/Main.kt", "https://example.com/files/Main.kt", 1000),
        File("2", "src/main/kotlin/com/example/Utils.kt", "https://example.com/files/Utils.kt", 500)
    )

    protected val testIssues = listOf(
        Issue("1", 101, "Fix bug in login flow", "Users are unable to log in...", Date(), Date(), null, listOf("bug", "high-priority"), "open", "https://example.com/issues/101",
            listOf(Mention("abc123", Date(), false))),
        Issue("2", 102, "Add new feature", "Implement profile customization", Date(), Date(), Date(), listOf("enhancement", "feature"), "closed", "https://example.com/issues/102",
            listOf(Mention("def456", Date(), true)))
    )

    protected val testMergeRequests = listOf(
        MergeRequest("1", 201, "Implement user authentication", "Add JWT auth", Date().toString(), Date().toString(), null, listOf("feature", "security"), "open", "https://example.com/merge_requests/201",
            listOf(Mention("abc123", Date(), false))),
        MergeRequest("2", 202, "Fix CSS", "Fix responsive design", Date().toString(), Date().toString(), Date().toString(), listOf("bug", "ui"), "merged", "https://example.com/merge_requests/202",
            listOf(Mention("def456", Date(), true)))
    )

    protected val testModules = listOf(
        Module("1", "src/main/kotlin/com/example/core"),
        Module("2", "src/main/kotlin/com/example/api")
    )

    protected val testNotes = listOf(
        Note("1", "This is a comment", "2023-01-01T10:00:00Z", "2023-01-01T10:00:00Z", false, true, false, false, false, "gitlab"),
        Note("2", "This is a system note", "2023-01-02T11:00:00Z", "2023-01-02T11:00:00Z", true, false, false, true, false, "github")
    )

    protected val testUsers = listOf(
        User("1", "John Doe <john.doe@example.com>"),
        User("2", "Jane Smith <jane.smith@example.com>")
    )

    @BeforeEach
    fun setupTestData() {
        clearAllData()
        createBasicEntities()
        createEntityRelationships()
    }

    private fun clearAllData() {
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
    }

    private fun createBasicEntities() {
        commitRepository.saveAll(testCommits)
        accountRepository.saveAll(testAccounts)
        branchRepository.saveAll(testBranches)
        buildRepository.saveAll(testBuilds)
        fileRepository.saveAll(testFiles)
        issueRepository.saveAll(testIssues)
        mergeRequestRepository.saveAll(testMergeRequests)
        moduleRepository.saveAll(testModules)
        noteRepository.saveAll(testNotes)
        userRepository.saveAll(testUsers)
    }

    private fun createEntityRelationships() {
        connectBranchesToFiles()
        // Other relationships can be added here
    }

    private fun connectBranchesToFiles() {
        val branch1 = testBranches[0]
        val branch2 = testBranches[1]

        val branch1Id = branch1.id ?: throw IllegalStateException("Branch ID cannot be null")
        val branch2Id = branch2.id ?: throw IllegalStateException("Branch ID cannot be null")

        // Branch 1 -> File 1 and File 2
        testFiles.forEach { file ->
            val fileId = file.id ?: throw IllegalStateException("File ID cannot be null")
            val connection = BranchFileConnection(
                id = "branch_${branch1Id}_file_${fileId}", // <-- Set id here (equals to _key)
                from = branch1,
                to = file
            )
            branchFileConnectionRepository.save(connection)
        }

        // Branch 2 -> File 2 only
        val file2Id = testFiles[1].id ?: throw IllegalStateException("File ID cannot be null")
        val connection = BranchFileConnection(
            id = "branch_${branch2Id}_file_${file2Id}", // <-- Set id here too
            from = branch2,
            to = testFiles[1]
        )
        branchFileConnectionRepository.save(connection)
    }


}
