package com.inso_world.binocular.cli.unit

import com.inso_world.binocular.cli.index.vcs.VcsCommit
import com.inso_world.binocular.cli.index.vcs.toDtos
import com.inso_world.binocular.cli.index.vcs.traverseGraph
import com.inso_world.binocular.ffi.BinocularFfi
import com.inso_world.binocular.ffi.pojos.BinocularCommitPojo
import com.inso_world.binocular.ffi.pojos.BinocularCommitSignaturePojo
import com.inso_world.binocular.ffi.pojos.BinocularTimePojo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertTimeout
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Base64
import kotlin.collections.contains

class VcsCommitToDtosTest {
//    private fun encodeMessage(msg: String): String =
//        Base64.getEncoder().encodeToString(msg.toByteArray(StandardCharsets.UTF_8))

    private fun signature(name: String, email: String, seconds: Long = 0L, offset: Int = 0) =
        BinocularCommitSignaturePojo(name, email, BinocularTimePojo(seconds, offset))

    @Test
    fun `empty list returns empty`() {
        val result = emptyList<BinocularCommitPojo>().toDtos()
        assertThat(result).isEmpty()
    }

    @Test
    fun `single commit with no parents`() {
        val pojo = BinocularCommitPojo(
            commit = "sha1",
            message = "msg1",
            committer = signature("committer", "c@test.com", 100),
            author = signature("author", "a@test.com", 200),
            branch = "main",
            parents = mutableSetOf()
        )
        val result = listOf(pojo).toDtos()
        assertAll(
            { assertThat(result).hasSize(1) },
            { assertThat(result[0].sha).isEqualTo("sha1") },
            { assertThat(result[0].message).isEqualTo("msg1") },
            { assertThat(result[0].branch).isEqualTo("main") },
            { assertThat(result[0].committer?.name).isEqualTo("committer") },
            { assertThat(result[0].author?.name).isEqualTo("author") },
            { assertThat(result[0].commitTime).isEqualTo(LocalDateTime.ofEpochSecond(100, 0, ZoneOffset.UTC)) },
            { assertThat(result[0].authorTime).isEqualTo(LocalDateTime.ofEpochSecond(200, 0, ZoneOffset.UTC)) },
            { assertThat(result[0].parents).isEmpty() }
        )
    }

    @Test
    fun `commit with one parent`() {
        val parent = BinocularCommitPojo(
            commit = "parent",
            message = "parent-msg",
            committer = signature("p", "p@test.com", 50),
            author = signature("pa", "pa@test.com", 60),
            branch = "main",
            parents = mutableSetOf()
        )
        val child = BinocularCommitPojo(
            commit = "child",
            message = "child-msg",
            committer = signature("c", "c@test.com", 100),
            author = signature("ca", "ca@test.com", 110),
            branch = "main",
            parents = mutableSetOf(parent)
        )
        val result = listOf(parent, child).toDtos()
        val childDto = result.find { it.sha == "child" }!!
        assertThat(childDto.parents).hasSize(1)
        assertThat(childDto.parents.first().sha).isEqualTo("parent")
    }

    @Test
    fun `commit with multiple parents (merge commit)`() {
        val p1 = BinocularCommitPojo(
            "p1",
            "m1",
            signature("a1", "a1@test.com", 1),
            signature("a1", "a1@test.com", 1),
            "main",
            mutableSetOf()
        )
        val p2 = BinocularCommitPojo(
            "p2",
            "m2",
            signature("a2", "a2@test.com", 2),
            signature("a2", "a2@test.com", 2),
            "main",
            mutableSetOf()
        )
        val merge = BinocularCommitPojo(
            "merge",
            "merge-msg",
            signature("m", "m@test.com", 3),
            signature("m", "m@test.com", 3),
            "main",
            mutableSetOf(p1, p2)
        )
        val result = listOf(p1, p2, merge).toDtos()
        val mergeDto = result.find { it.sha == "merge" }!!
        assertThat(mergeDto.parents.map { it.sha }).containsExactlyInAnyOrder("p1", "p2")
    }

    @Test
    fun `commit with null committer and author`() {
        val pojo = BinocularCommitPojo(
            commit = "sha1",
            message = "msg1",
            committer = null,
            author = null,
            branch = "main",
            parents = mutableSetOf()
        )
        val result = listOf(pojo).toDtos()
        assertThat(result[0].committer).isNull()
        assertThat(result[0].author).isNull()
    }

    @Test
    fun `commit with base64 message decodes correctly`() {
        val msg = "hello world!"
        val encoded = msg
        val pojo = BinocularCommitPojo(
            commit = "sha1",
            message = encoded,
            committer = signature("c", "c@test.com", 100),
            author = signature("a", "a@test.com", 200),
            branch = "main",
            parents = mutableSetOf()
        )
        val result = listOf(pojo).toDtos()
        assertThat(result[0].message).isEqualTo(msg)
    }

    @Nested
    inner class PermutationTests {
        @Test
        fun `permutations of parent order produce same parent set`() {
            val p1 = BinocularCommitPojo(
                "p1",
                "m1",
                signature("a1", "a1@test.com", 1),
                signature("a1", "a1@test.com", 1),
                "main",
                mutableSetOf()
            )
            val p2 = BinocularCommitPojo(
                "p2",
                "m2",
                signature("a2", "a2@test.com", 2),
                signature("a2", "a2@test.com", 2),
                "main",
                mutableSetOf()
            )
            val merge1 = BinocularCommitPojo(
                "merge",
                "merge-msg",
                signature("m", "m@test.com", 3),
                signature("m", "m@test.com", 3),
                "main",
                mutableSetOf(p1, p2)
            )
            val merge2 = BinocularCommitPojo(
                "merge",
                "merge-msg",
                signature("m", "m@test.com", 3),
                signature("m", "m@test.com", 3),
                "main",
                mutableSetOf(p2, p1)
            )
            val result1 = listOf(p1, p2, merge1).toDtos()
            val result2 = listOf(p2, p1, merge2).toDtos()
            val parents1 = result1.find { it.sha == "merge" }!!.parents.map { it.sha }.toSet()
            val parents2 = result2.find { it.sha == "merge" }!!.parents.map { it.sha }.toSet()
            assertThat(parents1).isEqualTo(parents2)
        }
    }

    @Nested
    @Disabled
    inner class PerformanceTests {
        private val ffi: BinocularFfi = BinocularFfi()
        private val repo = ffi.findRepo("../../.git")

        @ParameterizedTest
        @CsvSource(
            "origin/feature/5,219,20,18a0a9eb68d8c38d14b83da4faee8f40f53bd019",
            "origin/feature/31,592,22,fdc4fc21ed4eec0d0d49ec1677b590d67d361747",
            "origin/feature/32,658,22,23a50b9d695f87bc8ec42291b67c0e7b703c6742",
            "origin/main,1881,40,0f19849be9b57d56285a57ea31f3622f8ddecdf6",
            "origin/develop,2092,39,fb873518b4c369a6ff3ac0ec4d829124f9278c9e",
            "origin/feature/backend-new-gha,2238,38,92b51617d96b2f32ec60512d5bba429272cb8c7a"
        )
        fun `test mapping performance of Binocular repository`(branch: String, expectedCommits: Int, timeout: Long, headSha: String) {
            val results =
                assertTimeout(
                    Duration.ofMillis(timeout)
                ) { this.ffi.traverseBranch(repo, branch).toDtos() }

            assertThat(results).hasSize(expectedCommits)

            run {
                val graph: MutableMap<String, Any?> = mutableMapOf()

                val head = run {
                    val head = results.filter { it.sha == headSha }

                    assertThat(head).hasSize(1)

                    head[0]
                }

                head.traverseGraph(graph)
                assertThat(graph.values).hasSize(expectedCommits)
            }
        }

    }
}

fun VcsCommit.traverseGraph(recorder: MutableMap<String, Any?>) {
    recorder.computeIfAbsent(this.sha) {
        this.sha
    }
    this.parents.forEach { p ->
        if (!recorder.contains(p.sha)) {
            p.traverseGraph(recorder)
            recorder.computeIfAbsent(p.sha) { p.sha }
        }
    }
}
