package com.inso_world.binocular.infrastructure.sql.integration

import com.inso_world.binocular.infrastructure.sql.service.BranchInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.sql.service.CommitInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.sql.service.ProjectInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.sql.service.RepositoryInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.sql.service.UserInfrastructurePortImpl
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class SqlInfrastructureTestDataSetupService(
    private val projectRepository: ProjectInfrastructurePortImpl,
    private val commitRepository: CommitInfrastructurePortImpl,
    private val repositoryRepository: RepositoryInfrastructurePortImpl,
//    private val accountRepository: AccountInfrastructurePort,
    private val branchPort: BranchInfrastructurePortImpl,
//    private val buildRepository: BuildInfrastructurePort,
//    private val fileRepository: FileInfrastructurePort,
//    private val issueRepository: IssueInfrastructurePort,
//    private val mergeRequestRepository: MergeRequestInfrastructurePort,
//    private val moduleRepository: ModuleInfrastructurePort,
//    private val noteRepository: NoteInfrastructurePort,
    private val userRepository: UserInfrastructurePortImpl,
//    private val milestoneRepository: MilestoneInfrastructurePort,
) {
    /**
     * Clears all test data from the database.
     * This method only clears entity data, as relationship data is managed
     * by the infrastructure implementation.
     */
    @Transactional
    fun clearAllData() {
        projectRepository.deleteAll()
        repositoryRepository.deleteAll()
        userRepository.deleteAll()
        branchPort.deleteAll()
        commitRepository.deleteAll()

//        accountRepository.saveAll(emptyList())
//        branchRepository.saveAll(emptyList())
//        buildRepository.saveAll(emptyList())
//        fileRepository.saveAll(emptyList())
//        issueRepository.saveAll(emptyList())
//        mergeRequestRepository.saveAll(emptyList())
//        milestoneRepository.saveAll(emptyList())
//        moduleRepository.saveAll(emptyList())
//        noteRepository.saveAll(emptyList())
//        userRepository.saveAll(emptyList())
//        userRepository.findAll().map { userRepository.delete(it) }
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
