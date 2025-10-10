package com.inso_world.binocular.cli.integration

import com.inso_world.binocular.core.integration.base.InfrastructureDataSetup
import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.core.service.UserInfrastructurePort
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class TestDataSetupService(
    private val projectInfrastructurePort: ProjectInfrastructurePort,
    private val commitInfrastructurePort: CommitInfrastructurePort,
    private val repositoryInfrastructurePort: RepositoryInfrastructurePort,
//    private val accountRepository: AccountInfrastructurePort,
    private val branchInfrastructurePort: BranchInfrastructurePort,
//    private val buildRepository: BuildInfrastructurePort,
//    private val fileRepository: FileInfrastructurePort,
//    private val issueRepository: IssueInfrastructurePort,
//    private val mergeRequestRepository: MergeRequestInfrastructurePort,
//    private val moduleRepository: ModuleInfrastructurePort,
//    private val noteRepository: NoteInfrastructurePort,
    private val userRepository: UserInfrastructurePort,
//    private val milestoneRepository: MilestoneInfrastructurePort,
) {
    @Autowired
    @Lazy
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var infrastructureDataSetup: InfrastructureDataSetup

    /**
     * Clears all test data from the database.
     * This method only clears entity data, as relationship data is managed
     * by the infrastructure implementation.
     */
    @Transactional
    fun clearAllData() {
        infrastructureDataSetup.teardown()

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
        infrastructureDataSetup.setup()
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
