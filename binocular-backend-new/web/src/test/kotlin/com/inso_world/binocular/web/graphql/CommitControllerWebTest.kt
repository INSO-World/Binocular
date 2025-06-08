package com.inso_world.binocular.web.graphql

import com.inso_world.binocular.web.BaseDbTest
import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.exception.NotFoundException
import com.inso_world.binocular.web.exception.ValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CommitControllerWebTest : BaseDbTest() {

    @Test
    fun `should return all commits`() {
        // Test data is set up in BaseDbTest
        val result = graphQlTester.document("""
            query {
                commits(page: 1, perPage: 100) {
                    id
                    sha
                    message
                    branch
                    webUrl
                    stats {
                        additions
                        deletions
                    }
                }
            }
        """)
        .execute()
        .path("commits")
        .entityList(Commit::class.java)

        // Check size
        result.hasSize(2)

        // Get the commits from the result
        val commits = result.get()

        // Check that the commits match the test data
        testCommits.forEachIndexed { index, expectedCommit ->
            val actualCommit = commits[index]
            assert(actualCommit.id == expectedCommit.id) { "Commit ID mismatch: expected ${expectedCommit.id}, got ${actualCommit.id}" }
            assert(actualCommit.sha == expectedCommit.sha) { "Commit SHA mismatch: expected ${expectedCommit.sha}, got ${actualCommit.sha}" }
            assert(actualCommit.message == expectedCommit.message) { "Commit message mismatch: expected ${expectedCommit.message}, got ${actualCommit.message}" }
            assert(actualCommit.branch == expectedCommit.branch) { "Commit branch mismatch: expected ${expectedCommit.branch}, got ${actualCommit.branch}" }
            assert(actualCommit.webUrl == expectedCommit.webUrl) { "Commit webUrl mismatch: expected ${expectedCommit.webUrl}, got ${actualCommit.webUrl}" }
            assert(actualCommit.stats?.additions == expectedCommit.stats?.additions) { "Commit additions mismatch: expected ${expectedCommit.stats?.additions}, got ${actualCommit.stats?.additions}" }
            assert(actualCommit.stats?.deletions == expectedCommit.stats?.deletions) { "Commit deletions mismatch: expected ${expectedCommit.stats?.deletions}, got ${actualCommit.stats?.deletions}" }
        }
    }

    @Test
    fun `should return commit by id`() {
        // Test data is set up in BaseDbTest
        val expectedCommit = testCommits.first { it.id == "1" }

        val result = graphQlTester.document("""
            query {
                commit(id: "1") {
                    id
                    sha
                    message
                    branch
                    webUrl
                    stats {
                        additions
                        deletions
                    }
                }
            }
        """)
        .execute()
        .path("commit")

        // Check that the commit exists
        result.hasValue()

        // Get the commit from the result
        val actualCommit = result.entity(Commit::class.java).get()

        // Check that the commit matches the test data
        assert(actualCommit.id == expectedCommit.id) { "Commit ID mismatch: expected ${expectedCommit.id}, got ${actualCommit.id}" }
        assert(actualCommit.sha == expectedCommit.sha) { "Commit SHA mismatch: expected ${expectedCommit.sha}, got ${actualCommit.sha}" }
        assert(actualCommit.message == expectedCommit.message) { "Commit message mismatch: expected ${expectedCommit.message}, got ${actualCommit.message}" }
        assert(actualCommit.branch == expectedCommit.branch) { "Commit branch mismatch: expected ${expectedCommit.branch}, got ${actualCommit.branch}" }
        assert(actualCommit.webUrl == expectedCommit.webUrl) { "Commit webUrl mismatch: expected ${expectedCommit.webUrl}, got ${actualCommit.webUrl}" }
        assert(actualCommit.stats?.additions == expectedCommit.stats?.additions) { "Commit additions mismatch: expected ${expectedCommit.stats?.additions}, got ${actualCommit.stats?.additions}" }
        assert(actualCommit.stats?.deletions == expectedCommit.stats?.deletions) { "Commit deletions mismatch: expected ${expectedCommit.stats?.deletions}, got ${actualCommit.stats?.deletions}" }
    }

    @Test
    fun `should return commits with pagination`() {
        // Test with page=1, perPage=1 (should return only the first commit)
        val result = graphQlTester.document("""
            query {
                commits(page: 1, perPage: 1) {
                    id
                    sha
                    message
                }
            }
        """)
        .execute()
        .path("commits")
        .entityList(Commit::class.java)

        // Check size
        result.hasSize(1)

        // Get the commits from the result
        val commits = result.get()

        // Check that the commit matches the first test commit
        val expectedCommit = testCommits.first()
        val actualCommit = commits.first()
        assert(actualCommit.id == expectedCommit.id) { "Commit ID mismatch: expected ${expectedCommit.id}, got ${actualCommit.id}" }
        assert(actualCommit.sha == expectedCommit.sha) { "Commit SHA mismatch: expected ${expectedCommit.sha}, got ${actualCommit.sha}" }
        assert(actualCommit.message == expectedCommit.message) { "Commit message mismatch: expected ${expectedCommit.message}, got ${actualCommit.message}" }
    }

    @Test
    fun `should throw exception for non-existent commit id`() {
        // Test with a non-existent commit ID
        val nonExistentId = "999"

        val exception = graphQlTester.document("""
            query {
                commit(id: "$nonExistentId") {
                    id
                    sha
                    message
                }
            }
        """)
        .execute()
        .errors()
        .expect { error ->
            error.message?.contains("Commit not found with id: $nonExistentId") ?: false
        }
        .verify()
    }

    @Test
    fun `should throw exception for invalid pagination parameters`() {
        // Test with invalid page parameter
        graphQlTester.document("""
            query {
                commits(page: 0, perPage: 10) {
                    id
                    sha
                    message
                }
            }
        """)
        .execute()
        .errors()
        .expect { error ->
            error.message?.contains("Page must be greater than or equal to 1") ?: false
        }
        .verify()

        // Test with invalid perPage parameter
        graphQlTester.document("""
            query {
                commits(page: 1, perPage: 0) {
                    id
                    sha
                    message
                }
            }
        """)
        .execute()
        .errors()
        .expect { error ->
            error.message?.contains("PerPage must be greater than or equal to 1") ?: false
        }
        .verify()
    }

    @Test
    fun `should handle null pagination parameters`() {
        // Test with null page and perPage parameters (should use defaults)
        val result = graphQlTester.document("""
            query {
                commits {
                    id
                    sha
                    message
                }
            }
        """)
        .execute()
        .path("commits")
        .entityList(Commit::class.java)

        // Check size (should return all commits with default pagination)
        result.hasSize(2)
    }

    @Test
    fun `should return second page of commits`() {
        // Test with page=2, perPage=1 (should return only the second commit)
        val result = graphQlTester.document("""
            query {
                commits(page: 2, perPage: 1) {
                    id
                    sha
                    message
                }
            }
        """)
        .execute()
        .path("commits")
        .entityList(Commit::class.java)

        // Check size
        result.hasSize(1)

        // Get the commits from the result
        val commits = result.get()

        // Check that the commit matches the second test commit
        val expectedCommit = testCommits[1]
        val actualCommit = commits.first()
        assert(actualCommit.id == expectedCommit.id) { "Commit ID mismatch: expected ${expectedCommit.id}, got ${actualCommit.id}" }
        assert(actualCommit.sha == expectedCommit.sha) { "Commit SHA mismatch: expected ${expectedCommit.sha}, got ${actualCommit.sha}" }
        assert(actualCommit.message == expectedCommit.message) { "Commit message mismatch: expected ${expectedCommit.message}, got ${actualCommit.message}" }
    }
}
