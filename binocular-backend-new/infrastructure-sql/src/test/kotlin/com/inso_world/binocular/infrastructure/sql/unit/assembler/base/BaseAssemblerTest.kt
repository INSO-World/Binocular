package com.inso_world.binocular.infrastructure.sql.unit.assembler.base

import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.core.unit.base.BaseUnitTest
import com.inso_world.binocular.infrastructure.sql.assembler.ProjectAssembler
import com.inso_world.binocular.infrastructure.sql.assembler.RepositoryAssembler
import com.inso_world.binocular.infrastructure.sql.mapper.BranchMapper
import com.inso_world.binocular.infrastructure.sql.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.sql.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.sql.mapper.RemoteMapper
import com.inso_world.binocular.infrastructure.sql.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.sql.mapper.UserMapper
import io.mockk.spyk
import org.junit.jupiter.api.BeforeEach
import org.springframework.data.util.ReflectionUtils.setField

/**
 * Base test class for assembler unit tests.
 *
 * Provides common test infrastructure including:
 * - Mocked MappingContext with spy capabilities
 * - All mappers (Project, Repository, Commit, Branch, User) with spy capabilities
 * - All assemblers (Project, Repository) with spy capabilities
 * - Proper dependency wiring between components
 *
 * Extends [BaseUnitTest] to inherit standard unit test configuration.
 */
internal open class BaseAssemblerTest : BaseUnitTest() {
    lateinit var ctx: MappingContext

    // Mappers
    lateinit var projectMapper: ProjectMapper
    lateinit var repositoryMapper: RepositoryMapper
    lateinit var commitMapper: CommitMapper
    lateinit var branchMapper: BranchMapper
    lateinit var remoteMapper: RemoteMapper
    lateinit var userMapper: UserMapper

    // Assemblers
    lateinit var projectAssembler: ProjectAssembler
    lateinit var repositoryAssembler: RepositoryAssembler

    @BeforeEach
    fun setUpAssemblerTest() {
        // Create MappingContext spy
        ctx = spyk(MappingContext())

        // Create mapper spies
        commitMapper = spyk(CommitMapper())
        branchMapper = spyk(BranchMapper())
        remoteMapper = spyk(RemoteMapper())
        userMapper = spyk(UserMapper())
        repositoryMapper = spyk(RepositoryMapper())
        projectMapper = spyk(ProjectMapper())

        // Create assembler spies
        repositoryAssembler = spyk(RepositoryAssembler())
        projectAssembler = spyk(ProjectAssembler())

        // Wire up projectMapper
        with(projectMapper) {
            setField(
                this.javaClass.getDeclaredField("ctx"),
                this,
                ctx
            )
        }

        // Wire up repositoryMapper
        with(repositoryMapper) {
            setField(
                this.javaClass.getDeclaredField("ctx"),
                this,
                ctx
            )
        }

        // Wire up commitMapper
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

        // Wire up branchMapper
        with(branchMapper) {
            setField(
                this.javaClass.getDeclaredField("ctx"),
                this,
                ctx
            )
        }

        // Wire up remoteMapper
        with(remoteMapper) {
            setField(
                this.javaClass.getDeclaredField("ctx"),
                this,
                ctx
            )
        }

        // Wire up userMapper
        with(userMapper) {
            setField(
                this.javaClass.getDeclaredField("ctx"),
                this,
                ctx
            )
        }

        // Wire up repositoryAssembler
        with(repositoryAssembler) {
            setField(
                this.javaClass.getDeclaredField("ctx"),
                this,
                ctx
            )
            setField(
                this.javaClass.getDeclaredField("repositoryMapper"),
                this,
                repositoryMapper
            )
            setField(
                this.javaClass.getDeclaredField("commitMapper"),
                this,
                commitMapper
            )
            setField(
                this.javaClass.getDeclaredField("branchMapper"),
                this,
                branchMapper
            )
            setField(
                this.javaClass.getDeclaredField("remoteMapper"),
                this,
                remoteMapper
            )
            setField(
                this.javaClass.getDeclaredField("userMapper"),
                this,
                userMapper
            )
            setField(
                this.javaClass.getDeclaredField("projectMapper"),
                this,
                projectMapper
            )
        }

        // Wire up projectAssembler
        with(projectAssembler) {
            setField(
                this.javaClass.getDeclaredField("ctx"),
                this,
                ctx
            )
            setField(
                this.javaClass.getDeclaredField("projectMapper"),
                this,
                projectMapper
            )
            setField(
                this.javaClass.getDeclaredField("repositoryAssembler"),
                this,
                repositoryAssembler
            )
        }
    }
}
