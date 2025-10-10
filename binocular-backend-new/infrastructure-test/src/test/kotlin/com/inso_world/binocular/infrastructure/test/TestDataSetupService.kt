package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.integration.base.InfrastructureDataSetup
import com.inso_world.binocular.core.integration.base.TestDataProvider
import com.inso_world.binocular.core.service.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
internal class TestDataSetupService(
    @Autowired private val infrastructureDataSetup: InfrastructureDataSetup,
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
    private val projectRepository: ProjectInfrastructurePort,
    private val repositoryRepository: RepositoryInfrastructurePort,
) {
    fun clearAllData() {
        infrastructureDataSetup.teardown()

        // entities
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
        repositoryRepository.deleteAll()
        projectRepository.deleteAll()
    }

    fun setupTestData() {
        // order: create parents first where necessary
        projectRepository.saveAll(TestDataProvider.testProjects)
        repositoryRepository.saveAll(TestDataProvider.testRepositories)

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

        // relationships handled by infrastructure-specific setup
        infrastructureDataSetup.setup()
    }
}
