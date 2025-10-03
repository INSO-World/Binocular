package com.inso_world.binocular.infrastructure.sql.integration.persistence.mapper

import com.inso_world.binocular.infrastructure.sql.integration.persistence.mapper.base.BaseMapperTest
import com.inso_world.binocular.infrastructure.sql.mapper.BranchMapper
import com.inso_world.binocular.infrastructure.sql.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.sql.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import io.mockk.every
import io.mockk.mockk
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

internal class CommitMapperTest : BaseMapperTest() {
    @Autowired
    private lateinit var commitMapper: CommitMapper

    @Autowired
    private lateinit var ctx: MappingContext

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
                localPath = "TestRepository",
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
                localPath = this.repositoryEntity.localPath,
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

        this.branchEntity =
            BranchEntity(
                name = "testBranch",
                repository = this.repositoryEntity,
                commits = mutableSetOf(),
            )
//        commitEntityA
//        this.commitEntityA.addBranch(this.branchEntity)
//        commitEntityB
//        this.commitEntityB.addBranch(this.branchEntity)

        this.commitDomainA =
            Commit(
                id = this.commitEntityA.id?.toString(),
                sha = this.commitEntityA.sha,
                authorDateTime = this.commitEntityA.authorDateTime,
                commitDateTime = this.commitEntityA.commitDateTime,
                message = this.commitEntityA.message,
//                repositoryId =
//                    this.commitEntityA.repository
//                        ?.id
//                        .toString(),
            )
        repositoryDomain.commits.add(commitDomainA)

        this.commitDomainB =
            Commit(
                id = this.commitEntityB.id?.toString(),
                sha = this.commitEntityB.sha,
                authorDateTime = this.commitEntityB.authorDateTime,
                commitDateTime = this.commitEntityB.commitDateTime,
                message = this.commitEntityB.message,
//                repositoryId =
//                    this.commitEntityB.repository
//                        ?.id
//                        .toString(),
            )
        repositoryDomain.commits.add(commitDomainB)
    }

    @Nested
    inner class ToEntity : BaseMapperTest() {
        @Test
        fun `commitMapper toEntity, minimal valid example`() {
            val branch =
                Branch(
                    id = branchEntity.id?.toString(),
                    name = branchEntity.name,
                    repository =
                        branchEntity.repository?.toDomain(null)
//                            ?.id
//                            .toString(),
                )
            commitDomainA.branches.add(branch)
            branchEntity.addCommit(commitEntityA)
            repositoryEntity.addBranch(branchEntity)

            val entity =
                assertDoesNotThrow {
                    commitMapper.toEntity(commitDomainA)
                        .also { c -> repositoryEntity.addCommit(c) }
                        .also { branchEntity.addCommit(it) }
                }

            assertAll(
                "entity",
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
            )
            assertAll(
                "repositoryEntity",
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
                    repository =
                        branchEntity.repository?.toDomain(null)
//                            ?.id
//                            .toString(),
                )
//        Commit A
            commitDomainA.branches.add(branch)
            branchEntity.addCommit(commitEntityA)
//        Commit B
            commitDomainB.branches.add(branch)
            branchEntity.addCommit(commitEntityB)
//        Entity setup
            commitEntityA.addParent(commitEntityB)
//            commitEntityB.children.add(commitEntityA)

            val commitWithParent = commitDomainA
            commitWithParent.parents.add(commitDomainB)
//            commitDomainB.children.add(commitWithParent)

            assertThat(branch.commits).hasSize(2)
            assertThat(commitEntityA.parents).hasSize(1)
            assertThat(commitEntityB.parents).hasSize(0)
            assertThat(commitEntityB.children).hasSize(1)
            assertThat(branchEntity.commits).hasSize(2)
            assertThat(repositoryEntity.commits).hasSize(0)

            repositoryEntity.addBranch(branchEntity)

            val entity =
                assertDoesNotThrow {
                    commitMapper.toEntity(commitWithParent)
                }

            assertThat(entity.id).isEqualTo(commitEntityA.id)
            assertThat(entity.parents).hasSize(1)
            assertThat(entity.children).hasSize(0)
            run {
//                BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB
                val cmtB = entity.parents.find { it.sha == "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" }
                    ?: throw IllegalStateException("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB not found")
                assertThat(cmtB)
                    .usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .ignoringFieldsMatchingRegexes(".*branches", ".*repository")
                    .isEqualTo(commitEntityB)
            }
            assertThat(entity.repository).isNull()
            assertThat(entity.branches).hasSize(0)
//        check bidirectional relationship
//            assertThat(entity.branches.toList()[0].commits).hasSize(2)
//            assertThat(entity.branches.toList()[0].commits)
//                .usingRecursiveComparison()
//                .ignoringCollectionOrder()
//                .isEqualTo(listOf(commitEntityA, commitEntityB))
//            assertThat(entity.branches.toList()[0])
//                .usingRecursiveComparison()
//                .ignoringCollectionOrder()
//                .isEqualTo(branchEntity)
            assertThat(entity)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFieldsMatchingRegexes(".*branches", ".*repository")
                .isEqualTo(commitEntityA)
//            assertThat(repositoryEntity.branches).hasSize(1)
//            assertThat(repositoryEntity.branches.toList()[0]).isSameAs(entity.branches.toList()[0])
//            assertThat(repositoryEntity.branches.toList()[0])
//                .usingRecursiveComparison()
//                .ignoringCollectionOrder()
//                .isEqualTo(branchEntity)
//            assertThat(repositoryEntity.commits).hasSize(2)
//            assertThat(repositoryEntity.commits).containsExactly(commitEntityA, commitEntityB)
//            assertThat(repositoryEntity.user).hasSize(0)
        }
    }

    @Nested
    inner class ToDomain : BaseMapperTest() {
        @Autowired
        private lateinit var commitMapper: CommitMapper

        @Autowired
        private lateinit var branchMapper: BranchMapper

        @BeforeEach
        fun setup() {
            // Create a mock EntityManager
            val entityManagerMock = mockk<EntityManager>()

            // Use reflection to set the private field
            run {
                val field = BranchMapper::class.java.getDeclaredField("entityManager")
                field.isAccessible = true
                field.set(branchMapper, entityManagerMock)
            }

            // Now you can stub methods as before
            every { entityManagerMock.find(CommitEntity::class.java, commitEntityA.id) } returns commitEntityA
            every { entityManagerMock.find(CommitEntity::class.java, commitEntityB.id) } returns commitEntityB
            every { entityManagerMock.find(BranchEntity::class.java, branchEntity.id) } returns branchEntity
        }

        @Test
        fun `commitMapper toDomain, one commits, one branch`() {
//            val branchDomain = branchEntity.toDomain()
            commitEntityA.addBranch(branchEntity)
            branchEntity.addCommit(commitEntityA)

            assertThat(commitEntityA.branches).hasSize(1)
            assertThat(branchEntity.commits).hasSize(1)

            val domain =
                assertDoesNotThrow {
                    commitMapper.toDomain(commitEntityA)
                }

            assertThat(domain.repository).isNull()
            assertThat(domain)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFieldsMatchingRegexes(
                    ".*id",".*branches", ".*committer", ".*author", ".*repository", ".*project", ".*_*",
                    ".*issues", ".*modules", ".*stats", ".*repositoryId", ".*builds", ".*files"
                ).isEqualTo(commitEntityA)
            assertThat(domain.branches).isEmpty()
        }

        @Test
        fun `commitMapper toDomain, commit with parent, one branch`() {
            commitEntityA.addParent(commitEntityB)
            commitEntityA.addBranch(branchEntity)
            branchEntity.addCommit(commitEntityA)
            branchEntity.addCommit(commitEntityB)
            // wire up ctx
            ctx.domain.commit[commitDomainB.sha] = commitDomainB

            assertThat(commitEntityA.branches).hasSize(1)
            assertThat(branchEntity.commits).hasSize(2)

            val domain =
                assertDoesNotThrow {
                    commitMapper.toDomain(commitEntityA)
                }

            assertThat(domain.parents).hasSize(1)
            assertThat(domain.parents.toList()[0].children).hasSize(1)
            assertThat(domain.branches).hasSize(0)
            assertThat(setOf(domain) + domain.parents + domain.children)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFieldsMatchingRegexes(
                    ".*id",".*branches", ".*committer", ".*author", ".*repository", ".*project", ".*_*",
                    ".*issues", ".*modules", ".*stats", ".*repositoryId", ".*builds", ".*files"
                )
                .isEqualTo(listOf(commitEntityA, commitEntityB))
        }

        @Test
        fun `commitMapper toDomain, add branch multiple times`() {
            val branchDomain = branchEntity.toDomain()
            commitEntityA.addBranch(branchEntity)
            branchEntity.addCommit(commitEntityA)

            val domainA =
                assertDoesNotThrow {
                    commitMapper.toDomain(commitEntityA)
                        .also { repositoryDomain.commits.add(it) }
                        .also { branchDomain.commits.add(it) }
                }

            assertThat(domainA.branches).hasSize(1)

//            val domainBranch = domain.branches.toList()[0]
//            assertFalse(domain.addBranch(domainBranch))
            commitEntityB.addBranch(branchEntity)
            branchEntity.addCommit(commitEntityB)

            val domainB =
                assertDoesNotThrow {
                    commitMapper.toDomain(commitEntityB)
                        .also { repositoryDomain.commits.add(it) }
                        .also { branchDomain.commits.add(it) }
                }

            assertThat(domainB.branches).hasSize(1)
        }
    }
}
