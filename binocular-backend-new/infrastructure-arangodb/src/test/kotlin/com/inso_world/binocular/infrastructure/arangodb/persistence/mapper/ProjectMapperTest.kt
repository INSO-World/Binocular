package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.data.MockTestDataProvider
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.core.unit.base.BaseUnitTest
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.base.BaseMapperTest
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

internal class ProjectMapperTest : BaseMapperTest() {
    private lateinit var mockTestDataProvider: MockTestDataProvider

    @BeforeEach
    fun setup() {
        super.setUp()
        mockTestDataProvider = MockTestDataProvider()
    }

    @Test
    fun `toEntity maps domain object to entity, without repository`() {
        val domain = Project(
            name = "test-project",
        ).apply { description = "my super long description" }

        val entity = projectMapper.toEntity(domain)

        assertAll(
            "check mappings",
            { assertThat(entity.id).isEqualTo(domain.id) },
            { assertThat(entity.name).isEqualTo(domain.name) },
            { assertThat(entity.description).isEqualTo(domain.description) },
            { assertThat(entity.repository).isNull() }
        )

        assertThat(ctx.findEntity<Project.Key, Project, ProjectEntity>(domain)).isEqualTo(entity)

        verify(exactly = 1) { ctx.remember(domain, entity) }
        verify(exactly = 1) { projectMapper.toEntity(domain) }
        verify(exactly = 0) { repositoryMapper.toEntity(any()) }
    }

    @Test
    fun `toEntity maps domain object to entity, with repository`() {
        val domain = requireNotNull(mockTestDataProvider.projectsByName["proj-pg-0"])
        val entity = projectMapper.toEntity(domain)

        assertAll(
            "check mappings",
            { assertThat(entity.id).isEqualTo(domain.id) },
            { assertThat(entity.name).isEqualTo(domain.name) },
            { assertThat(entity.description).isEqualTo(domain.description) },
            { assertThat(entity.repository).isNull() },
        )

        assertThat(ctx.findEntity<Project.Key, Project, ProjectEntity>(domain)).isEqualTo(entity)
        assertThat(ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(requireNotNull(domain.repo))).isEqualTo(
            entity.repository
        )

        verifyOrder {
            ctx.findEntity<Project.Key, Project, ProjectEntity>(domain)
            ctx.remember(domain, entity)
        }
    }
}
