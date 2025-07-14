package com.inso_world.binocular.web

import com.inso_world.binocular.core.integration.base.InfrastructureDataSetup
import com.inso_world.binocular.core.integration.base.TestDataProvider
import com.inso_world.binocular.core.service.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Service for setting up test data in the database.
 * This service uses only infrastructure ports to create test data,
 * making it completely independent of specific infrastructure implementations.
 *
 * Note: This service only sets up entity data. Relationships between entities
 * are handled by the infrastructure implementation itself based on the entity data
 * and any relationship methods exposed by the infrastructure ports.
 */
@Service
internal class TestDataSetupService(
    @Autowired private val infrastructureDataSetup: InfrastructureDataSetup,
//    private val testDataProvider: TestDataProvider,
    private val commitRepository: CommitInfrastructurePort,
    private val accountRepository: AccountInfrastructurePort,
    private val branchRepository: BranchInfrastructurePort,
    private val buildRepository: BuildInfrastructurePort,
    private val fileRepository: FileInfrastructurePort,
    private val issueRepository: IssueInfrastructurePort,
    private val mergeRequestRepository: MergeRequestInfrastructurePort,
    private val moduleRepository: ModuleInfrastructurePort,
    private val noteRepository: NoteInfrastructurePort,
    private val userRepository: UserInfrastructurePort,
    private val milestoneRepository: MilestoneInfrastructurePort,
) {
    /**
     * Clears all test data from the database.
     * This method only clears entity data, as relationship data is managed
     * by the infrastructure implementation.
     */
    fun clearAllData() {
        infrastructureDataSetup.teardown()

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

    /**
     * Sets up test data in the database.
     * This method creates all test entities. Relationships between entities
     * are handled by the infrastructure implementation based on the entity data.
     */
    fun setupTestData() {
        commitRepository.saveAll(TestDataProvider.testCommits)
        accountRepository.saveAll(TestDataProvider.testAccounts)
        branchRepository.saveAll(TestDataProvider.testBranches)
        buildRepository.saveAll(TestDataProvider.testBuilds)
        fileRepository.saveAll(TestDataProvider.testFiles)
        issueRepository.saveAll(TestDataProvider.testIssues)
        mergeRequestRepository.saveAll(TestDataProvider.testMergeRequests)
        milestoneRepository.saveAll(TestDataProvider.testMilestones)
        moduleRepository.saveAll(TestDataProvider.testModules)
        noteRepository.saveAll(TestDataProvider.testNotes)
        userRepository.saveAll(TestDataProvider.testUsers)
        infrastructureDataSetup.setup()
    }
}
