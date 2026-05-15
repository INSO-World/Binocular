package com.inso_world.binocular.infrastructure.sql.unit.mapper.base

import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.core.unit.base.BaseUnitTest
import com.inso_world.binocular.infrastructure.sql.mapper.AccountMapper
import com.inso_world.binocular.infrastructure.sql.mapper.BranchMapper
import com.inso_world.binocular.infrastructure.sql.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.sql.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.sql.mapper.RemoteMapper
import com.inso_world.binocular.infrastructure.sql.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.sql.mapper.UserMapper
import io.mockk.spyk
import org.junit.jupiter.api.BeforeEach
import org.springframework.data.util.ReflectionUtils.setField

internal open class BaseMapperTest : BaseUnitTest() {
    lateinit var ctx: MappingContext
    lateinit var projectMapper: ProjectMapper
    lateinit var repositoryMapper: RepositoryMapper
    lateinit var branchMapper: BranchMapper
    lateinit var remoteMapper: RemoteMapper
    lateinit var userMapper: UserMapper
    lateinit var commitMapper: CommitMapper
    lateinit var accountMapper: AccountMapper

    @BeforeEach
    fun setUp() {
        ctx = spyk(MappingContext())

        accountMapper = spyk(AccountMapper())
        commitMapper = spyk(CommitMapper())
        branchMapper = spyk(BranchMapper())
        remoteMapper = spyk(RemoteMapper())
        userMapper = spyk(UserMapper())
        repositoryMapper = spyk(RepositoryMapper())


        projectMapper = spyk(ProjectMapper())

        // wire up projectMapper
        with(projectMapper) {
            setField(
                this.javaClass.getDeclaredField("ctx"),
                this,
                ctx
            )
        }

        // wire up repositoryMapper
        with(repositoryMapper) {
            setField(
                this.javaClass.getDeclaredField("ctx"),
                this,
                ctx
            )
//            setField(
//                this.javaClass.getDeclaredField("projectMapper"),
//                this,
//                projectMapper
//            )
        }

        // wire up commitMapper
        with(commitMapper) {
            setField(
                this.javaClass.getDeclaredField("ctx"),
                this,
                ctx
            )
            setField(
                this.javaClass.getDeclaredField("userMapper"),
                this,
                userMapper
            )
        }

        // wire up branchMapper
        with(branchMapper) {
            setField(
                this.javaClass.getDeclaredField("ctx"),
                this,
                ctx
            )
        }

        // wire up remoteMapper
        with(remoteMapper) {
            setField(
                this.javaClass.getDeclaredField("ctx"),
                this,
                ctx
            )
        }

        // wire up userMapper
        with(userMapper) {
            setField(
                this.javaClass.getDeclaredField("ctx"),
                this,
                ctx
            )
        }

        // wire up accountMapper
        with(accountMapper) {
            setField(
                this.javaClass.getDeclaredField("ctx"),
                this,
                ctx
            )
        }
    }
}
