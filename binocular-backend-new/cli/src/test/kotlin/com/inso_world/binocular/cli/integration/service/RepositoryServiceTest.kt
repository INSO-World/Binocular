package com.inso_world.binocular.cli.integration.service

import com.inso_world.binocular.cli.index.vcs.VcsCommit
import com.inso_world.binocular.cli.index.vcs.VcsPerson
import com.inso_world.binocular.cli.index.vcs.toDtos
import com.inso_world.binocular.cli.integration.TestDataSetupService
import com.inso_world.binocular.cli.integration.service.base.BaseServiceTest
import com.inso_world.binocular.cli.service.RepositoryService
import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.core.service.UserInfrastructurePort
import com.inso_world.binocular.ffi.BinocularFfi
import com.inso_world.binocular.model.Repository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

internal class RepositoryServiceTest(
    @Autowired
    private val repositoryService: RepositoryService,
) : BaseServiceTest() {
    @Autowired
    private lateinit var testDataSetupService: TestDataSetupService

    @Autowired
    private lateinit var userPort: UserInfrastructurePort

    @Autowired
    private lateinit var branchPort: BranchInfrastructurePort

    private lateinit var simpleRepoVcsCommits: List<VcsCommit>
    private lateinit var octoRepoVcsCommits: List<VcsCommit>
    private lateinit var advancedRepoVcsCommits: List<VcsCommit>
    private val ffi = BinocularFfi()

    @BeforeEach
    fun setUp() {
//        MockKAnnotations.init(this)

        val simpleRepo = ffi.findRepo("${FIXTURES_PATH}/${SIMPLE_REPO}")
        ffi.findCommit(simpleRepo, "HEAD")
        simpleRepoVcsCommits = ffi.traverseBranch(simpleRepo, "master").toDtos()

        val octoRepo = ffi.findRepo("${FIXTURES_PATH}/${OCTO_REPO}")
        ffi.findCommit(octoRepo, "HEAD")
        octoRepoVcsCommits = ffi.traverseBranch(octoRepo, "master").toDtos()

        val advancedRepo = ffi.findRepo("${FIXTURES_PATH}/${ADVANCED_REPO}")
        ffi.findCommit(advancedRepo, "HEAD")
        advancedRepoVcsCommits = ffi.traverseBranch(advancedRepo, "master").toDtos()
    }

    @Test
    fun `transformCommits with empty input should return empty list`() {
        val transformedCommits = repositoryService.transformCommits(this.simpleRepo, emptyList())
        assertThat(transformedCommits).isEmpty()
    }

    @Test
    fun `transformCommits with no parents should result in empty parent lists`() {
        val vcsCommits =
            listOf(
                VcsCommit(
                    "123456789-123456789-123456789-123456789-",
                    "msg1",
                    "null",
                    VcsPerson("User A", "a@test.com"),
                    null,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                ), // No parents field
                VcsCommit(
                    "123456789_123456789_123456789_123456789_",
                    "msg2",
                    "null",
                    VcsPerson("User B", "b@test.com"),
                    null,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    mutableSetOf(),
                ), // Empty parents list
            )
        val transformedCommits = repositoryService.transformCommits(this.simpleRepo, vcsCommits)
        transformedCommits.forEach { commit ->
            assertThat(commit.parents).isEmpty()
        }
    }

    @Test
    fun `transformCommits with null author or committer`() {
        val vcsCommits =
            listOf(
                VcsCommit(
                    "sha1sha1sha1sha1sha1sha1sha1sha1sha1sha1",
                    "msg1",
                    "null",
                    null,
                    VcsPerson("Author Only", "author@test.com"),
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                ),
                VcsCommit(
                    "sha2sha2sha2sha2sha2sha2sha2sha2sha2sha2",
                    "msg2",
                    "null",
                    VcsPerson("Committer Only", "committer@test.com"),
                    null,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                ),
                VcsCommit(
                    "sha3sha3sha3sha3sha3sha3sha3sha3sha3sha3",
                    "msg3",
                    "null",
                    null,
                    null,
                    LocalDateTime.now(),
                    LocalDateTime.now()
                ),
            )
        val transformedCommits = repositoryService.transformCommits(this.simpleRepo, vcsCommits)

        assertThat(transformedCommits.find { it.sha == "sha1sha1sha1sha1sha1sha1sha1sha1sha1sha1" }!!.committer).isNull()
        assertThat(transformedCommits.find { it.sha == "sha1sha1sha1sha1sha1sha1sha1sha1sha1sha1" }!!.author).isNotNull
        assertThat(transformedCommits.find { it.sha == "sha2sha2sha2sha2sha2sha2sha2sha2sha2sha2" }!!.committer).isNotNull
        assertThat(transformedCommits.find { it.sha == "sha2sha2sha2sha2sha2sha2sha2sha2sha2sha2" }!!.author).isNull()
        assertThat(transformedCommits.find { it.sha == "sha3sha3sha3sha3sha3sha3sha3sha3sha3sha3" }!!.committer).isNull()
        assertThat(transformedCommits.find { it.sha == "sha3sha3sha3sha3sha3sha3sha3sha3sha3sha3" }!!.author).isNull()
    }

    @Test
    fun `transformCommits with diamond parent-child structure`() {
        val user = VcsPerson("User", "user@test.com")
        val c1 =
            VcsCommit(
                "c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1",
                "m1",
                "null",
                user,
                user,
                LocalDateTime.now(),
                LocalDateTime.now()
            )
        val c2 =
            VcsCommit(
                "c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2",
                "m2",
                "null",
                user,
                user,
                LocalDateTime.now(),
                LocalDateTime.now(),
                mutableSetOf(c1),
            )
        val c3 =
            VcsCommit(
                "c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3",
                "m3",
                "null",
                user,
                user,
                LocalDateTime.now(),
                LocalDateTime.now(),
                mutableSetOf(c1),
            )
        val c4 =
            VcsCommit(
                "c4c4c4c4c4c4c4c4c4c4c4c4c4c4c4c4c4c4c4c4",
                "m4",
                "null",
                user,
                user,
                LocalDateTime.now(),
                LocalDateTime.now(),
                mutableSetOf(c2, c3)
            )
        val vcsCommits = listOf(c1, c2, c3, c4)

        val transformedCommits = repositoryService.transformCommits(this.simpleRepo, vcsCommits)
        val commitMap = transformedCommits.associateBy { it.sha }

        assertThat(commitMap["c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1"]!!.parents).isEmpty()
        assertThat(
            commitMap["c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2"]!!.parents.map {
                it.sha
            },
        ).containsExactly("c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1")
        assertThat(
            commitMap["c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3"]!!.parents.map {
                it.sha
            },
        ).containsExactly("c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1")
        assertThat(
            commitMap["c4c4c4c4c4c4c4c4c4c4c4c4c4c4c4c4c4c4c4c4"]!!.parents.map {
                it.sha
            },
        ).containsExactlyInAnyOrder(
            "c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2",
            "c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3"
        )

        // Check object identity for parents
        assertThat(
            commitMap["c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2"]!!.parents.first(),
        ).isSameAs(commitMap["c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1"]!!)
        assertThat(
            commitMap["c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3"]!!.parents.first(),
        ).isSameAs(commitMap["c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1c1"]!!)
        assertThat(
            commitMap["c4c4c4c4c4c4c4c4c4c4c4c4c4c4c4c4c4c4c4c4"]!!.parents,
        ).containsExactlyInAnyOrder(
            commitMap["c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2c2"]!!,
            commitMap["c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3c3"]!!,
        )
    }

    @Test
    fun `transformCommits with non-existent parent SHA should not include it`() {
        val vcsCommits =
            listOf(
                VcsCommit(
                    "123456789-123456789-123456789-123456789-",
                    "msg1",
                    "null",
                    VcsPerson("User A", "a@test.com"),
                    null,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    mutableSetOf(
                        VcsCommit(
                            sha = "nonExistentSha",
                            message = "msg2",
                            branch = "null2",
                            committer = VcsPerson("User B", "b@test.com"),
                            author = null,
                            commitTime = LocalDateTime.now(),
                            authorTime = LocalDateTime.now(),
                        )
                    ),
                ),
            )
        val transformedCommits = repositoryService.transformCommits(this.simpleRepo, vcsCommits)
        assertThat(transformedCommits.first().parents).isEmpty()
    }

    @Test
    fun `transformCommits with different committer and author sharing same email`() {
        val vcsCommits =
            listOf(
                VcsCommit(
                    "sha1sha1sha1sha1sha1sha1sha1sha1sha1sha1",
                    "msg1",
                    "null",
                    VcsPerson("Committer Name", "shared@test.com"),
                    VcsPerson("Author Name", "shared@test.com"),
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                ),
            )
        val transformedCommits = repositoryService.transformCommits(this.simpleRepo, vcsCommits)
        val commit = transformedCommits.first()
        assertThat(commit.committer).isNotNull
        assertThat(commit.author).isNotNull
        assertThat(commit.committer).isSameAs(commit.author) // Should be the same User entity instance
        assertThat(commit.committer!!.email).isEqualTo("shared@test.com")
    }

    @Test
    fun `transformCommits should correctly map VcsCommits to Commit entities for simple repository`() {
        val transformedCommits = repositoryService.transformCommits(this.simpleRepo, simpleRepoVcsCommits)

        assertAll(
            { assertThat(transformedCommits).hasSize(simpleRepoVcsCommits.size) },
            {
                transformedCommits.forEach { commit ->
                    val originalVcsCommit = simpleRepoVcsCommits.find { it.sha == commit.sha }
                    assertThat(originalVcsCommit).isNotNull
                    assertThat(commit.message).isEqualTo(originalVcsCommit!!.message)
                    assertThat(commit.branches.map { it.name }).containsExactly(originalVcsCommit.branch)
                    assertThat(commit.commitDateTime).isEqualTo(originalVcsCommit.commitTime)
                    assertThat(commit.authorDateTime).isEqualTo(originalVcsCommit.authorTime)

                    // Check committer details and user caching
                    originalVcsCommit.committer?.let {
                        assertThat(commit.committer).isNotNull
                        assertThat(commit.committer!!.email).isEqualTo(it.email)
                        assertThat(commit.committer!!.name).isEqualTo(it.name)
                    }

                    // Check author details and user caching
                    originalVcsCommit.author?.let {
                        assertThat(commit.author).isNotNull
                        assertThat(commit.author!!.email).isEqualTo(it.email)
                        assertThat(commit.author!!.name).isEqualTo(it.name)
                    }
                }
            },
        )
    }

    @Test
    fun `transformCommits should correctly establish parent-child relationships`() {
        testDataSetupService.clearAllData()

        assertAll(
            "check database numbers",
            { assertThat(commitPort.findAll()).hasSize(0) },
            { assertThat(repositoryPort.findAll()).hasSize(0) },
        )

        val transformedCommits = repositoryService.transformCommits(
            Repository(
                name = "new one"
            ), simpleRepoVcsCommits
        )
        val transformedCommitMap = transformedCommits.associateBy { it.sha }

        transformedCommits.forEach { vcsCommit ->
            val transformedCommit = transformedCommitMap[vcsCommit.sha]
            assertThat(transformedCommit).isNotNull()

            val expectedParentShas = vcsCommit.parents.map { it.sha }
            assertThat(transformedCommit!!.parents.map { it.sha }).containsExactlyInAnyOrderElementsOf(
                expectedParentShas,
            )

            // Ensure parent objects are the correct instances from the transformed map
            transformedCommit.parents.forEach { parentCommit ->
                val parent = transformedCommitMap[parentCommit.sha]
                    ?: throw IllegalStateException("Could not find parent $parentCommit")

                assertThat(parent)
                    .usingRecursiveComparison()
                    .isEqualTo(parentCommit)
                assertThat(parent).isSameAs(parentCommit)
            }
        }
        run {
            val root = transformedCommits.find { it.sha == "48a384a6a9188f376835005cd10fd97542e69bf7" }
            assertThat(root).isNotNull()
            assertThat(root?.parents).isEmpty()
        }
    }

    @Test
    fun `transformCommits should reuse User entities for same email`() {
        val transformedCommits = repositoryService.transformCommits(this.simpleRepo, simpleRepoVcsCommits)
        val allUsers = transformedCommits.flatMap { listOfNotNull(it.committer, it.author) }.filterNotNull()

        val uniqueUsersByEmail = allUsers.distinctBy { it.email }

        assertThat(allUsers.size).isGreaterThanOrEqualTo(uniqueUsersByEmail.size)

        // Check that for each unique email, all User objects with that email are the same instance
        uniqueUsersByEmail.forEach { uniqueUser ->
            allUsers.filter { it.email == uniqueUser.email }.forEach { userInstance ->
                assertThat(userInstance).isSameAs(uniqueUser)
            }
        }

        // Based on simple.sh fixture, we know there are 3 unique users (Alice, Bob, Carol)
        val expectedUniqueUserCount = 3
        assertThat(uniqueUsersByEmail.size).isEqualTo(expectedUniqueUserCount)
    }

    @Test
    fun `transformCommits should establish relationships regardless of input order`() {
        val user = VcsPerson("User", "user@test.com")
        val parent =
            VcsCommit(
                "parentShaparentShaparentShaparentShapare",
                "Parent Commit",
                "null",
                user,
                user,
                LocalDateTime.now(),
                LocalDateTime.now(),
            )
        val child =
            VcsCommit(
                "childShachildShachildShachildShachildSha",
                "Child Commit",
                "null",
                user,
                user,
                LocalDateTime.now(),
                LocalDateTime.now(),
                mutableSetOf(parent),
            )

        // Order: child first, then parent
        val vcsCommitsOutOfOrder = listOf(child, parent)
        val transformedCommits = repositoryService.transformCommits(this.simpleRepo, vcsCommitsOutOfOrder)
        val commitMap = transformedCommits.associateBy { it.sha }

        val transformedChild = commitMap["childShachildShachildShachildShachildSha"]
        val transformedParent = commitMap["parentShaparentShaparentShaparentShapare"]

        assertThat(transformedChild).isNotNull
        assertThat(transformedParent).isNotNull
        assertThat(transformedChild!!.parents).hasSize(1)
        assertThat(transformedChild.parents.first().sha).isEqualTo("parentShaparentShaparentShaparentShapare")
        assertThat(transformedChild.parents.first()).isSameAs(transformedParent) // Check instance identity
        assertThat(transformedParent!!.parents).isEmpty()
    }

    @Test
    fun `transformCommits should only increase user count`() {
        // Clear data to start with empty state
        testDataSetupService.clearAllData()

        val initialUserCount = userPort.findAll().toSet().size
        val initialBranchCount = branchPort.findAll().toSet().size
        val initialCommitCount = commitPort.findAll().toSet().size

        // First transformation
        val transformedCommits1 = repositoryService.transformCommits(this.simpleRepo, simpleRepoVcsCommits)

        val userCountAfterFirst = userPort.findAll().toSet().size
        val branchCountAfterFirst = branchPort.findAll().toSet().size
        val commitCountAfterFirst = commitPort.findAll().toSet().size

        assertAll(
            "check counts after first transformation",
            { assertThat(userCountAfterFirst).isGreaterThanOrEqualTo(initialUserCount) },
            { assertThat(branchCountAfterFirst).isGreaterThanOrEqualTo(initialBranchCount) },
            { assertThat(commitCountAfterFirst).isGreaterThanOrEqualTo(initialCommitCount) }
        )

        // Second transformation with same data
        val transformedCommits2 = repositoryService.transformCommits(this.simpleRepo, simpleRepoVcsCommits)

        val userCountAfterSecond = userPort.findAll().toSet().size
        val branchCountAfterSecond = branchPort.findAll().toSet().size
        val commitCountAfterSecond = commitPort.findAll().toSet().size

        assertAll(
            "check counts after second transformation",
            { assertThat(userCountAfterSecond).isEqualTo(userCountAfterFirst) },
            { assertThat(branchCountAfterSecond).isEqualTo(branchCountAfterFirst) },
            { assertThat(commitCountAfterSecond).isEqualTo(commitCountAfterFirst) }
        )
    }

    @Test
    fun `transformCommits should only increase branch count`() {
        // Clear data to start with empty state
        testDataSetupService.clearAllData()

        val initialBranchCount = branchPort.findAll().toSet().size

        // Transform commits from simple repo
        repositoryService.transformCommits(this.simpleRepo, simpleRepoVcsCommits)
        val branchCountAfterSimple = branchPort.findAll().toSet().size

        assertThat(branchCountAfterSimple).isGreaterThanOrEqualTo(initialBranchCount)

        // Transform commits from octo repo (different branches)
        repositoryService.transformCommits(this.simpleRepo, octoRepoVcsCommits)
        val branchCountAfterOcto = branchPort.findAll().toSet().size

        assertThat(branchCountAfterOcto).isGreaterThanOrEqualTo(branchCountAfterSimple)
    }

    @Test
    fun `transformCommits should only increase commit count`() {
        // Clear data to start with empty state
        testDataSetupService.clearAllData()

        val initialCommitCount = commitPort.findAll().toSet().size

        // First transformation
        repositoryService.transformCommits(this.simpleRepo, simpleRepoVcsCommits)
        val commitCountAfterSimple = commitPort.findAll().toSet().size

        assertThat(commitCountAfterSimple).isGreaterThanOrEqualTo(initialCommitCount)

        // Second transformation with different data
        repositoryService.transformCommits(this.simpleRepo, octoRepoVcsCommits)
        val commitCountAfterOcto = commitPort.findAll().toSet().size

        assertThat(commitCountAfterOcto).isGreaterThanOrEqualTo(commitCountAfterSimple)

        // Third transformation with advanced data
        repositoryService.transformCommits(this.simpleRepo, advancedRepoVcsCommits)
        val commitCountAfterAdvanced = commitPort.findAll().toSet().size

        assertThat(commitCountAfterAdvanced).isGreaterThanOrEqualTo(commitCountAfterOcto)
    }

    @Test
    fun `transformCommits should maintain monotonic increase across multiple calls`() {
        // Clear data to start with empty state
        testDataSetupService.clearAllData()

        val initialUserCount = userPort.findAll().toSet().size
        val initialBranchCount = branchPort.findAll().toSet().size
        val initialCommitCount = commitPort.findAll().toSet().size

        var previousUserCount = initialUserCount
        var previousBranchCount = initialBranchCount
        var previousCommitCount = initialCommitCount

        // Multiple transformations with different data
        val testData = listOf(simpleRepoVcsCommits, octoRepoVcsCommits, advancedRepoVcsCommits)

        testData.forEach { vcsCommits ->
            repositoryService.transformCommits(this.simpleRepo, vcsCommits)

            val currentUserCount = userPort.findAll().toSet().size
            val currentBranchCount = branchPort.findAll().toSet().size
            val currentCommitCount = commitPort.findAll().toSet().size

            assertAll(
                "check monotonic increase",
                { assertThat(currentUserCount).isGreaterThanOrEqualTo(previousUserCount) },
                { assertThat(currentBranchCount).isGreaterThanOrEqualTo(previousBranchCount) },
                { assertThat(currentCommitCount).isGreaterThanOrEqualTo(previousCommitCount) }
            )

            previousUserCount = currentUserCount
            previousBranchCount = currentBranchCount
            previousCommitCount = currentCommitCount
        }
    }

    @Test
    fun `transformCommits should handle empty input without affecting counts`() {
        // Clear data to start with empty state
        testDataSetupService.clearAllData()

        val initialUserCount = userPort.findAll().toSet().size
        val initialBranchCount = branchPort.findAll().toSet().size
        val initialCommitCount = commitPort.findAll().toSet().size

        // Transform with empty input
        val transformedCommits = repositoryService.transformCommits(this.simpleRepo, emptyList())

        val userCountAfterEmpty = userPort.findAll().toSet().size
        val branchCountAfterEmpty = branchPort.findAll().toSet().size
        val commitCountAfterEmpty = commitPort.findAll().toSet().size

        assertAll(
            "check counts remain unchanged with empty input",
            { assertThat(transformedCommits).isEmpty() },
            { assertThat(userCountAfterEmpty).isEqualTo(initialUserCount) },
            { assertThat(branchCountAfterEmpty).isEqualTo(initialBranchCount) },
            { assertThat(commitCountAfterEmpty).isEqualTo(initialCommitCount) }
        )
    }

    @Nested
    @Disabled
    inner class RepositoryUpdateCountTests {

        @Test
        fun `update repository with new users should increase user count`() {
            testDataSetupService.clearAllData()

            val initialUserCount = userPort.findAll().toSet().size

            // First update with simple repo
            repositoryService.transformCommits(simpleRepo, simpleRepoVcsCommits)
            val userCountAfterSimple = userPort.findAll().toSet().size

            assertThat(userCountAfterSimple).isGreaterThan(initialUserCount)

            // Update with octo repo (different users)
            repositoryService.transformCommits(simpleRepo, octoRepoVcsCommits)
            val userCountAfterOcto = userPort.findAll().toSet().size

            assertThat(userCountAfterOcto).isGreaterThanOrEqualTo(userCountAfterSimple)
        }

        @Test
        fun `update repository with new branches should increase branch count`() {
            testDataSetupService.clearAllData()

            val initialBranchCount = branchPort.findAll().toSet().size

            // First update with simple repo
            repositoryService.transformCommits(simpleRepo, simpleRepoVcsCommits)
            val branchCountAfterSimple = branchPort.findAll().toSet().size

            assertThat(branchCountAfterSimple).isGreaterThan(initialBranchCount)

            // Update with octo repo (different branches)
            repositoryService.transformCommits(simpleRepo, octoRepoVcsCommits)
            val branchCountAfterOcto = branchPort.findAll().toSet().size

            assertThat(branchCountAfterOcto).isGreaterThanOrEqualTo(branchCountAfterSimple)

            // Update with advanced repo (more branches)
            repositoryService.transformCommits(simpleRepo, advancedRepoVcsCommits)
            val branchCountAfterAdvanced = branchPort.findAll().toSet().size

            assertThat(branchCountAfterAdvanced).isGreaterThanOrEqualTo(branchCountAfterOcto)
        }

        @Test
        fun `update repository with new commits should increase commit count`() {
            testDataSetupService.clearAllData()

            val initialCommitCount = commitPort.findAll().toSet().size
            assertThat(initialCommitCount).isEqualTo(0)

            // First update with simple repo
            repositoryService.transformCommits(simpleRepo, simpleRepoVcsCommits)
            val commitCountAfterSimple = simpleRepo.commits.toSet().size
            assertThat(commitCountAfterSimple).isEqualTo(14)

            // Update with octo repo (different commits)
            repositoryService.transformCommits(simpleRepo, octoRepoVcsCommits).size
            val commitCountAfterOcto = simpleRepo.commits.toSet().size
            assertThat(commitCountAfterOcto).isEqualTo(14 + 19)

            // Update with advanced repo (more commits)
            repositoryService.transformCommits(simpleRepo, advancedRepoVcsCommits).size
            val commitCountAfterAdvanced = simpleRepo.commits.size
            assertThat(commitCountAfterAdvanced).isEqualTo(14 + 19 + 34)
        }

        @Test
        fun `update repository with overlapping data should maintain counts`() {
            testDataSetupService.clearAllData()

            val initialUserCount = userPort.findAll().toSet().size
            val initialBranchCount = branchPort.findAll().toSet().size
            val initialCommitCount = commitPort.findAll().toSet().size

            // First update
            repositoryService.transformCommits(simpleRepo, simpleRepoVcsCommits)
            val userCountAfterFirst = userPort.findAll().toSet().size
            val branchCountAfterFirst = branchPort.findAll().toSet().size
            val commitCountAfterFirst = commitPort.findAll().toSet().size

            // Second update with same data (should not increase counts)
            repositoryService.transformCommits(simpleRepo, simpleRepoVcsCommits)
            val userCountAfterSecond = userPort.findAll().toSet().size
            val branchCountAfterSecond = branchPort.findAll().toSet().size
            val commitCountAfterSecond = commitPort.findAll().toSet().size

            assertAll(
                "check counts remain same with overlapping data",
                { assertThat(userCountAfterSecond).isEqualTo(userCountAfterFirst) },
                { assertThat(branchCountAfterSecond).isEqualTo(branchCountAfterFirst) },
                { assertThat(commitCountAfterSecond).isEqualTo(commitCountAfterFirst) }
            )
        }

        @Test
        fun `update repository with partial new data should increase counts appropriately`() {
            testDataSetupService.clearAllData()

            val initialUserCount = userPort.findAll().toSet().size
            val initialBranchCount = branchPort.findAll().toSet().size
            val initialCommitCount = commitPort.findAll().toSet().size

            // First update with simple repo
            repositoryService.transformCommits(simpleRepo, simpleRepoVcsCommits)
            val userCountAfterSimple = userPort.findAll().toSet().size
            val branchCountAfterSimple = branchPort.findAll().toSet().size
            val commitCountAfterSimple = commitPort.findAll().toSet().size

            // Update with subset of simple repo data (should not increase)
            val subsetCommits = simpleRepoVcsCommits.take(simpleRepoVcsCommits.size / 2)
            repositoryService.transformCommits(simpleRepo, subsetCommits)
            val userCountAfterSubset = userPort.findAll().toSet().size
            val branchCountAfterSubset = branchPort.findAll().toSet().size
            val commitCountAfterSubset = commitPort.findAll().toSet().size

            assertAll(
                "check counts with subset data",
                { assertThat(userCountAfterSubset).isEqualTo(userCountAfterSimple) },
                { assertThat(branchCountAfterSubset).isEqualTo(branchCountAfterSimple) },
                { assertThat(commitCountAfterSubset).isEqualTo(commitCountAfterSimple) }
            )
        }

        @Test
        fun `update repository with incremental data should show progressive increase`() {
            testDataSetupService.clearAllData()

            val initialUserCount = userPort.findAll().toSet().size
            val initialBranchCount = branchPort.findAll().toSet().size
            val initialCommitCount = commitPort.findAll().toSet().size

            var previousUserCount = initialUserCount
            var previousBranchCount = initialBranchCount
            var previousCommitCount = initialCommitCount

            // Progressive updates with different data sets
            val testDataSets = listOf(
                simpleRepoVcsCommits.take(5), // Small subset
                simpleRepoVcsCommits, // Full simple repo
                octoRepoVcsCommits.take(3), // Small subset of octo
                octoRepoVcsCommits, // Full octo repo
                advancedRepoVcsCommits.take(7), // Medium subset of advanced
                advancedRepoVcsCommits // Full advanced repo
            )

            testDataSets.forEachIndexed { index, vcsCommits ->
                repositoryService.transformCommits(simpleRepo, vcsCommits)

                val currentUserCount = userPort.findAll().toSet().size
                val currentBranchCount = branchPort.findAll().toSet().size
                val currentCommitCount = commitPort.findAll().toSet().size

                assertAll(
                    "check progressive increase for iteration $index",
                    { assertThat(currentUserCount).isGreaterThanOrEqualTo(previousUserCount) },
                    { assertThat(currentBranchCount).isGreaterThanOrEqualTo(previousBranchCount) },
                    { assertThat(currentCommitCount).isGreaterThanOrEqualTo(previousCommitCount) }
                )

                previousUserCount = currentUserCount
                previousBranchCount = currentBranchCount
                previousCommitCount = currentCommitCount
            }
        }

        @Test
        fun `update repository with mixed data should handle all count types correctly`() {
            testDataSetupService.clearAllData()

            val initialUserCount = userPort.findAll().toSet().size
            val initialBranchCount = branchPort.findAll().toSet().size
            val initialCommitCount = commitPort.findAll().toSet().size

            // Create mixed data with new users, branches, and commits
            val mixedCommits = simpleRepoVcsCommits + octoRepoVcsCommits.take(3)
            repositoryService.transformCommits(simpleRepo, mixedCommits)

            val userCountAfterMixed = userPort.findAll().toSet().size
            val branchCountAfterMixed = branchPort.findAll().toSet().size
            val commitCountAfterMixed = commitPort.findAll().toSet().size

            assertAll(
                "check mixed data increases all counts",
                { assertThat(userCountAfterMixed).isGreaterThan(initialUserCount) },
                { assertThat(branchCountAfterMixed).isGreaterThan(initialBranchCount) },
                { assertThat(commitCountAfterMixed).isGreaterThan(initialCommitCount) }
            )

            // Add more mixed data
            val moreMixedCommits = advancedRepoVcsCommits.take(4) + octoRepoVcsCommits.take(2)
            repositoryService.transformCommits(simpleRepo, moreMixedCommits)

            val userCountAfterMoreMixed = userPort.findAll().toSet().size
            val branchCountAfterMoreMixed = branchPort.findAll().toSet().size
            val commitCountAfterMoreMixed = commitPort.findAll().toSet().size

            assertAll(
                "check additional mixed data maintains or increases counts",
                { assertThat(userCountAfterMoreMixed).isGreaterThanOrEqualTo(userCountAfterMixed) },
                { assertThat(branchCountAfterMoreMixed).isGreaterThanOrEqualTo(branchCountAfterMixed) },
                { assertThat(commitCountAfterMoreMixed).isGreaterThanOrEqualTo(commitCountAfterMixed) }
            )
        }

        @Test
        fun `update repository with duplicate data should not increase counts`() {
            testDataSetupService.clearAllData()

            val initialUserCount = userPort.findAll().toSet().size
            val initialBranchCount = branchPort.findAll().toSet().size
            val initialCommitCount = commitPort.findAll().toSet().size

            // First update
            repositoryService.transformCommits(simpleRepo, simpleRepoVcsCommits)
            val userCountAfterFirst = userPort.findAll().toSet().size
            val branchCountAfterFirst = branchPort.findAll().toSet().size
            val commitCountAfterFirst = commitPort.findAll().toSet().size

            // Update with duplicate data
            val duplicateCommits = simpleRepoVcsCommits.map { it.copy() }
            repositoryService.transformCommits(simpleRepo, duplicateCommits)
            val userCountAfterDuplicate = userPort.findAll().toSet().size
            val branchCountAfterDuplicate = branchPort.findAll().toSet().size
            val commitCountAfterDuplicate = commitPort.findAll().toSet().size

            assertAll(
                "check duplicate data doesn't increase counts",
                { assertThat(userCountAfterDuplicate).isEqualTo(userCountAfterFirst) },
                { assertThat(branchCountAfterDuplicate).isEqualTo(branchCountAfterFirst) },
                { assertThat(commitCountAfterDuplicate).isEqualTo(commitCountAfterFirst) }
            )
        }

        @Test
        fun `update repository with empty data between updates should maintain counts`() {
            testDataSetupService.clearAllData()

            val initialUserCount = userPort.findAll().toSet().size
            val initialBranchCount = branchPort.findAll().toSet().size
            val initialCommitCount = commitPort.findAll().toSet().size

            // First update with data
            repositoryService.transformCommits(simpleRepo, simpleRepoVcsCommits)
            val userCountAfterFirst = userPort.findAll().toSet().size
            val branchCountAfterFirst = branchPort.findAll().toSet().size
            val commitCountAfterFirst = commitPort.findAll().toSet().size

            // Update with empty data
            repositoryService.transformCommits(simpleRepo, emptyList())
            val userCountAfterEmpty = userPort.findAll().toSet().size
            val branchCountAfterEmpty = branchPort.findAll().toSet().size
            val commitCountAfterEmpty = commitPort.findAll().toSet().size

            // Update with more data
            repositoryService.transformCommits(simpleRepo, octoRepoVcsCommits)
            val userCountAfterMore = userPort.findAll().toSet().size
            val branchCountAfterMore = branchPort.findAll().toSet().size
            val commitCountAfterMore = commitPort.findAll().toSet().size

            assertAll(
                "check empty data between updates maintains counts",
                { assertThat(userCountAfterEmpty).isEqualTo(userCountAfterFirst) },
                { assertThat(branchCountAfterEmpty).isEqualTo(branchCountAfterFirst) },
                { assertThat(commitCountAfterEmpty).isEqualTo(commitCountAfterFirst) },
                { assertThat(userCountAfterMore).isGreaterThanOrEqualTo(userCountAfterEmpty) },
                { assertThat(branchCountAfterMore).isGreaterThanOrEqualTo(branchCountAfterEmpty) },
                { assertThat(commitCountAfterMore).isGreaterThanOrEqualTo(commitCountAfterEmpty) }
            )
        }
    }
}
