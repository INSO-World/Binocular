package com.inso_world.binocular.infrastructure.sql.unit.mapper

import com.inso_world.binocular.core.extension.reset
import com.inso_world.binocular.infrastructure.sql.TestData
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RemoteEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.unit.mapper.base.BaseMapperTest
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.vcs.Remote
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal class RemoteMapperTest : BaseMapperTest() {

    @Nested
    inner class ToEntity {
        private lateinit var repositoryEntity: RepositoryEntity
        private lateinit var repositoryDomain: Repository

        @BeforeEach
        fun setup() {
            val project = TestData.Domain.testProject(name = "TestProject")
            val projectEntity = TestData.Entity.testProjectEntity(name = project.name, id = 1L)
            repositoryDomain = TestData.Domain.testRepository(localPath = "/tmp/repo", project = project)
            repositoryEntity = TestData.Entity.testRepositoryEntity(
                localPath = repositoryDomain.localPath,
                id = 1L,
                project = projectEntity,
            )
            ctx.remember(repositoryDomain, repositoryEntity)
        }

        @Test
        fun `maps remote metadata`() {
            val remote = Remote(name = "origin", url = "https://example.com/repo.git", repository = repositoryDomain)

            val entity = assertDoesNotThrow { remoteMapper.toEntity(remote) }

            assertAll(
                { assertThat(entity.name).isEqualTo("origin") },
                { assertThat(entity.url).isEqualTo("https://example.com/repo.git") },
                { assertThat(entity.repository).isSameAs(repositoryEntity) },
                { assertThat(entity.iid).isEqualTo(remote.iid) },
            )
        }

        @Test
        fun `requires repository in context`() {
            ctx.reset()
            val remote = Remote(name = "origin", url = "https://example.com/repo.git", repository = repositoryDomain)

            val exception = assertThrows<IllegalStateException> { remoteMapper.toEntity(remote) }

            assertThat(exception.message).contains("RepositoryEntity must be mapped before RemoteEntity")
        }

        @Test
        fun `fast path returns cached entity`() {
            val remote = Remote(name = "origin", url = "https://example.com/repo.git", repository = repositoryDomain)
            val first = remoteMapper.toEntity(remote)

            val cached = remoteMapper.toEntity(remote)

            assertThat(cached).isSameAs(first)
        }
    }

    @Nested
    inner class ToDomain {
        private lateinit var repositoryEntity: RepositoryEntity
        private lateinit var repositoryDomain: Repository

        @BeforeEach
        fun setup() {
            val projectEntity = ProjectEntity(
                name = "TestProject",
                iid = Project.Id(Uuid.random())
            ).apply {
                id = 1L
                description = "desc"
            }
            repositoryEntity = RepositoryEntity(
                iid = Repository.Id(Uuid.random()),
                localPath = "/tmp/repo",
                project = projectEntity,
            ).apply { id = 10L }
            projectEntity.repo = repositoryEntity

            val project = TestData.Domain.testProject(name = projectEntity.name, id = projectEntity.id.toString())
            repositoryDomain = TestData.Domain.testRepository(
                localPath = repositoryEntity.localPath,
                id = repositoryEntity.id.toString(),
                project = project
            )
            ctx.remember(project, projectEntity)
            ctx.remember(repositoryDomain, repositoryEntity)
        }

        @Test
        fun `maps remote metadata`() {
            val remoteEntity = RemoteEntity(
                name = "origin",
                url = "https://example.com/repo.git",
                repository = repositoryEntity,
                iid = Remote.Id(Uuid.random())
            ).apply { id = 30L }

            val domain = assertDoesNotThrow { remoteMapper.toDomain(remoteEntity) }

            assertAll(
                { assertThat(domain.name).isEqualTo(remoteEntity.name) },
                { assertThat(domain.url).isEqualTo(remoteEntity.url) },
                { assertThat(domain.repository).isSameAs(repositoryDomain) },
                { assertThat(domain.iid).isEqualTo(remoteEntity.iid) },
            )
        }

        @Test
        fun `requires repository domain in context`() {
            ctx.reset()
            val remoteEntity = RemoteEntity(
                name = "origin",
                url = "https://example.com/repo.git",
                repository = repositoryEntity,
                iid = Remote.Id(Uuid.random())
            )

            val exception = assertThrows<IllegalStateException> { remoteMapper.toDomain(remoteEntity) }

            assertThat(exception.message).contains("Repository must be mapped before Remote")
        }

        @Test
        fun `fast path returns cached domain`() {
            val remoteEntity = RemoteEntity(
                name = "origin",
                url = "https://example.com/repo.git",
                repository = repositoryEntity,
                iid = Remote.Id(Uuid.random())
            )
            val first = remoteMapper.toDomain(remoteEntity)

            val cached = remoteMapper.toDomain(remoteEntity)

            assertThat(cached).isSameAs(first)
        }
    }
}
