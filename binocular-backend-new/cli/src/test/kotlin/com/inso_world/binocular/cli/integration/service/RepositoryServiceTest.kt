package com.inso_world.binocular.cli.integration.service

import com.inso_world.binocular.cli.integration.TestDataSetupService
import com.inso_world.binocular.cli.integration.service.base.BaseServiceTest
import com.inso_world.binocular.cli.service.RepositoryService
import com.inso_world.binocular.core.data.MockTestDataProvider
import com.inso_world.binocular.core.index.GitIndexer
import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.core.service.UserInfrastructurePort
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import kotlin.io.path.Path

@Deprecated("still required?")
internal class RepositoryServiceTest(
    @all:Autowired
    private val repositoryService: RepositoryService,
) : BaseServiceTest() {
    @Autowired
    private lateinit var idx: GitIndexer

    @Autowired
    private lateinit var testDataSetupService: TestDataSetupService

    @Autowired
    private lateinit var userPort: UserInfrastructurePort

    @Autowired
    private lateinit var branchPort: BranchInfrastructurePort

    private lateinit var simpleRepoVcsCommits: List<Commit>
    private lateinit var octoRepoVcsCommits: List<Commit>
    private lateinit var advancedRepoVcsCommits: List<Commit>
    private lateinit var testData: MockTestDataProvider

    @BeforeEach
    fun setUp() {
        this.simpleRepoVcsCommits = this.simpleRepo.commits.toList()
        this.testData = MockTestDataProvider()

        run {
            val path = Path("${FIXTURES_PATH}/${OCTO_REPO}")
            val repo = idx.findRepo(path)
            val branch = Branch(name = "master")
            repo.branches.add(branch)
            octoRepoVcsCommits = idx.traverseBranch(repo, branch)
            repo
        }


        run {
            val path = Path("${FIXTURES_PATH}/${ADVANCED_REPO}")
            val repo = idx.findRepo(path)
            val branch = Branch(name = "master")
            repo.branches.add(branch)
            advancedRepoVcsCommits = idx.traverseBranch(repo, branch)
            repo
        }
    }

    @Test
    fun `transformCommits with empty input should return empty list`() {
        val transformedCommits = repositoryService.transformCommits(this.simpleRepo, emptyList())
        assertThat(transformedCommits).isEmpty()
    }

    @Test
    fun `transformCommits with no parents should result in empty parent lists`() {
        val vcsCommits = this.testData.commits
        val transformedCommits = repositoryService.transformCommits(this.simpleRepo, vcsCommits)
        transformedCommits.forEach { commit ->
            assertThat(commit.parents).isEmpty()
        }
    }

    @Test
    fun `transformCommits with null author or committer`() {
        val vcsCommits = this.testData.commits
        val transformedCommits = repositoryService.transformCommits(this.simpleRepo, vcsCommits)

        assertThat(transformedCommits.find { it.sha == "a".repeat(40) }?.committer).isNotNull()
        assertThat(transformedCommits.find { it.sha == "a".repeat(40) }?.author).isNull()
        assertThat(transformedCommits.find { it.sha == "b".repeat(40) }?.committer).isNotNull()
        assertThat(transformedCommits.find { it.sha == "b".repeat(40) }?.author).isNull()
        assertThat(transformedCommits.find { it.sha == "c".repeat(40) }?.committer).isNotNull()
        assertThat(transformedCommits.find { it.sha == "c".repeat(40) }?.author).isNull()
    }

    @Test
    fun `transformCommits with diamond parent-child structure`() {
        val c1 = this.testData.commits[0]
        val c2 = run {
            val cmt = this.testData.commits[1]
            cmt.parents.add(c1)
            cmt
        }
        val c3 = run {
            val cmt = this.testData.commits[2]
            cmt.parents.add(c1)
            cmt
        }
        val c4 = run {
            val cmt = this.testData.commits[3]
            cmt.parents.add(c2)
            cmt.parents.add(c3)
            cmt
        }
        val vcsCommits = listOf(c1, c2, c3, c4)

        val transformedCommits = repositoryService.transformCommits(this.simpleRepo, vcsCommits)
        val commitMap = transformedCommits.associateBy { it.sha }

        assertThat(commitMap["a".repeat(40)]!!.parents).isEmpty()
        assertThat(
            commitMap["b".repeat(40)]!!.parents.map {
                it.sha
            },
        ).containsExactly("a".repeat(40))
        assertThat(
            commitMap["c".repeat(40)]!!.parents.map {
                it.sha
            },
        ).containsExactly("a".repeat(40))
        assertThat(
            commitMap["d".repeat(40)]!!.parents.map {
                it.sha
            },
        ).containsExactlyInAnyOrder(
            "b".repeat(40),
            "c".repeat(40)
        )

        // Check object identity for parents
        assertThat(
            commitMap["b".repeat(40)]!!.parents.first(),
        ).isSameAs(commitMap["a".repeat(40)]!!)
        assertThat(
            commitMap["c".repeat(40)]!!.parents.first(),
        ).isSameAs(commitMap["a".repeat(40)]!!)
        assertThat(
            commitMap["d".repeat(40)]!!.parents,
        ).containsExactlyInAnyOrder(
            commitMap["b".repeat(40)]!!,
            commitMap["c".repeat(40)]!!,
        )
    }

    @Test
    fun `transformCommits with different committer and author sharing same email`() {
        val vcsCommits = run {
            val cmt = this.testData.commits[0]
            // user has same email, should be mapped to User A in transformCommits
            val user = User(name = "User B", email = "a@test.com")
            val repo = this.simpleRepo
            require(repo.user.add(user))
            cmt.author = user
            listOf(cmt)
        }
        val transformedCommits = repositoryService.transformCommits(this.simpleRepo, vcsCommits)
        val commit = transformedCommits.first()
        assertThat(commit.committer).isNotNull()
        assertThat(commit.author).isNotNull()
        assertThat(commit.committer).isSameAs(commit.author) // Should be the same User entity instance
        assertThat(commit.committer?.email).isEqualTo("a@test.com")
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
                    assertThat(commit.branches.map { it.name }).containsExactlyInAnyOrder(*originalVcsCommit.branches.map { it.name }
                        .toTypedArray())
                    assertThat(commit.commitDateTime).isEqualTo(originalVcsCommit.commitDateTime)
                    assertThat(commit.authorDateTime).isEqualTo(originalVcsCommit.authorDateTime)

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
                localPath = "new one"
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
        val user = this.testData.users[0]
        val parent = run {
            val cmt = this.testData.commits[0]
//            cmt.committer = user
//            cmt.author = user
            cmt
        }
        val child = run {
            val cmt = this.testData.commits[1]
            // TODO use reflection here?
//            cmt.committer = user
//            cmt.author = user
            cmt.parents.add(parent)
            cmt
        }

        // Order: child first, then parent
        val vcsCommitsOutOfOrder = listOf(child, parent)
        val transformedCommits = repositoryService.transformCommits(this.simpleRepo, vcsCommitsOutOfOrder)
        val commitMap = transformedCommits.associateBy { it.sha }

        val transformedChild = commitMap["b".repeat(40)]
        val transformedParent = commitMap["a".repeat(40)]

        assertThat(transformedChild).isNotNull()
        assertThat(transformedParent).isNotNull()
        assertThat(transformedChild!!.parents).hasSize(1)
        assertThat(transformedChild.parents.first().sha).isEqualTo("a".repeat(40))
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
