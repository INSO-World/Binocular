package com.inso_world.binocular.web

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
import com.inso_world.binocular.web.base.AbstractWebIntegrationTest
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile

/**
 * Base test class that sets up test data in the database.
 * This class can be extended by test classes that need database test data.
 * It uses the TestDataSetupService to manage test data independently of infrastructure.
 */
@Profile("test")
internal abstract class BaseDbTest : AbstractWebIntegrationTest() {
    @Autowired
    protected lateinit var testDataSetupService: TestDataSetupService

    @Autowired
    protected lateinit var commitRepository: CommitInfrastructurePort

    @Autowired
    protected lateinit var accountRepository: AccountInfrastructurePort

    @Autowired
    protected lateinit var branchRepository: BranchInfrastructurePort

    @Autowired
    protected lateinit var buildRepository: BuildInfrastructurePort

    @Autowired
    protected lateinit var fileRepository: FileInfrastructurePort

    @Autowired
    protected lateinit var issueRepository: IssueInfrastructurePort

    @Autowired
    protected lateinit var mergeRequestRepository: MergeRequestInfrastructurePort

    @Autowired
    protected lateinit var moduleRepository: ModuleInfrastructurePort

    @Autowired
    protected lateinit var noteRepository: NoteInfrastructurePort

    @Autowired
    protected lateinit var userRepository: UserInfrastructurePort

    @Autowired
    protected lateinit var milestoneRepository: MilestoneInfrastructurePort

    @BeforeEach
    fun setupTestData() {
        testDataSetupService.clearAllData()
        testDataSetupService.setupTestData()
    }
}
