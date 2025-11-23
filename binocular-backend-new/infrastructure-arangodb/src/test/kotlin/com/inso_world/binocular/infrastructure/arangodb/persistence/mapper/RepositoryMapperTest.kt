package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.data.MockTestDataProvider
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.base.BaseMapperTest
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import io.mockk.clearMocks
import io.mockk.verify
import io.mockk.verifyOrder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

internal class RepositoryMapperTest : BaseMapperTest() {
    private lateinit var mockTestDataProvider: MockTestDataProvider

    @BeforeEach
    fun setup() {
        super.setUp()
        mockTestDataProvider = MockTestDataProvider()
    }

    @Test
    fun `toEntity maps domain object to entity, with repository`() {
        val domain = requireNotNull(mockTestDataProvider.repositoriesByPath["repo-pg-0"])
        with(domain.project) {
            ctx.remember(
                this, ProjectEntity(
                    id = this.id,
                    name = this.name,
                    description = this.description,
                )
            )
        }
        // clean mock
        clearMocks(ctx)
        val entity = repositoryMapper.toEntity(domain)

        assertAll(
            "check mappings",
            { assertThat(entity.id).isEqualTo(domain.id) },
            { assertThat(entity.name).isEqualTo(domain.localPath) },
            { assertThat(entity.project).isNotNull() },
            { assertThat(entity.project.repository).isSameAs(entity) }
        )

        assertThat(ctx.findEntity<Project.Key, Project, ProjectEntity>(requireNotNull(domain.project))).isEqualTo(entity.project)
        assertThat(ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(domain)).isEqualTo(entity)

        verifyOrder {
            ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(domain)
            ctx.remember(domain, entity)
        }
    }
}
