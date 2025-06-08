package com.inso_world.binocular.web.graphql

import com.inso_world.binocular.web.BaseDbTest
import com.inso_world.binocular.web.entity.Branch
import com.inso_world.binocular.web.exception.NotFoundException
import com.inso_world.binocular.web.exception.ValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BranchControllerWebTest : BaseDbTest() {

    @Test
    fun `should return all branches`() {
        // Test data is set up in BaseDbTest
        val result = graphQlTester.document("""
            query {
                branches(page: 1, perPage: 100) {
                    id
                    branch
                    active
                    tracksFileRenames
                    latestCommit
                }
            }
        """)
        .execute()
        .path("branches")
        .entityList(Branch::class.java)

        // Check size
        result.hasSize(2)

        // Get the branches from the result
        val branches = result.get()

        // Check that the branches match the test data
        testBranches.forEachIndexed { index, expectedBranch ->
            val actualBranch = branches[index]
            assert(actualBranch.id == expectedBranch.id) { "Branch ID mismatch: expected ${expectedBranch.id}, got ${actualBranch.id}" }
            assert(actualBranch.branch == expectedBranch.branch) { "Branch name mismatch: expected ${expectedBranch.branch}, got ${actualBranch.branch}" }
            assert(actualBranch.active == expectedBranch.active) { "Branch active mismatch: expected ${expectedBranch.active}, got ${actualBranch.active}" }
            assert(actualBranch.tracksFileRenames == expectedBranch.tracksFileRenames) { "Branch tracksFileRenames mismatch: expected ${expectedBranch.tracksFileRenames}, got ${actualBranch.tracksFileRenames}" }
            assert(actualBranch.latestCommit == expectedBranch.latestCommit) { "Branch latestCommit mismatch: expected ${expectedBranch.latestCommit}, got ${actualBranch.latestCommit}" }
        }
    }

    @Test
    fun `should return branch by id`() {
        // Test data is set up in BaseDbTest
        val expectedBranch = testBranches.first { it.id == "1" }

        val result = graphQlTester.document("""
            query {
                branch(id: "1") {
                    id
                    branch
                    active
                    tracksFileRenames
                    latestCommit
                }
            }
        """)
        .execute()
        .path("branch")

        // Check that the branch exists
        result.hasValue()

        // Get the branch from the result
        val actualBranch = result.entity(Branch::class.java).get()

        // Check that the branch matches the test data
        assert(actualBranch.id == expectedBranch.id) { "Branch ID mismatch: expected ${expectedBranch.id}, got ${actualBranch.id}" }
        assert(actualBranch.branch == expectedBranch.branch) { "Branch name mismatch: expected ${expectedBranch.branch}, got ${actualBranch.branch}" }
        assert(actualBranch.active == expectedBranch.active) { "Branch active mismatch: expected ${expectedBranch.active}, got ${actualBranch.active}" }
        assert(actualBranch.tracksFileRenames == expectedBranch.tracksFileRenames) { "Branch tracksFileRenames mismatch: expected ${expectedBranch.tracksFileRenames}, got ${actualBranch.tracksFileRenames}" }
        assert(actualBranch.latestCommit == expectedBranch.latestCommit) { "Branch latestCommit mismatch: expected ${expectedBranch.latestCommit}, got ${actualBranch.latestCommit}" }
    }

    @Test
    fun `should return branches with pagination`() {
        // Test with page=1, perPage=1 (should return only the first branch)
        val result = graphQlTester.document("""
            query {
                branches(page: 1, perPage: 1) {
                    id
                    branch
                    active
                    tracksFileRenames
                }
            }
        """)
        .execute()
        .path("branches")
        .entityList(Branch::class.java)

        // Check size
        result.hasSize(1)

        // Get the branches from the result
        val branches = result.get()

        // Check that the branch matches the first test branch
        val expectedBranch = testBranches.first()
        val actualBranch = branches.first()
        assert(actualBranch.id == expectedBranch.id) { "Branch ID mismatch: expected ${expectedBranch.id}, got ${actualBranch.id}" }
        assert(actualBranch.branch == expectedBranch.branch) { "Branch name mismatch: expected ${expectedBranch.branch}, got ${actualBranch.branch}" }
        assert(actualBranch.active == expectedBranch.active) { "Branch active mismatch: expected ${expectedBranch.active}, got ${actualBranch.active}" }
        assert(actualBranch.tracksFileRenames == expectedBranch.tracksFileRenames) { "Branch tracksFileRenames mismatch: expected ${expectedBranch.tracksFileRenames}, got ${actualBranch.tracksFileRenames}" }
    }

    @Test
    fun `should throw exception for non-existent branch id`() {
        // Test with a non-existent branch ID
        val nonExistentId = "999"

        graphQlTester.document("""
            query {
                branch(id: "$nonExistentId") {
                    id
                    branch
                    active
                }
            }
        """)
        .execute()
        .errors()
        .expect { error ->
            error.message?.contains("Branch not found with id: $nonExistentId") ?: false
        }
        .verify()
    }

    @Test
    fun `should throw exception for invalid pagination parameters`() {
        // Test with invalid page parameter
        graphQlTester.document("""
            query {
                branches(page: 0, perPage: 10) {
                    id
                    branch
                    active
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
                branches(page: 1, perPage: 0) {
                    id
                    branch
                    active
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
                branches {
                    id
                    branch
                    active
                }
            }
        """)
        .execute()
        .path("branches")
        .entityList(Branch::class.java)

        // Check size (should return all branches with default pagination)
        result.hasSize(2)
    }

    @Test
    fun `should return second page of branches`() {
        // Test with page=2, perPage=1 (should return only the second branch)
        val result = graphQlTester.document("""
            query {
                branches(page: 2, perPage: 1) {
                    id
                    branch
                    active
                }
            }
        """)
        .execute()
        .path("branches")
        .entityList(Branch::class.java)

        // Check size
        result.hasSize(1)

        // Get the branches from the result
        val branches = result.get()

        // Check that the branch matches the second test branch
        val expectedBranch = testBranches[1]
        val actualBranch = branches.first()
        assert(actualBranch.id == expectedBranch.id) { "Branch ID mismatch: expected ${expectedBranch.id}, got ${actualBranch.id}" }
        assert(actualBranch.branch == expectedBranch.branch) { "Branch name mismatch: expected ${expectedBranch.branch}, got ${actualBranch.branch}" }
        assert(actualBranch.active == expectedBranch.active) { "Branch active mismatch: expected ${expectedBranch.active}, got ${actualBranch.active}" }
    }
}
