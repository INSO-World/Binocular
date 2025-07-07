package com.inso_world.binocular.infrastructure.arangodb

import com.inso_world.binocular.core.integration.base.BaseIntegrationTest
import com.inso_world.binocular.infrastructure.arangodb.model.edge.BranchFileConnection
import com.inso_world.binocular.infrastructure.arangodb.model.edge.CommitBuildConnection
import com.inso_world.binocular.infrastructure.arangodb.model.edge.CommitCommitConnection
import com.inso_world.binocular.infrastructure.arangodb.model.edge.CommitFileConnection
import com.inso_world.binocular.infrastructure.arangodb.model.edge.CommitFileUserConnection
import com.inso_world.binocular.infrastructure.arangodb.model.edge.CommitModuleConnection
import com.inso_world.binocular.infrastructure.arangodb.model.edge.CommitUserConnection
import com.inso_world.binocular.infrastructure.arangodb.model.edge.IssueAccountConnection
import com.inso_world.binocular.infrastructure.arangodb.model.edge.IssueCommitConnection
import com.inso_world.binocular.infrastructure.arangodb.model.edge.IssueMilestoneConnection
import com.inso_world.binocular.infrastructure.arangodb.model.edge.IssueNoteConnection
import com.inso_world.binocular.infrastructure.arangodb.model.edge.IssueUserConnection
import com.inso_world.binocular.infrastructure.arangodb.model.edge.MergeRequestAccountConnection
import com.inso_world.binocular.infrastructure.arangodb.model.edge.MergeRequestMilestoneConnection
import com.inso_world.binocular.infrastructure.arangodb.model.edge.MergeRequestNoteConnection
import com.inso_world.binocular.infrastructure.arangodb.model.edge.ModuleFileConnection
import com.inso_world.binocular.infrastructure.arangodb.model.edge.ModuleModuleConnection
import com.inso_world.binocular.infrastructure.arangodb.model.edge.NoteAccountConnection
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.IBranchFileConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.IBranchFileFileConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.ICommitBuildConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.ICommitCommitConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.ICommitFileConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.ICommitFileUserConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.ICommitModuleConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.ICommitUserConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.IIssueAccountConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.IIssueCommitConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.IIssueMilestoneConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.IIssueNoteConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.IIssueUserConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.IMergeRequestAccountConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.IMergeRequestMilestoneConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.IMergeRequestNoteConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.IModuleFileConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.IModuleModuleConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.INoteAccountConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IAccountDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IBranchDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IBuildDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.ICommitDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IFileDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IIssueDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IMergeRequestDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IMilestoneDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IModuleDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.INoteDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IUserDao
import com.inso_world.binocular.model.*
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.util.Date

/**
 * Base test class that sets up test data in the database.
 * This class can be extended by test classes that need database test data.
 */
@SpringBootTest
@AutoConfigureGraphQlTester
internal abstract class BaseDbTest : BaseIntegrationTest() {
//    @Autowired
//    private lateinit var graphQlTester: GraphQlTester

    @Autowired
    private lateinit var commitRepository: ICommitDao

    @Autowired
    private lateinit var accountRepository: IAccountDao

    @Autowired
    private lateinit var branchRepository: IBranchDao

    @Autowired
    private lateinit var buildRepository: IBuildDao

    @Autowired
    private lateinit var fileRepository: IFileDao

    @Autowired
    private lateinit var issueRepository: IIssueDao

    @Autowired
    private lateinit var mergeRequestRepository: IMergeRequestDao

    @Autowired
    private lateinit var moduleRepository: IModuleDao

    @Autowired
    private lateinit var noteRepository: INoteDao

    @Autowired
    private lateinit var userRepository: IUserDao

    @Autowired
    private lateinit var milestoneRepository: IMilestoneDao

    @Autowired
    private lateinit var branchFileConnectionRepository: IBranchFileConnectionDao

    @Autowired
    private lateinit var branchFileFileConnectionRepository: IBranchFileFileConnectionDao

    @Autowired
    private lateinit var commitBuildConnectionRepository: ICommitBuildConnectionDao

    @Autowired
    private lateinit var commitCommitConnectionRepository: ICommitCommitConnectionDao

    @Autowired
    private lateinit var commitFileConnectionRepository: ICommitFileConnectionDao

    @Autowired
    private lateinit var commitFileUserConnectionRepository: ICommitFileUserConnectionDao

    @Autowired
    private lateinit var commitModuleConnectionRepository: ICommitModuleConnectionDao

    @Autowired
    private lateinit var commitUserConnectionRepository: ICommitUserConnectionDao

    @Autowired
    private lateinit var issueAccountConnectionRepository: IIssueAccountConnectionDao

    @Autowired
    private lateinit var issueCommitConnectionRepository: IIssueCommitConnectionDao

    @Autowired
    private lateinit var issueMilestoneConnectionRepository: IIssueMilestoneConnectionDao

    @Autowired
    private lateinit var issueNoteConnectionRepository: IIssueNoteConnectionDao

    @Autowired
    private lateinit var issueUserConnectionRepository: IIssueUserConnectionDao

    @Autowired
    private lateinit var mergeRequestAccountConnectionRepository: IMergeRequestAccountConnectionDao

    @Autowired
    private lateinit var mergeRequestMilestoneConnectionRepository: IMergeRequestMilestoneConnectionDao

    @Autowired
    private lateinit var mergeRequestNoteConnectionRepository: IMergeRequestNoteConnectionDao

    @Autowired
    private lateinit var moduleFileConnectionRepository: IModuleFileConnectionDao

    @Autowired
    private lateinit var moduleModuleConnectionRepository: IModuleModuleConnectionDao

    @Autowired
    private lateinit var noteAccountConnectionRepository: INoteAccountConnectionDao

    protected val testAccounts =
        listOf(
            Account("1", Platform.GitHub, "user1", "User One", "https://example.com/avatars/user1.png", "https://github.com/user1"),
            Account("2", Platform.GitLab, "user2", "User Two", "https://example.com/avatars/user2.png", "https://gitlab.com/user2"),
        )

    protected val testCommits =
        listOf(
            Commit("1", "abc123", Date(), "First commit", "https://example.com/commit/abc123", "main", Stats(10, 5)),
            Commit("2", "def456", Date(), "Second commit", "https://example.com/commit/def456", "main", Stats(7, 3)),
        )

    protected val testBranches =
        listOf(
            Branch("1", "main", true, true, "abc123"),
            Branch("2", "feature/new-feature", true, false, "def456"),
        )

    protected val testBuilds =
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
                    Build.Job("job1", "test", "success", "test", LocalDateTime.now(), LocalDateTime.now(), "https://example.com/jobs/job1"),
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
                    Build.Job(
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

    protected val testFiles =
        listOf(
            File("1", "src/main/kotlin/com/example/Main.kt", "https://example.com/files/Main.kt", 1000),
            File("2", "src/main/kotlin/com/example/Utils.kt", "https://example.com/files/Utils.kt", 500),
        )

    protected val testIssues =
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
                listOf(Mention("abc123", LocalDateTime.now(), false)),
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
                listOf(Mention("def456", LocalDateTime.now(), true)),
            ),
        )

    protected val testMergeRequests =
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
                listOf(Mention("abc123", LocalDateTime.now(), false)),
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
                listOf(Mention("def456", LocalDateTime.now(), true)),
            ),
        )

    protected val testModules =
        listOf(
            Module("1", "src/main/kotlin/com/example/core"),
            Module("2", "src/main/kotlin/com/example/api"),
        )

    protected val testNotes =
        listOf(
            Note("1", "This is a comment", "2023-01-01T10:00:00Z", "2023-01-01T10:00:00Z", false, true, false, false, false, "gitlab"),
            Note("2", "This is a system note", "2023-01-02T11:00:00Z", "2023-01-02T11:00:00Z", true, false, false, true, false, "github"),
        )

    protected val testUsers =
        listOf(
            User("1", "John Doe <john.doe@example.com>"),
            User("2", "Jane Smith <jane.smith@example.com>"),
        )

    protected val testMilestones =
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

    @BeforeEach
    fun setupTestData() {
        clearAllData()
        createBasicEntities()
        createEntityRelationships()
    }

    private fun clearAllData() {
        // First clear all edge connections
        branchFileConnectionRepository.deleteAll()
        branchFileFileConnectionRepository.deleteAll()
        commitBuildConnectionRepository.deleteAll()
        commitCommitConnectionRepository.deleteAll()
        commitFileConnectionRepository.deleteAll()
        commitFileUserConnectionRepository.deleteAll()
        commitModuleConnectionRepository.deleteAll()
        commitUserConnectionRepository.deleteAll()
        issueAccountConnectionRepository.deleteAll()
        issueCommitConnectionRepository.deleteAll()
        issueMilestoneConnectionRepository.deleteAll()
        issueNoteConnectionRepository.deleteAll()
        issueUserConnectionRepository.deleteAll()
        mergeRequestAccountConnectionRepository.deleteAll()
        mergeRequestMilestoneConnectionRepository.deleteAll()
        mergeRequestNoteConnectionRepository.deleteAll()
        moduleFileConnectionRepository.deleteAll()
        moduleModuleConnectionRepository.deleteAll()
        noteAccountConnectionRepository.deleteAll()

        // Then clear all entities
        commitRepository.deleteAll()
        accountRepository.deleteAll()
        branchRepository.deleteAll()
        buildRepository.deleteAll()
        fileRepository.deleteAll()
        issueRepository.deleteAll()
        mergeRequestRepository.deleteAll()
        milestoneRepository.deleteAll()
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
        milestoneRepository.saveAll(testMilestones)
        moduleRepository.saveAll(testModules)
        noteRepository.saveAll(testNotes)
        userRepository.saveAll(testUsers)
    }

    private fun createEntityRelationships() {
        // Branch relationships
        connectBranchesToFiles()

        // Commit relationships
        connectCommitsToBuilds()
        connectCommitsToCommits()
        connectCommitsToFiles()
        connectCommitsToModules()
        connectCommitsToUsers()

        // Issue relationships
        connectIssuesToAccounts()
        connectIssuesToCommits()
        connectIssuesToMilestones()
        connectIssuesToNotes()
        connectIssuesToUsers()

        // MergeRequest relationships
        connectMergeRequestsToAccounts()
        connectMergeRequestsToMilestones()
        connectMergeRequestsToNotes()

        // Module relationships
        connectModulesToFiles()
        connectModulesToModules()

        // Note relationships
        connectNotesToAccounts()

        // User relationships
        connectCommitFilesToUsers()
    }

    private fun connectBranchesToFiles() {
        val branch1 = testBranches[0]
        val branch2 = testBranches[1]

        val branch1Id = branch1.id ?: throw IllegalStateException("Branch ID cannot be null")
        val branch2Id = branch2.id ?: throw IllegalStateException("Branch ID cannot be null")

        // Branch 1 -> File 1 and File 2
        testFiles.forEach { file ->
            val fileId = file.id ?: throw IllegalStateException("File ID cannot be null")
            val connection =
                BranchFileConnection(
                    id = "branch_${branch1Id}_file_$fileId", // <-- Set id here (equals to _key)
                    from = branch1,
                    to = file,
                )
            branchFileConnectionRepository.save(connection)
        }

        // Branch 2 -> File 2 only
        val file2Id = testFiles[1].id ?: throw IllegalStateException("File ID cannot be null")
        val connection =
            BranchFileConnection(
                id = "branch_${branch2Id}_file_$file2Id", // <-- Set id here too
                from = branch2,
                to = testFiles[1],
            )
        branchFileConnectionRepository.save(connection)
    }

    private fun connectCommitsToBuilds() {
        val commit1 = testCommits[0]
        val build1 = testBuilds[0]

        val commit1Id = commit1.id ?: throw IllegalStateException("Commit ID cannot be null")
        val build1Id = build1.id ?: throw IllegalStateException("Build ID cannot be null")

        // Commit 1 -> Build 1
        val connection =
            CommitBuildConnection(
                id = "commit_${commit1Id}_build_$build1Id",
                from = commit1,
                to = build1,
            )
        commitBuildConnectionRepository.save(connection)
    }

    private fun connectCommitsToCommits() {
        val commit1 = testCommits[0]
        val commit2 = testCommits[1]

        val commit1Id = commit1.id ?: throw IllegalStateException("Commit ID cannot be null")
        val commit2Id = commit2.id ?: throw IllegalStateException("Commit ID cannot be null")

        // Commit 2 -> Commit 1 (Commit 1 is parent of Commit 2)
        val connection =
            CommitCommitConnection(
                id = "commit_${commit2Id}_commit_$commit1Id",
                from = commit2,
                to = commit1,
            )
        commitCommitConnectionRepository.save(connection)
    }

    private fun connectCommitsToFiles() {
        val commit1 = testCommits[0]
        val file1 = testFiles[0]

        val commit1Id = commit1.id ?: throw IllegalStateException("Commit ID cannot be null")
        val file1Id = file1.id ?: throw IllegalStateException("File ID cannot be null")

        // Commit 1 -> File 1
        val connection =
            CommitFileConnection(
                id = "commit_${commit1Id}_file_$file1Id",
                from = commit1,
                to = file1,
                lineCount = 100,
            )
        commitFileConnectionRepository.save(connection)
    }

    private fun connectCommitsToModules() {
        val commit1 = testCommits[0]
        val module1 = testModules[0]

        val commit1Id = commit1.id ?: throw IllegalStateException("Commit ID cannot be null")
        val module1Id = module1.id ?: throw IllegalStateException("Module ID cannot be null")

        // Commit 1 -> Module 1
        val connection =
            CommitModuleConnection(
                id = "commit_${commit1Id}_module_$module1Id",
                from = commit1,
                to = module1,
            )
        commitModuleConnectionRepository.save(connection)
    }

    private fun connectCommitsToUsers() {
        val commit1 = testCommits[0]
        val user1 = testUsers[0]

        val commit1Id = commit1.id ?: throw IllegalStateException("Commit ID cannot be null")
        val user1Id = user1.id ?: throw IllegalStateException("User ID cannot be null")

        // Commit 1 -> User 1
        val connection =
            CommitUserConnection(
                id = "commit_${commit1Id}_user_$user1Id",
                from = commit1,
                to = user1,
            )
        commitUserConnectionRepository.save(connection)
    }

    private fun connectIssuesToAccounts() {
        val issue1 = testIssues[0]
        val account1 = testAccounts[0]

        val issue1Id = issue1.id ?: throw IllegalStateException("Issue ID cannot be null")
        val account1Id = account1.id ?: throw IllegalStateException("Account ID cannot be null")

        // Issue 1 -> Account 1
        val connection =
            IssueAccountConnection(
                id = "issue_${issue1Id}_account_$account1Id",
                from = issue1,
                to = account1,
            )
        issueAccountConnectionRepository.save(connection)
    }

    private fun connectIssuesToCommits() {
        val issue1 = testIssues[0]
        val commit1 = testCommits[0]

        val issue1Id = issue1.id ?: throw IllegalStateException("Issue ID cannot be null")
        val commit1Id = commit1.id ?: throw IllegalStateException("Commit ID cannot be null")

        // Issue 1 -> Commit 1
        val connection =
            IssueCommitConnection(
                id = "issue_${issue1Id}_commit_$commit1Id",
                from = issue1,
                to = commit1,
            )
        issueCommitConnectionRepository.save(connection)
    }

    private fun connectIssuesToMilestones() {
        val issue1 = testIssues[0]
        val milestone1 = testMilestones[0]

        val issue1Id = issue1.id ?: throw IllegalStateException("Issue ID cannot be null")
        val milestone1Id = milestone1.id ?: throw IllegalStateException("Milestone ID cannot be null")

        // Issue 1 -> Milestone 1
        val connection =
            IssueMilestoneConnection(
                id = "issue_${issue1Id}_milestone_$milestone1Id",
                from = issue1,
                to = milestone1,
            )
        issueMilestoneConnectionRepository.save(connection)
    }

    private fun connectIssuesToNotes() {
        val issue1 = testIssues[0]
        val note1 = testNotes[0]

        val issue1Id = issue1.id ?: throw IllegalStateException("Issue ID cannot be null")
        val note1Id = note1.id ?: throw IllegalStateException("Note ID cannot be null")

        // Issue 1 -> Note 1
        val connection =
            IssueNoteConnection(
                id = "issue_${issue1Id}_note_$note1Id",
                from = issue1,
                to = note1,
            )
        issueNoteConnectionRepository.save(connection)
    }

    private fun connectIssuesToUsers() {
        val issue1 = testIssues[0]
        val user1 = testUsers[0]

        val issue1Id = issue1.id ?: throw IllegalStateException("Issue ID cannot be null")
        val user1Id = user1.id ?: throw IllegalStateException("User ID cannot be null")

        // Issue 1 -> User 1
        val connection =
            IssueUserConnection(
                id = "issue_${issue1Id}_user_$user1Id",
                from = issue1,
                to = user1,
            )
        issueUserConnectionRepository.save(connection)
    }

    private fun connectMergeRequestsToAccounts() {
        val mergeRequest1 = testMergeRequests[0]
        val account1 = testAccounts[0]

        val mergeRequest1Id = mergeRequest1.id ?: throw IllegalStateException("MergeRequest ID cannot be null")
        val account1Id = account1.id ?: throw IllegalStateException("Account ID cannot be null")

        // MergeRequest 1 -> Account 1
        val connection =
            MergeRequestAccountConnection(
                id = "mergeRequest_${mergeRequest1Id}_account_$account1Id",
                from = mergeRequest1,
                to = account1,
            )
        mergeRequestAccountConnectionRepository.save(connection)
    }

    private fun connectMergeRequestsToMilestones() {
        val mergeRequest1 = testMergeRequests[0]
        val milestone1 = testMilestones[0]

        val mergeRequest1Id = mergeRequest1.id ?: throw IllegalStateException("MergeRequest ID cannot be null")
        val milestone1Id = milestone1.id ?: throw IllegalStateException("Milestone ID cannot be null")

        // MergeRequest 1 -> Milestone 1
        val connection =
            MergeRequestMilestoneConnection(
                id = "mergeRequest_${mergeRequest1Id}_milestone_$milestone1Id",
                from = mergeRequest1,
                to = milestone1,
            )
        mergeRequestMilestoneConnectionRepository.save(connection)
    }

    private fun connectMergeRequestsToNotes() {
        val mergeRequest1 = testMergeRequests[0]
        val note1 = testNotes[0]

        val mergeRequest1Id = mergeRequest1.id ?: throw IllegalStateException("MergeRequest ID cannot be null")
        val note1Id = note1.id ?: throw IllegalStateException("Note ID cannot be null")

        // MergeRequest 1 -> Note 1
        val connection =
            MergeRequestNoteConnection(
                id = "mergeRequest_${mergeRequest1Id}_note_$note1Id",
                from = mergeRequest1,
                to = note1,
            )
        mergeRequestNoteConnectionRepository.save(connection)
    }

    private fun connectModulesToFiles() {
        val module1 = testModules[0]
        val file1 = testFiles[0]

        val module1Id = module1.id ?: throw IllegalStateException("Module ID cannot be null")
        val file1Id = file1.id ?: throw IllegalStateException("File ID cannot be null")

        // Module 1 -> File 1
        val connection =
            ModuleFileConnection(
                id = "module_${module1Id}_file_$file1Id",
                from = module1,
                to = file1,
            )
        moduleFileConnectionRepository.save(connection)
    }

    private fun connectModulesToModules() {
        val module1 = testModules[0]
        val module2 = testModules[1]

        val module1Id = module1.id ?: throw IllegalStateException("Module ID cannot be null")
        val module2Id = module2.id ?: throw IllegalStateException("Module ID cannot be null")

        // Module 1 -> Module 2 (Module 2 is a child of Module 1)
        val connection =
            ModuleModuleConnection(
                id = "module_${module1Id}_module_$module2Id",
                from = module1,
                to = module2,
            )
        moduleModuleConnectionRepository.save(connection)
    }

    private fun connectNotesToAccounts() {
        val note1 = testNotes[0]
        val account1 = testAccounts[0]

        val note1Id = note1.id ?: throw IllegalStateException("Note ID cannot be null")
        val account1Id = account1.id ?: throw IllegalStateException("Account ID cannot be null")

        // Note 1 -> Account 1
        val connection =
            NoteAccountConnection(
                id = "note_${note1Id}_account_$account1Id",
                from = note1,
                to = account1,
            )
        noteAccountConnectionRepository.save(connection)
    }

    private fun connectCommitFilesToUsers() {
        val commit1 = testCommits[0]
        val file1 = testFiles[0]
        val user1 = testUsers[0]

        val file1Id = file1.id ?: throw IllegalStateException("File ID cannot be null")
        val user1Id = user1.id ?: throw IllegalStateException("User ID cannot be null")

        // Create a CommitFileUserConnection
        val connection =
            CommitFileUserConnection(
                id = "file_${file1Id}_user_$user1Id",
                from = file1, // from File
                to = user1, // to User
            )
        commitFileUserConnectionRepository.save(connection)
    }
}
