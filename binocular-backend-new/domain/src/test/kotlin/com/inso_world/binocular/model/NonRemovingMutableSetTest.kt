package com.inso_world.binocular.model

import com.inso_world.binocular.model.utils.ReflectionUtils.Companion.setField
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class NonRemovingMutableSetTest {

    private fun project(name: String = "P"): Project = Project(name)
    private fun repo(localPath: String = "/tmp/repo", project: Project = project()) =
        Repository(localPath = localPath, project = project)

    @Nested
    inner class Correctness_BusinessKeyDedupe {

        @Test
        fun `add multiple branches at once with duplicates, expect unique to be added`() {
            val repository = repo()

            val branchA = Branch(name = "feature/branch-a", repository = repository)
            val branchB = Branch(name = "feature/branch-b", repository = repository)
            val branchC = Branch(name = "feature/branch-a", repository = repository) // duplicate by business key

            val list = listOf(branchA, branchB, branchC)
            assertThat(list).hasSize(3)

            // NonRemovingMutableSet dedupes by uniqueKey (repo.iid + name) -> size 2
            assertThat(repository.branches).hasSize(2)
            assertThat(repository.branches.map { it.name }).containsExactlyInAnyOrder("feature/branch-a", "feature/branch-b")
        }

        @Test
        fun `same branch name across different repositories are independent`() {
            val p = project()
            val r1 = repo("/tmp/r1", p)
            val r2 = repo("/tmp/r2", project())
            setField(
                r2.javaClass.getDeclaredField("project"),
                r2,
                r1.project,
            )

            val b1 = Branch(name = "feature/x", repository = r1)
            val b2 = Branch(name = "feature/x", repository = r2) // same name, different repo

            assertThat(r1.branches).contains(b1)
            assertThat(r2.branches).contains(b2)
            assertThat(r1.branches).hasSize(1)
            assertThat(r2.branches).hasSize(1)
        }

        @Test
        fun `cannot add a Branch tied to repo_A into repo_B`() {
            val rA = repo("/tmp/A")
            val rB = repo("/tmp/B")

            val bA = Branch(name = "dev", repository = rA)

            assertThatThrownBy { rB.branches.add(bA) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }
    }

    @Nested
    inner class Contract_NoRemovalAllowed {

        @Test
        fun `clear is not allowed`() {
            val r = repo()
            Branch("a", repository = r)
            assertThatThrownBy { r.branches.clear() }
                .isInstanceOf(UnsupportedOperationException::class.java)
        }

        @Test
        fun `remove and retain operations are not allowed`() {
            val r = repo()
            val b = Branch("a", repository = r)
            assertThatThrownBy { r.branches.remove(b) }
                .isInstanceOf(UnsupportedOperationException::class.java)
            assertThatThrownBy { r.branches.removeAll(setOf(b)) }
                .isInstanceOf(UnsupportedOperationException::class.java)
            assertThatThrownBy { r.branches.retainAll(emptyList()) }
                .isInstanceOf(UnsupportedOperationException::class.java)
        }

        @Test
        fun `iterator remove is not allowed`() {
            val r = repo()
            Branch("a", repository = r)
            val it = r.branches.iterator()
            assertTrue(it.hasNext())
            assertThat(it.next()).isInstanceOf(Branch::class.java)
            assertThatThrownBy { it.remove() }
                .isInstanceOf(UnsupportedOperationException::class.java)
        }
    }

    @Nested
    inner class Contains_Semantics {

        @Test
        fun `contains uses business key, not object identity`() {
            val r = repo()
            val original = Branch("feat/same", repository = r)
            val probe = Branch("feat/same", repository = r) // rejected from add, but shares same uniqueKey

            // The second instance was not inserted, but contains must still be true via uniqueKey
            assertTrue(r.branches.contains(probe))
            assertThat(r.branches).hasSize(1)
            assertThat(r.branches.first()).isEqualTo(original)
        }

        @Test
        fun `containsAll should accept collections of elements with same unique keys`() {
            val r = repo()
            val b1 = Branch("a", repository = r)
            val b2 = Branch("b", repository = r)
            val probeA = Branch("a", repository = r) // not inserted; unique-key equal to b1

            // Expected: true (once containsAll maps elements -> uniqueKey)
            assertTrue(r.branches.containsAll(listOf(b1, probeA, b2)))
        }
    }

    @Nested
    inner class Concurrency_DeDuplication {

        @Test
        fun `concurrent inserts of same business keys do not produce duplicates`() {
            val r = repo()
            val pool = Executors.newFixedThreadPool(8)
            val start = CountDownLatch(1)
            val done = CountDownLatch(100)

            // Names with duplicates
            val names = listOf("a", "b", "c", "d", "e")
            repeat(100) {
                pool.submit {
                    try {
                        start.await()
                        // Choose a name deterministically
                        val n = names[it % names.size]
                        Branch(n, repository = r) // Branch init attempts to add
                    } finally {
                        done.countDown()
                    }
                }
            }

            start.countDown()
            done.await(10, TimeUnit.SECONDS)
            pool.shutdown()

            assertThat(r.branches.map { it.name }.toSet())
                .containsExactlyInAnyOrderElementsOf(names)
            assertThat(r.branches).hasSize(names.size)
        }
    }
}
