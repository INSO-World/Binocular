package com.inso_world.binocular.infrastructure.sql.integration.persistence.mapper

import com.inso_world.binocular.infrastructure.sql.integration.persistence.mapper.base.BaseMapperTest
import com.inso_world.binocular.infrastructure.sql.mapper.RepositoryMapper
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
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.stream.Stream
import kotlin.test.assertEquals

internal class RepositoryMapperTest : BaseMapperTest() {
    @Autowired
    private lateinit var repositoryMapper: RepositoryMapper

    private lateinit var projectEntity: ProjectEntity

    @BeforeEach
    fun setup() {
        this.projectEntity =
            ProjectEntity(
                id = 1L,
                name = "TestProject",
                description = "A test project",
            )
    }

    companion object {
        private fun branchModel() =
            Branch(
                name = "testBranch",
            )

        @JvmStatic
        fun commitList(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    with(branchModel()) {
                        listOf(
                            run {
                                val cmt = Commit(
                                    id = "1",
                                    sha = "a".repeat(40),
                                    authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                                    commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                                    message = "Valid commit 1",
                                )
                                this.commits.add(cmt)
                                cmt.parents.add(
                                    Commit(
                                        id = "2",
                                        sha = "b".repeat(40),
                                        authorDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                                        commitDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                                        message = "Valid commit 2",
                                    )
                                )
                                cmt
                            },
                            run {
                                val cmt = Commit(
                                    id = "2",
                                    sha = "b".repeat(40),
                                    authorDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                                    commitDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                                    message = "Valid commit 2",
                                )
                                this.commits.add(cmt)
                                cmt
                            },
                        )
                    },
                    2,
                ),
                Arguments.of(
                    with(branchModel()) {
                        listOf(
                            run {
                                val cmt = Commit(
                                    id = "3",
                                    sha = "a".repeat(40),
                                    authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                                    commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                                    message = "Valid commit 1",
                                )
                                this.commits.add(cmt)
                                cmt.parents.add(
                                    Commit(
                                        id = "4",
                                        sha = "b".repeat(40),
                                        authorDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                                        commitDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                                        message = "Valid commit 2",
                                    )
                                )
                                cmt
                            },
                            run {
                                val cmt = Commit(
                                    id = "5",
                                    sha = "c".repeat(40),
                                    authorDateTime = LocalDateTime.of(2020, 1, 4, 1, 0, 0, 0),
                                    commitDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                                    message = "Valid commit 3",
                                )
                                this.commits.add(cmt)
                                cmt
                            },
                        )
                    },
                    3,
                ),
                Arguments.of(
                    with(branchModel()) {
                        listOf(
                            run {
                                val cmt = Commit(
                                    id = "6",
                                    sha = "a".repeat(40),
                                    authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                                    commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                                    message = "Valid commit 1",
                                )
                                this.commits.add(cmt)
                                cmt.parents.add(
                                    Commit(
                                        id = "7",
                                        sha = "b".repeat(40),
                                        authorDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                                        commitDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                                        message = "Valid commit 2",
                                    )
                                )
                                cmt
                            },
                            run {
                                val cmt = Commit(
                                    id = "8",
                                    sha = "c".repeat(40),
                                    authorDateTime = LocalDateTime.of(2020, 1, 4, 1, 0, 0, 0),
                                    commitDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                                    message = "Valid commit 3",
                                )
                                this.commits.add(cmt)
                                cmt.parents.add(
                                    Commit(
                                        id = "7",
                                        sha = "b".repeat(40),
                                        authorDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                                        commitDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                                        message = "Valid commit 2",
                                    )
                                )
                                cmt
                            },
                        )
                    },
                    3,
                ),
                Arguments.of(
                    with(branchModel()) {
                        listOf(
                            run {
                                val cmt = Commit(
                                    id = "9",
                                    sha = "a".repeat(40),
                                    authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                                    commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                                    message = "Valid commit 1",
                                )
                                this.commits.add(cmt)
                                cmt.parents.add(
                                    Commit(
                                        id = "10",
                                        sha = "b".repeat(40),
                                        authorDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                                        commitDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                                        message = "Valid commit 2",
                                    )
                                )
                                cmt
                            },
                            run {
                                val cmt = Commit(
                                    id = "11",
                                    sha = "c".repeat(40),
                                    authorDateTime = LocalDateTime.of(2020, 1, 4, 1, 0, 0, 0),
                                    commitDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                                    message = "Valid commit 3",
                                )
                                this.commits.add(cmt)
                                cmt.parents.add(
                                    Commit(
                                        id = "12",
                                        sha = "d".repeat(40),
                                        authorDateTime = LocalDateTime.of(2020, 1, 5, 1, 0, 0, 0),
                                        commitDateTime = LocalDateTime.of(2020, 1, 4, 1, 0, 0, 0),
                                        message = "Valid commit 4",
                                    )
                                )
                                cmt
                            },
                        )
                    },
                    4,
                ),
            )
    }

    @Test
    fun `repositoryMapper toEntity, minimal valid repository`() {
        // Create a minimal valid Repository domain object
        val repositoryDomain =
            Repository(
                id = "10",
                localPath = "TestRepo",
                project = Project(
                    id = projectEntity.id?.toString(),
                    name = projectEntity.name,
                    description = projectEntity.description,
                )
            )

        // Map to entity
        val repositoryEntity = repositoryMapper.toEntity(repositoryDomain, projectEntity)

        // Assert mapping
        assertAll(
            { assertNotNull(repositoryEntity) },
            { assertEquals(repositoryEntity.localPath, repositoryDomain.localPath) },
            { assertThat(repositoryEntity.project).isSameAs(projectEntity) },
            { assertThat(projectEntity.repo).isSameAs(repositoryEntity) },
            { assertThat(projectEntity.repo).usingRecursiveComparison().isEqualTo(repositoryEntity) },
        )
    }

    @Test
    fun `repositoryMapper toEntity, with commits no parents`() {
        // Create a minimal valid Repository domain object
        val domain =
            Repository(
                id = "10",
                localPath = "TestRepo",
                project = Project(
                    id = projectEntity.id?.toString(),
                    name = projectEntity.name,
                    description = projectEntity.description,
                )
            )
        val commitList =
            listOf(
                Commit(
                    id = "1",
                    sha = "a".repeat(40),
                    authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                    commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                    message = "Valid commit 1",
                ),
                Commit(
                    id = "2",
                    sha = "b".repeat(40),
                    authorDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                    commitDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                    message = "Valid commit 2",
                ),
            )
        domain.commits.addAll(commitList)

        val branch =
            Branch(
                name = "test",
                repository = domain,
            )
        branch.commits.addAll(commitList)
        commitList.forEach { it.branches.add(branch) }
        domain.branches.add(branch)

        // Map to entity
        val entity = repositoryMapper.toEntity(domain, projectEntity)

        // Assert mapping
        assertAll(
            { assertEquals(entity.localPath, domain.localPath) },
            { assertThat(entity.commits).hasSize(domain.commits.size) },
//            {
//                assertThat(entity.commits)
//                    .usingRecursiveComparison()
//                    .ignoringCollectionOrder()
//                    .ignoringFields("id", "repository", "repositoryId", "branches")
//                    .isEqualTo(domain.commits)
//            },
//            {
//                assertThat(entity.commits.flatMap { it.branches }.distinct())
//                    .usingRecursiveComparison()
//                    .ignoringCollectionOrder()
//                    .isEqualTo(entity.branches)
//            },
//            {
//                assertThat(entity.commits.flatMap { it.branches }.distinct())
//                    .usingRecursiveComparison()
//                    .ignoringCollectionOrder()
//                    .comparingOnlyFields("id", "name")
//                    .isEqualTo(domain.commits.flatMap { it.branches }.distinct())
//            },
//            {
//                assertThat(entity.commits.map { it.repository?.id }).isEqualTo(
//                    listOf(
//                        domain.id?.toLong(),
//                        domain.id?.toLong(),
//                    ),
//                )
//            },
//            { assertThat(entity.project).isSameAs(projectEntity) },
//            { assertThat(projectEntity.repo).isSameAs(entity) },
//            { assertThat(projectEntity.repo).usingRecursiveComparison().isEqualTo(entity) },
        )
    }

    @ParameterizedTest
    @MethodSource("commitList")
    fun `repositoryMapper toEntity, with commits with parents`(
        commitList: List<Commit>,
        noOfUniqueCommits: Int,
    ) {
        // Create a minimal valid Repository domain object
        val domain =
            Repository(
                id = "10",
                localPath = "TestRepo",
                project = Project(
                    id = projectEntity.id?.toString(),
                    name = projectEntity.name,
                    description = projectEntity.description,
                )
            )
        commitList.forEach {
            // wire up author, committer
            it.author?.authoredCommits?.add(it)
            it.committer?.committedCommits?.add(it)
            it.repository = domain
            it.branches.forEach { b -> domain.branches.add(b) }
            it.parents.forEach { parent ->
                parent.repository = domain
                it.branches.forEach { b ->
                    parent.branches.add(b)
                    domain.branches.add(b)
                }
                parent.children.add(it)
            }
        }

        commitList.forEach { domain.commits.add(it) }

        assertThat(domain.branches).hasSize(1)

        // Map to entity
        val entity = repositoryMapper.toEntity(domain, projectEntity)

        // Assert mapping
        assertAll(
            { assertThat(entity.commits).hasSize(noOfUniqueCommits) },
            { assertThat(entity.branches).hasSize(1) },
            {
                assertThat(entity.commits.map { it.repository?.id }).containsOnly(domain.id?.toLong())
            },
        )
        (domain.commits + domain.commits.flatMap { it.parents } + domain.commits.flatMap { it.children }).forEach { domainCmt ->
            val entityCmt =
                entity.commits.find { it.sha == domainCmt.sha } ?: throw IllegalStateException("must find commit here")
            assertThat(entityCmt)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFieldsMatchingRegexes(
                    ".*id",
                    ".*repositoryId",
                    ".*repository",
                    ".*children",
                    ".*commits",
                    ".*commitShas",
                    ".*diffs"
                )
                .isEqualTo(domainCmt)
            assertThat(entityCmt.branches.flatMap { b -> b.commits.map { c -> c.sha } })
                .containsAll(domainCmt.branches.flatMap { b -> b.commits.map { c -> c.sha } })
        }
    }

    @Test
    fun `repositoryMapper toEntity, with commits with branch, add via commit`() {
        // Create a minimal valid Repository domain object
        val domain =
            Repository(
                id = "10",
                localPath = "TestRepo",
                project = Project(
                    id = projectEntity.id?.toString(),
                    name = projectEntity.name,
                    description = projectEntity.description,
                )
            )
        val commit =
            Commit(
                id = "1",
                sha = "a".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                message = "Valid commit 1",
            )
        val branch =
            Branch(
                name = "test",
                repository = domain,
            )
        branch.commits.add(commit)
        commit.branches.add(branch)
        domain.branches.add(branch)

        domain.commits.add(commit)

        // Map to entity
        val entity = repositoryMapper.toEntity(domain, projectEntity)

        assertThat(entity.commits).hasSize(domain.commits.size)
        assertThat(entity.branches).hasSize(domain.branches.size)
        assertThat(entity.commits).hasSize(1)
        assertThat(entity.commits.map { it.branches }).hasSize(1)
        assertThat(entity.branches).hasSize(1)
        assertThat(entity.commits.flatMap { it.branches }.toList()[0]).isSameAs(entity.branches.toList()[0])
    }

    @Nested
    inner class ToDomain : BaseMapperTest() {
        private lateinit var repositoryEntity: RepositoryEntity
        private lateinit var repositoryDomain: Repository
        private lateinit var branchEntity: BranchEntity

        private lateinit var commitEntityA: CommitEntity
        private lateinit var commitEntityB: CommitEntity
        private lateinit var commitDomainA: Commit
        private lateinit var commitDomainB: Commit

        val entityManagerMock = mockk<EntityManager>()

        @BeforeEach
        fun setup() {
            run {
                val field = RepositoryMapper::class.java.getDeclaredField("entityManager")
                field.isAccessible = true
                field.set(repositoryMapper, entityManagerMock)
            }

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

            every { entityManagerMock.find(RepositoryEntity::class.java, repositoryEntity.id) } returns repositoryEntity
        }

        @Test
        @Disabled("Probably the mapper needs a refactoring as it uses transactions internally")
        fun `repositoryMapper toDomain, with commit and parent`() {
            commitEntityA.addParent(commitEntityB)
            commitEntityA.addBranch(branchEntity)
            branchEntity.addCommit(commitEntityA)
            branchEntity.addCommit(commitEntityB)

            repositoryEntity.addCommit(commitEntityA)
            repositoryEntity.addCommit(commitEntityB)
            repositoryEntity.addBranch(branchEntity)

            val domain = repositoryMapper.toDomain(repositoryEntity, null)

            assertThat(domain.commits).hasSize(2)
//            assertThat(domain.branches).hasSize(1)
            val commitA = domain.commits.find { it.sha == commitEntityA.sha }
                ?: throw java.lang.IllegalStateException("Could not find commitEntityA")
            val commitB = domain.commits.find { it.sha == commitEntityB.sha }
                ?: throw java.lang.IllegalStateException("Could not find commitEntityB")

            assertThat(commitA.parents).hasSize(1)
            assertThat(commitA.children).hasSize(0)

            assertThat(commitB.parents).hasSize(0)
            assertThat(commitB.children).hasSize(1)
        }
    }
}
