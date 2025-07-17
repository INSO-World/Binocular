package com.inso_world.binocular.infrastructure.sql.integration.persistence.mapper

import com.inso_world.binocular.infrastructure.sql.integration.persistence.mapper.base.BaseMapperTest
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.mapper.RepositoryMapper
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
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
        private val branchModel: Branch =
            Branch(
                name = "testBranch",
            )

        @JvmStatic
        fun commitList(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    listOf(
                        Commit(
                            id = "1",
                            sha = "a".repeat(40),
                            authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                            commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                            message = "Valid commit 1",
                            parents =
                                mutableSetOf(
                                    Commit(
                                        id = "2",
                                        sha = "b".repeat(40),
                                        authorDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                                        commitDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                                        message = "Valid commit 2",
                                    ),
                                ),
                            branches = mutableSetOf(branchModel),
                        ),
                        Commit(
                            id = "2",
                            sha = "b".repeat(40),
                            authorDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                            commitDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                            message = "Valid commit 2",
                            branches = mutableSetOf(branchModel),
                        ),
                    ),
                    2,
                ),
                Arguments.of(
                    listOf(
                        Commit(
                            id = "3",
                            sha = "a".repeat(40),
                            authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                            commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                            message = "Valid commit 1",
                            parents =
                                mutableSetOf(
                                    Commit(
                                        id = "4",
                                        sha = "b".repeat(40),
                                        authorDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                                        commitDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                                        message = "Valid commit 2",
                                    ),
                                ),
                            branches = mutableSetOf(branchModel),
                        ),
                        Commit(
                            id = "5",
                            sha = "c".repeat(40),
                            authorDateTime = LocalDateTime.of(2020, 1, 4, 1, 0, 0, 0),
                            commitDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                            message = "Valid commit 3",
                            branches = mutableSetOf(branchModel),
                        ),
                    ),
                    3,
                ),
                Arguments.of(
                    listOf(
                        Commit(
                            id = "6",
                            sha = "a".repeat(40),
                            authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                            commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                            message = "Valid commit 1",
                            parents =
                                mutableSetOf(
                                    Commit(
                                        id = "7",
                                        sha = "b".repeat(40),
                                        authorDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                                        commitDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                                        message = "Valid commit 2",
                                    ),
                                ),
                            branches = mutableSetOf(branchModel),
                        ),
                        Commit(
                            id = "8",
                            sha = "c".repeat(40),
                            authorDateTime = LocalDateTime.of(2020, 1, 4, 1, 0, 0, 0),
                            commitDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                            message = "Valid commit 3",
                            parents =
                                mutableSetOf(
                                    Commit(
                                        id = "7",
                                        sha = "b".repeat(40),
                                        authorDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                                        commitDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                                        message = "Valid commit 2",
                                    ),
                                ),
                            branches = mutableSetOf(branchModel),
                        ),
                    ),
                    3,
                ),
                Arguments.of(
                    listOf(
                        Commit(
                            id = "9",
                            sha = "a".repeat(40),
                            authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                            commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                            message = "Valid commit 1",
                            parents =
                                mutableSetOf(
                                    Commit(
                                        id = "10",
                                        sha = "b".repeat(40),
                                        authorDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                                        commitDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                                        message = "Valid commit 2",
                                    ),
                                ),
                            branches = mutableSetOf(branchModel),
                        ),
                        Commit(
                            id = "11",
                            sha = "c".repeat(40),
                            authorDateTime = LocalDateTime.of(2020, 1, 4, 1, 0, 0, 0),
                            commitDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                            message = "Valid commit 3",
                            parents =
                                mutableSetOf(
                                    Commit(
                                        id = "12",
                                        sha = "d".repeat(40),
                                        authorDateTime = LocalDateTime.of(2020, 1, 5, 1, 0, 0, 0),
                                        commitDateTime = LocalDateTime.of(2020, 1, 4, 1, 0, 0, 0),
                                        message = "Valid commit 4",
                                    ),
                                ),
                            branches = mutableSetOf(branchModel),
                        ),
                    ),
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
                name = "TestRepo",
                projectId = projectEntity.id?.toString(),
            )

        // Map to entity
        val repositoryEntity = repositoryMapper.toEntity(repositoryDomain, projectEntity)

        // Assert mapping
        assertAll(
            { assertNotNull(repositoryEntity) },
            { assertEquals(repositoryEntity.name, repositoryDomain.name) },
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
                name = "TestRepo",
                projectId = projectEntity.id?.toString(),
            )
        val commitList =
            listOf(
                Commit(
                    id = "1",
                    sha = "a".repeat(40),
                    authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                    commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                    message = "Valid commit 1",
                    repositoryId = domain.id,
                ),
                Commit(
                    id = "2",
                    sha = "b".repeat(40),
                    authorDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                    commitDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                    message = "Valid commit 2",
                    repositoryId = domain.id,
                ),
            )

        domain.commits.addAll(commitList)
        val branch =
            Branch(
                name = "test",
                commitShas = commitList.map { it.sha }.toMutableSet(),
                repositoryId = domain.id,
            )
        commitList.forEach { it.branches.add(branch) }
        domain.branches.add(branch)

        // Map to entity
        val entity = repositoryMapper.toEntity(domain, projectEntity)

        // Assert mapping
        assertAll(
            { assertEquals(entity.name, domain.name) },
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
                name = "TestRepo",
                projectId = projectEntity.id?.toString(),
            )
        commitList.forEach {
            it.repositoryId = domain.id
            domain.branches.addAll(it.branches)
            it.parents.forEach { parent ->
                parent.repositoryId = domain.id
                parent.branches.addAll(it.branches)
            }
        }

        domain.commits.addAll(commitList)

        assertThat(domain.branches).hasSize(1)

        // Map to entity
        val entity = repositoryMapper.toEntity(domain, projectEntity)

        // Assert mapping
        assertAll(
            { assertThat(entity.commits).hasSize(noOfUniqueCommits) },
            { assertThat(entity.branches).hasSize(1) },
            {
//                TODO use commitList?
                val uniqueCommits = (domain.commits + domain.commits.flatMap { it.parents }).distinctBy { it.sha }
                assertThat(entity.commits)
                    .usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .ignoringFields(
                        "id",
                        "repository",
                        "repositoryId",
                        "branches",
                        "parents.id",
                        "parents.repository",
                        "parents.repositoryId",
                        "parents.branches",
                    ).isEqualTo(uniqueCommits)
            },
            {
                assertThat(entity.commits.map { it.repository?.id }).containsOnly(domain.id?.toLong())
            },
        )
    }

    @Test
    fun `repositoryMapper toEntity, with commits with branch, add via commit`() {
        // Create a minimal valid Repository domain object
        val domain =
            Repository(
                id = "10",
                name = "TestRepo",
                projectId = projectEntity.id?.toString(),
            )
        val commit =
            Commit(
                id = "1",
                sha = "a".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                message = "Valid commit 1",
                repositoryId = domain.id,
            )
        val branch =
            Branch(
                name = "test",
                commitShas = mutableSetOf(commit.sha),
                repositoryId = domain.id,
            )
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
}
