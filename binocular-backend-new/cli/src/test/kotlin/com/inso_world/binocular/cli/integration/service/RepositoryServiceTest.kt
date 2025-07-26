package com.inso_world.binocular.cli.integration.service

import com.inso_world.binocular.cli.index.vcs.VcsCommit
import com.inso_world.binocular.cli.index.vcs.VcsPerson
import com.inso_world.binocular.cli.index.vcs.toDtos
import com.inso_world.binocular.cli.integration.TestDataSetupService
import com.inso_world.binocular.cli.integration.service.base.BaseServiceTest
import com.inso_world.binocular.cli.service.RepositoryService
import com.inso_world.binocular.ffi.BinocularFfi
import com.inso_world.binocular.ffi.pojos.BinocularCommitPojo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
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
                    setOf(),
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
                setOf(c1),
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
                setOf(c1),
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
                setOf(c2, c3)
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
                    setOf(
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

        val transformedCommits = repositoryService.transformCommits(this.simpleRepo, simpleRepoVcsCommits)
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
                assertThat(transformedCommitMap[parentCommit.sha]).isSameAs(parentCommit)
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
                setOf(parent),
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
}
