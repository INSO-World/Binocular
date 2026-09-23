package com.inso_world.binocular.infrastructure.sql.unit.mapper.base

import com.inso_world.binocular.core.unit.base.BaseUnitTest
import com.inso_world.binocular.infrastructure.sql.mapper.AccountMapper
import com.inso_world.binocular.infrastructure.sql.mapper.BranchMapper
import com.inso_world.binocular.infrastructure.sql.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.sql.mapper.DeveloperMapper
import com.inso_world.binocular.infrastructure.sql.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.sql.mapper.RemoteMapper
import com.inso_world.binocular.infrastructure.sql.mapper.RepositoryMapper
import io.mockk.spyk
import org.junit.jupiter.api.BeforeEach

internal open class BaseMapperTest : BaseUnitTest() {
    lateinit var projectMapper: ProjectMapper
    lateinit var repositoryMapper: RepositoryMapper
    lateinit var branchMapper: BranchMapper
    lateinit var remoteMapper: RemoteMapper
    lateinit var developerMapper: DeveloperMapper
    lateinit var commitMapper: CommitMapper
    lateinit var accountMapper: AccountMapper

    @BeforeEach
    fun setUp() {
        accountMapper = spyk(AccountMapper())
        commitMapper = spyk(CommitMapper())
        branchMapper = spyk(BranchMapper())
        remoteMapper = spyk(RemoteMapper())
        developerMapper = spyk(DeveloperMapper())
        repositoryMapper = spyk(RepositoryMapper())
        projectMapper = spyk(ProjectMapper())
    }
}
