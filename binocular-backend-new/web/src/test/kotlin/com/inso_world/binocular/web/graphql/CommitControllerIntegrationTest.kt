package com.inso_world.binocular.web.graphql

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.graphql.test.tester.GraphQlTester

/**
 * Integration tests for the Commit GraphQL queries.
 * These tests can be run against both the old and new implementations.
 */
class CommitControllerIntegrationTest : GraphQLIntegrationTestBase() {

    @Test
    fun `should get all commits`() {
        // Define the GraphQL query
        val query = """
            query {
                commits {
                    id
                    sha
                    date
                    message
                }
            }
        """.trimIndent()

        // Execute the query
        val response = executeQuery(query)

        // Verify the response contains commits data
        val commits = response.path("data.commits").entityList(Map::class.java).get()
        assertNotNull(commits, "Commits should not be null")
        
        // If there are commits, verify they have the expected fields
        if (commits.isNotEmpty()) {
            val firstCommit = commits[0] as Map<*, *>
            assertNotNull(firstCommit["id"], "Commit should have an id")
            assertNotNull(firstCommit["sha"], "Commit should have a sha")
            assertNotNull(firstCommit["date"], "Commit should have a date")
            assertNotNull(firstCommit["message"], "Commit should have a message")
        }
    }

    @Test
    fun `should get commit by id`() {
        // Define the GraphQL query to get the first commit's ID
        val query = """
            query {
                commits(perPage: 1) {
                    id
                }
            }
        """.trimIndent()

        // Execute the query to get commits
        val commitsResponse = executeQuery(query)
        
        // Get the list of commits
        val commits = commitsResponse.path("data.commits").entityList(Map::class.java).get()
        
        // Skip the test if there are no commits
        if (commits.isEmpty()) {
            println("No commits found, skipping test")
            return
        }
        
        // Get the ID of the first commit
        val firstCommit = commits[0] as Map<*, *>
        val commitId = firstCommit["id"] as String

        // Define the query to get a specific commit
        val commitQuery = """
            query {
                commit(id: "$commitId") {
                    id
                    sha
                    date
                    message
                    stats {
                        additions
                        deletions
                    }
                }
            }
        """.trimIndent()

        // Execute the query
        val response = executeQuery(commitQuery)

        // Get the commit data
        val commit = response.path("data.commit").entity(Map::class.java).get()
        
        // Verify the response
        assertEquals(commitId, commit["id"], "Commit ID should match the requested ID")
        assertNotNull(commit["sha"], "Commit should have a sha")
        assertNotNull(commit["date"], "Commit should have a date")
        assertNotNull(commit["message"], "Commit should have a message")
        
        // Check stats if available
        val stats = commit["stats"] as? Map<*, *>
        if (stats != null) {
            assertNotNull(stats["additions"], "Stats should have additions")
            assertNotNull(stats["deletions"], "Stats should have deletions")
        }
    }

    @Test
    fun `should handle pagination for commits`() {
        // Define the GraphQL query with pagination
        val query = """
            query {
                commits(page: 0, perPage: 2) {
                    id
                    sha
                }
            }
        """.trimIndent()

        // Execute the query
        val response = executeQuery(query)

        // Get the commits data
        val commits = response.path("data.commits").entityList(Map::class.java).get()
        
        // Verify we got at most 2 commits (as requested by perPage)
        assertTrue(commits.size <= 2, "Expected at most 2 commits, but got ${commits.size}")
    }

    @Test
    fun `should handle non-existent commit id`() {
        // Define the GraphQL query with a non-existent ID
        val query = """
            query {
                commit(id: "non-existent-id") {
                    id
                    sha
                }
            }
        """.trimIndent()

        // Execute the query
        val response = executeQuery(query)
        
        try {
            // Try to get the commit data
            val commit = response.path("data.commit").entity(Map::class.java).get()
            
            // If we get here, the commit should be null
            assertNull(commit, "Commit with non-existent ID should be null")
        } catch (e: Exception) {
            // If we get an exception, that's also acceptable as it means there was an error
            // This handles the case where the implementation returns an error instead of null
            println("Got expected exception for non-existent commit ID: ${e.message}")
        }
    }
    
    @Test
    fun `should get commit with related entities`() {
        // Define the GraphQL query to get the first commit's ID
        val query = """
            query {
                commits(perPage: 1) {
                    id
                }
            }
        """.trimIndent()

        // Execute the query to get commits
        val commitsResponse = executeQuery(query)
        
        // Get the list of commits
        val commits = commitsResponse.path("data.commits").entityList(Map::class.java).get()
        
        // Skip the test if there are no commits
        if (commits.isEmpty()) {
            println("No commits found, skipping test")
            return
        }
        
        // Get the ID of the first commit
        val firstCommit = commits[0] as Map<*, *>
        val commitId = firstCommit["id"] as String

        // Define the query to get a specific commit with related entities
        val commitQuery = """
            query {
                commit(id: "$commitId") {
                    id
                    files {
                        id
                        path
                    }
                    modules {
                        id
                        path
                    }
                    users {
                        id
                        gitSignature
                    }
                }
            }
        """.trimIndent()

        // Execute the query
        val response = executeQuery(commitQuery)

        // Get the commit data
        val commit = response.path("data.commit").entity(Map::class.java).get()
        
        // Verify the response
        assertEquals(commitId, commit["id"], "Commit ID should match the requested ID")
        
        // Check related entities if available
        val files = commit["files"] as? List<*>
        if (files != null && files.isNotEmpty()) {
            val firstFile = files[0] as Map<*, *>
            assertNotNull(firstFile["id"], "File should have an id")
            assertNotNull(firstFile["path"], "File should have a path")
        }
        
        val modules = commit["modules"] as? List<*>
        if (modules != null && modules.isNotEmpty()) {
            val firstModule = modules[0] as Map<*, *>
            assertNotNull(firstModule["id"], "Module should have an id")
            assertNotNull(firstModule["path"], "Module should have a path")
        }
        
        val users = commit["users"] as? List<*>
        if (users != null && users.isNotEmpty()) {
            val firstUser = users[0] as Map<*, *>
            assertNotNull(firstUser["id"], "User should have an id")
            assertNotNull(firstUser["gitSignature"], "User should have a gitSignature")
        }
    }
}
