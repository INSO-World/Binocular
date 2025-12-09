package com.inso_world.binocular.infrastructure.sql.unit.mapper.base

import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.core.unit.base.BaseUnitTest
import com.inso_world.binocular.infrastructure.sql.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.sql.mapper.RemoteMapper
import com.inso_world.binocular.infrastructure.sql.mapper.RepositoryMapper
import io.mockk.spyk
import org.junit.jupiter.api.BeforeEach
import org.springframework.data.util.ReflectionUtils.setField

internal open class BaseMapperTest : BaseUnitTest() {
    lateinit var ctx: MappingContext
    lateinit var projectMapper: ProjectMapper
    lateinit var repositoryMapper: RepositoryMapper
    lateinit var remoteMapper: RemoteMapper

    @BeforeEach
    fun setUp() {
        ctx = spyk(MappingContext())

        remoteMapper = spyk(RemoteMapper())
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

        // wire up remoteMapper
        with(remoteMapper) {
            setField(
                this.javaClass.getDeclaredField("ctx"),
                this,
                ctx
            )
        }
    }
}
