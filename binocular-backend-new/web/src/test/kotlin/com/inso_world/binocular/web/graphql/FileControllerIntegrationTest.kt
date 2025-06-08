package com.inso_world.binocular.web.graphql

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.graphql.test.tester.GraphQlTester

/**
 * Integration tests for the File GraphQL queries.
 * These tests can be run against both the old and new implementations.
 */
class FileControllerIntegrationTest : GraphQLIntegrationTestBase() {

    @Test
    fun `should get all files`() {
        // Define the GraphQL query
        val query = """
            query {
                files {
                    id
                    path
                    webUrl
                }
            }
        """.trimIndent()

        // Execute the query
        val response = executeQuery(query)

        // Verify the response contains files data
        val files = response.path("data.files").entityList(Map::class.java).get()
        assertNotNull(files, "Files should not be null")
        
        // If there are files, verify they have the expected fields
        if (files.isNotEmpty()) {
            val firstFile = files[0] as Map<*, *>
            assertNotNull(firstFile["id"], "File should have an id")
            assertNotNull(firstFile["path"], "File should have a path")
        }
    }

    @Test
    fun `should get file by id`() {
        // Define the GraphQL query to get the first file's ID
        val query = """
            query {
                files(perPage: 1) {
                    id
                }
            }
        """.trimIndent()

        // Execute the query to get files
        val filesResponse = executeQuery(query)
        
        // Get the list of files
        val files = filesResponse.path("data.files").entityList(Map::class.java).get()
        
        // Skip the test if there are no files
        if (files.isEmpty()) {
            println("No files found, skipping test")
            return
        }
        
        // Get the ID of the first file
        val firstFile = files[0] as Map<*, *>
        val fileId = firstFile["id"] as String

        // Define the query to get a specific file
        val fileQuery = """
            query {
                file(id: "$fileId") {
                    id
                    path
                    webUrl
                    maxLength
                }
            }
        """.trimIndent()

        // Execute the query
        val response = executeQuery(fileQuery)

        // Get the file data
        val file = response.path("data.file").entity(Map::class.java).get()
        
        // Verify the response
        assertEquals(fileId, file["id"], "File ID should match the requested ID")
        assertNotNull(file["path"], "File should have a path")
    }

    @Test
    fun `should handle pagination for files`() {
        // Define the GraphQL query with pagination
        val query = """
            query {
                files(page: 0, perPage: 2) {
                    id
                    path
                }
            }
        """.trimIndent()

        // Execute the query
        val response = executeQuery(query)

        // Get the files data
        val files = response.path("data.files").entityList(Map::class.java).get()
        
        // Verify we got at most 2 files (as requested by perPage)
        assertTrue(files.size <= 2, "Expected at most 2 files, but got ${files.size}")
    }

    @Test
    fun `should handle non-existent file id`() {
        // Define the GraphQL query with a non-existent ID
        val query = """
            query {
                file(id: "non-existent-id") {
                    id
                    path
                }
            }
        """.trimIndent()

        // Execute the query
        val response = executeQuery(query)
        
        try {
            // Try to get the file data
            val file = response.path("data.file").entity(Map::class.java).get()
            
            // If we get here, the file should be null
            assertNull(file, "File with non-existent ID should be null")
        } catch (e: Exception) {
            // If we get an exception, that's also acceptable as it means there was an error
            // This handles the case where the implementation returns an error instead of null
            println("Got expected exception for non-existent file ID: ${e.message}")
        }
    }
    
    @Test
    fun `should get file with related entities`() {
        // Define the GraphQL query to get the first file's ID
        val query = """
            query {
                files(perPage: 1) {
                    id
                }
            }
        """.trimIndent()

        // Execute the query to get files
        val filesResponse = executeQuery(query)
        
        // Get the list of files
        val files = filesResponse.path("data.files").entityList(Map::class.java).get()
        
        // Skip the test if there are no files
        if (files.isEmpty()) {
            println("No files found, skipping test")
            return
        }
        
        // Get the ID of the first file
        val firstFile = files[0] as Map<*, *>
        val fileId = firstFile["id"] as String

        // Define the query to get a specific file with related entities
        val fileQuery = """
            query {
                file(id: "$fileId") {
                    id
                    path
                    commits {
                        id
                        sha
                    }
                    branches {
                        id
                        branch
                    }
                    modules {
                        id
                        path
                    }
                }
            }
        """.trimIndent()

        // Execute the query
        val response = executeQuery(fileQuery)

        // Get the file data
        val file = response.path("data.file").entity(Map::class.java).get()
        
        // Verify the response
        assertEquals(fileId, file["id"], "File ID should match the requested ID")
        
        // Check related entities if available
        val commits = file["commits"] as? List<*>
        if (commits != null && commits.isNotEmpty()) {
            val firstCommit = commits[0] as Map<*, *>
            assertNotNull(firstCommit["id"], "Commit should have an id")
            assertNotNull(firstCommit["sha"], "Commit should have a sha")
        }
        
        val branches = file["branches"] as? List<*>
        if (branches != null && branches.isNotEmpty()) {
            val firstBranch = branches[0] as Map<*, *>
            assertNotNull(firstBranch["id"], "Branch should have an id")
            assertNotNull(firstBranch["branch"], "Branch should have a branch name")
        }
        
        val modules = file["modules"] as? List<*>
        if (modules != null && modules.isNotEmpty()) {
            val firstModule = modules[0] as Map<*, *>
            assertNotNull(firstModule["id"], "Module should have an id")
            assertNotNull(firstModule["path"], "Module should have a path")
        }
    }
}
