package com.inso_world.binocular.infrastructure.sql.integration.persistence.mapper

import com.inso_world.binocular.infrastructure.sql.integration.persistence.mapper.base.BaseMapperTest
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.mapper.BranchMapper
import com.inso_world.binocular.infrastructure.sql.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.sql.mapper.context.MappingSession
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import io.mockk.every
import io.mockk.mockk
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

@Disabled
internal class CommitMapperTest : BaseMapperTest() {
    @Autowired
    private lateinit var commitMapper: CommitMapper

    private lateinit var repositoryEntity: RepositoryEntity
    private lateinit var repositoryDomain: Repository
    private lateinit var branchEntity: BranchEntity

    private lateinit var commitEntityA: CommitEntity
    private lateinit var commitEntityB: CommitEntity
    private lateinit var commitDomainA: Commit
    private lateinit var commitDomainB: Commit

    @BeforeEach
    fun setup() {
        this.repositoryEntity =
            RepositoryEntity(
                id = 1L,
                name = "TestRepository",
                project =
                    ProjectEntity(
                        id = 1L,
                        name = "TestProject",
                        description = "A test project",
                    ),
            )
        this.repositoryDomain =
            Repository(
                id = this.repositoryEntity.id?.toString(),
                name = this.repositoryEntity.name,
                project = Project(
                    id = this.repositoryEntity.project.id?.toString(),
                    name = this.repositoryEntity.project.name,
                    description = this.repositoryEntity.project.description,
                ),
            )

        this.commitEntityA =
            CommitEntity(
                id = 1,
                sha = "A".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                message = "Valid commit 1",
                repository = this.repositoryEntity,
                branches = mutableSetOf(),
            )
//            MapperTestData.commitEntityA.copy().apply {
//                branches = mutableSetOf()
//                parents = mutableSetOf()
//            }
        this.commitEntityB =
            CommitEntity(
                id = 2,
                sha = "B".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                message = "Valid commit 2",
                repository = this.repositoryEntity,
                branches = mutableSetOf(),
            )
//            MapperTestData.commitEntityB.copy().apply {
//                branches = mutableSetOf()
//                parents = mutableSetOf()
//            }
        this.branchEntity =
            BranchEntity(
                name = "testBranch",
                repository = this.repositoryEntity,
                commits =
                    mutableSetOf(
//                    commitEntityA,
//                    commitEntityB,
                    ),
            )
//            MapperTestData.branchEntity.copy().apply {
//                commits = mutableSetOf()
//            }
//        commitEntityA
        this.commitEntityA.branches.add(this.branchEntity)
//        this.branchEntity.commits.add(this.commitEntityA)
//        commitEntityB
        this.commitEntityB.branches.add(this.branchEntity)
//        this.branchEntity.commits.add(this.commitEntityB)

//            MapperTestData.repositoryEntity.copy().apply {
//                branches = mutableSetOf()
//                commits = mutableSetOf()
// //            user = mutableSetOf()
//            }

        this.commitDomainA =
            Commit(
                id = this.commitEntityA.id?.toString(),
                sha = this.commitEntityA.sha,
                authorDateTime = this.commitEntityA.authorDateTime,
                commitDateTime = this.commitEntityA.commitDateTime,
                message = this.commitEntityA.message,
                repositoryId =
                    this.commitEntityA.repository
                        ?.id
                        .toString(),
            )
//            MapperTestData.commitDomainA.copy().apply {
//                branches = mutableSetOf()
//                parents = mutableListOf()
//            }
        this.commitDomainB =
            Commit(
                id = this.commitEntityB.id?.toString(),
                sha = this.commitEntityB.sha,
                authorDateTime = this.commitEntityB.authorDateTime,
                commitDateTime = this.commitEntityB.commitDateTime,
                message = this.commitEntityB.message,
                repositoryId =
                    this.commitEntityB.repository
                        ?.id
                        .toString(),
            )
//            MapperTestData.commitDomainB.copy().apply {
//                branches = mutableSetOf()
//                parents = mutableListOf()
//            }

//        this.commitDomainB.branches.add(branch)
//        branch.commitShas.add(this.commitEntityB.sha)
    }

    @Nested
    inner class ToEntity : BaseMapperTest() {
        @Test
        fun `commitMapper toEntity, minimal valid example`() {
            val branch =
                Branch(
                    id = branchEntity.id?.toString(),
                    name = branchEntity.name,
                    repositoryId =
                        branchEntity.repository
                            ?.id
                            .toString(),
                )
            commitDomainA.branches.add(branch)
            branch.commitShas.add(commitEntityA.sha)
            branchEntity.commits.add(commitEntityA)
//        repositoryEntity.branches.add(branchEntity)

            val entity =
                assertDoesNotThrow {
                    commitMapper.toEntity(commitDomainA, repositoryEntity)
                }

            assertAll(
                { assertThat(entity.id).isEqualTo(commitEntityA.id) },
                { assertThat(entity.parents).isEmpty() },
                { assertThat(entity.repository).isNotNull() },
                { assertThat(entity.repository).isSameAs(repositoryEntity) },
                { assertThat(entity.branches).hasSize(1) },
                {
                    assertThat(entity.branches.toList()[0])
                        .usingRecursiveComparison()
                        .ignoringCollectionOrder()
                        .isEqualTo(branchEntity)
                },
                {
                    val actualCommitUnwrapped =
                        entity.copy(
                            parents = entity.parents.toMutableSet(),
                        )
                    val expectedCommitUnwrapped =
                        commitEntityA.copy(
                            parents = commitEntityA.parents.toMutableSet(),
                        )
                    assertThat(actualCommitUnwrapped)
                        .usingRecursiveComparison()
                        .ignoringCollectionOrder()
                        .isEqualTo(expectedCommitUnwrapped)
                },
                { assertThat(repositoryEntity.branches).hasSize(1) },
                { assertThat(repositoryEntity.commits).hasSize(1) },
                { assertThat(repositoryEntity.user).hasSize(0) },
            )
        }

        @Test
        fun `commitMapper toEntity, commit with parent`() {
            val branch =
                Branch(
                    id = branchEntity.id?.toString(),
                    name = branchEntity.name,
                    repositoryId =
                        branchEntity.repository
                            ?.id
                            .toString(),
                )
//        Commit A
            commitDomainA.branches.add(branch)
            branch.commitShas.add(commitEntityA.sha)
            branchEntity.commits.add(commitEntityA)
//        Commit B
            commitDomainB.branches.add(branch)
            branch.commitShas.add(commitEntityB.sha)
            branchEntity.commits.add(commitEntityB)
//        Entity setup
            commitEntityA.parents.add(commitEntityB)

            val commitWithParent = commitDomainA
            commitWithParent.parents.add(commitDomainB)

            assertThat(branch.commitShas).hasSize(2)
            assertThat(commitEntityA.parents).hasSize(1)
            assertThat(branchEntity.commits).hasSize(2)
            assertThat(repositoryEntity.commits).hasSize(0)

            val entity =
                assertDoesNotThrow {
                    commitMapper.toEntity(commitWithParent, repositoryEntity)
                }

            assertThat(entity.id).isEqualTo(commitEntityA.id)
            assertThat(entity.parents).hasSize(1)
            assertThat(entity.parents.toList()[0])
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(commitEntityB)
            assertThat(entity.repository).isNotNull()
            assertThat(entity.branches).hasSize(1)
//        check bidirectional relationship
            assertThat(entity.branches.toList()[0].commits).hasSize(2)
            assertThat(entity.branches.toList()[0].commits)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(listOf(commitEntityA, commitEntityB))
            assertThat(entity.branches.toList()[0])
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(branchEntity)
            assertThat(entity)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(commitEntityA)
            assertThat(repositoryEntity.branches).hasSize(1)
            assertThat(repositoryEntity.branches.toList()[0]).isSameAs(entity.branches.toList()[0])
            assertThat(repositoryEntity.branches.toList()[0])
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(branchEntity)
            assertThat(repositoryEntity.commits).hasSize(2)
            assertThat(repositoryEntity.commits).containsExactly(commitEntityA, commitEntityB)
            assertThat(repositoryEntity.user).hasSize(0)
        }
    }

    @Nested
    inner class ToDomain : BaseMapperTest() {
        @Autowired
        private lateinit var commitMapper: CommitMapper

        @BeforeEach
        fun setup() {
            // Create a mock EntityManager
            val entityManagerMock = mockk<EntityManager>()

            // Use reflection to set the private field
            run {
                val field = CommitMapper::class.java.getDeclaredField("entityManager")
                field.isAccessible = true
                field.set(commitMapper, entityManagerMock)
            }

            run {
                // Get private field 'branchMapper' from commitMapper
                val branchMapperField = CommitMapper::class.java.getDeclaredField("branchMapper")
                branchMapperField.isAccessible = true
                val branchMapperInstance = branchMapperField.get(commitMapper)

                // Set private field 'entityManager' on branchMapper
                val branchMapperEntityManagerField = BranchMapper::class.java.getDeclaredField("entityManager")
                branchMapperEntityManagerField.isAccessible = true
                branchMapperEntityManagerField.set(branchMapperInstance, entityManagerMock)
            }

            // Now you can stub methods as before
            every { entityManagerMock.find(CommitEntity::class.java, commitEntityA.id) } returns commitEntityA
            every { entityManagerMock.find(CommitEntity::class.java, commitEntityB.id) } returns commitEntityB
            every { entityManagerMock.find(BranchEntity::class.java, branchEntity.id) } returns branchEntity
        }

        @Test
        fun `commitMapper toDomain, one commits, one branch`() {
            commitEntityA.branches.add(branchEntity)
            branchEntity.commits.add(commitEntityA)

            assertThat(commitEntityA.branches).hasSize(1)
            assertThat(branchEntity.commits).hasSize(1)

            val domain =
                assertDoesNotThrow {
                    commitMapper.toDomain(commitEntityA, repositoryDomain)
                }

            assertThat(domain.repositoryId).isEqualTo(commitEntityA.id.toString())
            assertThat(domain)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFields(
                    "id",
                    "branch",
                    "stats",
                    "repositoryId",
                    "branches", // on purpose, should be checked by next assert
                    "issues",
                    "modules",
                    "users",
                    "children",
                    "builds",
                    "files",
                ).isEqualTo(commitEntityA)
            assertThat(domain.branches)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFields(
//                    copied from BranchMapperTest
                    "id",
                    "commits",
                    "latestCommit",
                    "active",
                    "repositoryId",
                    "tracksFileRenames",
                    "files",
                    "branch",
                    "commitShas",
                ).isEqualTo(commitEntityA.branches)
        }

        @Test
        fun `commitMapper toDomain, commit with parent, one branch`() {
            commitEntityA.parents.add(commitEntityB)
            commitEntityA.branches.add(branchEntity)
            branchEntity.commits.add(commitEntityA)
            branchEntity.commits.add(commitEntityB)

            assertThat(commitEntityA.branches).hasSize(1)
            assertThat(branchEntity.commits).hasSize(2)

            val domain =
                assertDoesNotThrow {
                    commitMapper.toDomain(commitEntityA, repositoryDomain)
                }

            assertThat(domain.parents).hasSize(1)
            assertThat(domain.branches).hasSize(1)
            assertThat(domain.branches.toList()[0].commitShas)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(listOf(commitEntityA.sha, commitEntityB.sha))
        }

        @Test
        fun `commitMapper toDomain, add branch multiple times`() {
            commitEntityA.branches.add(branchEntity)
            branchEntity.commits.add(commitEntityA)

            val domainA =
                assertDoesNotThrow {
                    commitMapper.toDomain(commitEntityA, repositoryDomain)
                }

            assertThat(domainA.branches).hasSize(1)

//            val domainBranch = domain.branches.toList()[0]
//            assertFalse(domain.branches.add(domainBranch))
            commitEntityB.branches.add(branchEntity)
            branchEntity.commits.add(commitEntityB)

            val domainB =
                assertDoesNotThrow {
                    commitMapper.toDomain(commitEntityB, repositoryDomain)
                }

            assertThat(domainB.branches).hasSize(1)
        }
    }
}
