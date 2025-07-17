package com.inso_world.binocular.infrastructure.sql.integration.persistence.mapper

import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.sql.integration.persistence.mapper.base.BaseMapperTest
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.mapper.BranchMapper
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository
import io.mockk.every
import io.mockk.mockk
import jakarta.persistence.EntityManager
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

internal class BranchMapperTest : BaseMapperTest() {
    @Autowired
    private lateinit var proxyFactory: RelationshipProxyFactory

    @Autowired
    private lateinit var branchMapper: BranchMapper

    lateinit var validator: Validator

    private lateinit var repositoryEntity: RepositoryEntity
    private lateinit var repositoryModel: Repository

    @BeforeEach
    fun setup() {
        val projectEntity =
            ProjectEntity(
                id = 1L,
                name = "TestProject",
                description = "A test project",
            )

        this.repositoryEntity =
            RepositoryEntity(
                id = 1L,
                name = "TestRepository",
                project = projectEntity,
            )

        this.repositoryModel =
            Repository(
                id = this.repositoryEntity.id.toString(),
                name = this.repositoryEntity.name,
                projectId = this.repositoryEntity.id.toString(),
            )

        this.validator = Validation.buildDefaultValidatorFactory().validator
    }

    @Test
    fun `branchMapper toEntity, minimal valid example`() {
        val branch =
            Branch(
                name = "testBranch",
                repositoryId = this.repositoryEntity.id?.toString(),
            )

        val entity = this.branchMapper.toEntity(branch, this.repositoryEntity, mutableMapOf(), mutableMapOf())

        assertThat(entity.repository).isNotNull()
        assertAll(
            { assertThat(entity.repository?.id).isEqualTo(this.repositoryEntity.id) },
            { assertThat(entity.commits).isEmpty() },
        )
    }

    @Test
    fun `branchMapper toEntity, with commit`() {
        val commit =
            Commit(
                id = "1",
                sha = "a".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                message = "Valid commit 1",
                repositoryId = this.repositoryEntity.id?.toString(),
            )
        val branch =
            Branch(
                name = "testBranch",
                repositoryId = this.repositoryEntity.id?.toString(),
                commitShas = mutableSetOf(commit.sha),
            )
        // needed as branchMapper.toEntity requires the commit to be part of the repository already
        val commitEntity =
            CommitEntity(
                sha = commit.sha,
                authorDateTime = commit.authorDateTime,
                commitDateTime = commit.commitDateTime,
                message = commit.message,
                repository = this.repositoryEntity,
                branches = mutableSetOf(),
            )
        val branchEntity =
            BranchEntity(
                name = branch.name,
                commits = proxyFactory.createLazyMutableSet { listOf(commitEntity) },
                repository = this.repositoryEntity,
            )
        commitEntity.branches.add(branchEntity)
        this.repositoryEntity.commits.add(commitEntity)
        this.repositoryEntity.branches.add(branchEntity)

        val entity =
            assertDoesNotThrow {
                this.branchMapper.toEntity(
                    branch,
                    this.repositoryEntity,
                    this.repositoryEntity.commits.associateBy { it.sha },
                    this.repositoryEntity.branches
                        .associateBy { it.uniqueKey() }
                        .toMutableMap(),
                )
            }

        assertAll(
            "entity.repository",
            { assertThat(entity.repository).isNotNull() },
            { assertThat(entity.repository?.id).isEqualTo(this.repositoryEntity.id) },
            { assertThat(entity.repository?.commits).hasSize(1) },
            { assertThat(entity.repository?.commits?.toList()[0]).isSameAs(entity.commits.toList()[0]) },
        )
        assertAll(
            "entity.commits",
            { assertThat(entity.commits).hasSize(1) },
        )
        assertAll(
            "entity.commits.branches",
            { assertThat(entity.commits.flatMap { it.branches }).hasSize(1) },
            { assertThat(entity.commits.flatMap { it.branches }.toList()[0]).isSameAs(entity) },
        )
    }

    @Nested
    inner class ToDomain {
        private lateinit var entityManagerMock: EntityManager

        @BeforeEach
        fun setup() {
            // Create a mock EntityManager
            entityManagerMock = mockk<EntityManager>()

            // Use reflection to set the private field
            run {
                val field = BranchMapper::class.java.getDeclaredField("entityManager")
                field.isAccessible = true
                field.set(branchMapper, entityManagerMock)
            }
        }

        @Test
        fun `branchMapper toDomain, minimal valid example`() {
            val cmt =
                CommitEntity(
                    sha = "d".repeat(40),
                    branches = mutableSetOf(),
                )
            val branch =
                BranchEntity(
                    id = 1,
                    name = "testBranch",
                    repository = repositoryEntity,
                    commits =
                        mutableSetOf(cmt),
                )
            cmt.branches.add(branch)

            every { entityManagerMock.find(BranchEntity::class.java, branch.id) } returns branch

            val entity = branchMapper.toDomain(branch, repositoryModel, mutableMapOf(), mutableMapOf())

            assertAll(
                { assertThat(entity.id).isEqualTo(branch.id.toString()) },
                { assertThat(entity.repositoryId).isEqualTo(repositoryModel.id) },
                { assertThat(entity.repositoryId).isEqualTo(repositoryEntity.id.toString()) },
                { assertThat(entity.repositoryId).isEqualTo(branch.repository?.id?.toString()) },
                { assertThat(entity.commitShas).containsExactlyInAnyOrderElementsOf(listOf("d".repeat(40))) },
                {
                    assertThat(entity)
                        .usingRecursiveComparison()
                        .ignoringCollectionOrder()
                        .ignoringFields(
                            "id",
                            "commits",
                            "latestCommit",
                            "active",
                            "repositoryId",
                            "tracksFileRenames",
                            "files",
                            "branch",
                            "commitShas",
                        ).isEqualTo(branch)
                },
            )
        }
    }
}
