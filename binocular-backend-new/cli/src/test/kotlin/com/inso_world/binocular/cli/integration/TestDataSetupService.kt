package com.inso_world.binocular.cli.integration

import com.inso_world.binocular.core.service.AccountInfrastructurePort
import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.core.service.BuildInfrastructurePort
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.FileInfrastructurePort
import com.inso_world.binocular.core.service.IssueInfrastructurePort
import com.inso_world.binocular.core.service.MergeRequestInfrastructurePort
import com.inso_world.binocular.core.service.MilestoneInfrastructurePort
import com.inso_world.binocular.core.service.ModuleInfrastructurePort
import com.inso_world.binocular.core.service.NoteInfrastructurePort
import com.inso_world.binocular.core.service.UserInfrastructurePort
import org.springframework.stereotype.Service

@Service
internal class TestDataSetupService(
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
        commitRepository.saveAll(emptyList())
        accountRepository.saveAll(emptyList())
        branchRepository.saveAll(emptyList())
        buildRepository.saveAll(emptyList())
        fileRepository.saveAll(emptyList())
        issueRepository.saveAll(emptyList())
        mergeRequestRepository.saveAll(emptyList())
        milestoneRepository.saveAll(emptyList())
        moduleRepository.saveAll(emptyList())
        noteRepository.saveAll(emptyList())
        userRepository.saveAll(emptyList())
    }

    /**
     * Sets up test data in the database.
     * This method creates all test entities. Relationships between entities
     * are handled by the infrastructure implementation based on the entity data.
     */
    fun setupTestData() {
//        commitRepository.saveAll(TestDataProvider.testCommits)
//        accountRepository.saveAll(TestDataProvider.testAccounts)
//        branchRepository.saveAll(TestDataProvider.testBranches)
//        buildRepository.saveAll(TestDataProvider.testBuilds)
//        fileRepository.saveAll(TestDataProvider.testFiles)
//        issueRepository.saveAll(TestDataProvider.testIssues)
//        mergeRequestRepository.saveAll(TestDataProvider.testMergeRequests)
//        milestoneRepository.saveAll(TestDataProvider.testMilestones)
//        moduleRepository.saveAll(TestDataProvider.testModules)
//        noteRepository.saveAll(TestDataProvider.testNotes)
//        userRepository.saveAll(TestDataProvider.testUsers)
    }
}
