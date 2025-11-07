package com.inso_world.binocular.infrastructure.sql

import com.inso_world.binocular.core.data.MockTestDataProvider
import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.integration.base.InfrastructureDataSetup
import com.inso_world.binocular.infrastructure.sql.service.BranchInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.sql.service.CommitInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.sql.service.ProjectInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.sql.service.RepositoryInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.sql.service.UserInfrastructurePortImpl
import org.springframework.stereotype.Component

@Component
internal class SqlInfrastructureDataSetup(
    private val projectInfrastructurePort: ProjectInfrastructurePortImpl,
    private val commitInfrastructurePort: CommitInfrastructurePortImpl,
    private val repositoryInfrastructurePort: RepositoryInfrastructurePortImpl,
//    private val accountRepository: AccountInfrastructurePort,
    private val branchInfrastructurePort: BranchInfrastructurePortImpl,
//    private val buildRepository: BuildInfrastructurePort,
//    private val fileRepository: FileInfrastructurePort,
//    private val issueRepository: IssueInfrastructurePort,
//    private val mergeRequestRepository: MergeRequestInfrastructurePort,
//    private val moduleRepository: ModuleInfrastructurePort,
//    private val noteRepository: NoteInfrastructurePort,
    private val userPort: UserInfrastructurePortImpl,
//    private val milestoneRepository: MilestoneInfrastructurePort,
) : InfrastructureDataSetup {

    private lateinit var mockTestData: MockTestDataProvider
    companion object {
        private val logger by logger()
    }

    override fun setup() {
        logger.info(">>> SqlInfrastructureDataSetup setup")
        this.mockTestData = MockTestDataProvider()

        try { commitInfrastructurePort.deleteAll() } catch (ex: Exception) { logger.warn("teardown commit failed: ${'$'}{ex.message}") }
        try { userPort.deleteAll() } catch (ex: Exception) { logger.warn("teardown user failed: ${'$'}{ex.message}") }
        try { branchInfrastructurePort.deleteAll() } catch (ex: Exception) { logger.warn("teardown branch failed: ${'$'}{ex.message}") }
        try { repositoryInfrastructurePort.deleteAll() } catch (ex: Exception) { logger.warn("teardown repo failed: ${'$'}{ex.message}") }
        try { projectInfrastructurePort.deleteAll() } catch (ex: Exception) { logger.warn("teardown project failed: ${'$'}{ex.message}") }

        projectInfrastructurePort.saveAll(mockTestData.testProjects)
//        repositoryInfrastructurePort.saveAll(mockTestData.testRepositories)

//        commitInfrastructurePort.saveAll(TestDataProvider.testCommits)

        logger.info("<<< SqlInfrastructureDataSetup setup")
    }

    override fun teardown() {
        logger.info(">>> SqlInfrastructureDataSetup teardown")

        try { commitInfrastructurePort.deleteAll() } catch (ex: Exception) { logger.warn("teardown commit failed: ${'$'}{ex.message}") }
        try { userPort.deleteAll() } catch (ex: Exception) { logger.warn("teardown user failed: ${'$'}{ex.message}") }
        try { branchInfrastructurePort.deleteAll() } catch (ex: Exception) { logger.warn("teardown branch failed: ${'$'}{ex.message}") }
        try { repositoryInfrastructurePort.deleteAll() } catch (ex: Exception) { logger.warn("teardown repo failed: ${'$'}{ex.message}") }
        try { projectInfrastructurePort.deleteAll() } catch (ex: Exception) { logger.warn("teardown project failed: ${'$'}{ex.message}") }

        logger.info("<<< SqlInfrastructureDataSetup teardown")
    }
}
