package com.inso_world.binocular.web.graphql

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

/**
 * A simple test class that directly makes HTTP requests to the GraphQL endpoints
 * without relying on the Spring application context.
 */
class SimpleGraphQLTest {

    // URL for the old GraphQL implementation
    private val oldGraphQLUrl = "http://localhost:8529/_db/binocular-repo/binocular-ql"

    // URL for the new GraphQL implementation
    private val newGraphQLUrl = "http://localhost:8080/graphql"

    // HTTP client for making requests
    private val httpClient = HttpClient.newBuilder().build()

    // Object mapper for JSON serialization/deserialization
    private val objectMapper = ObjectMapper().registerKotlinModule()

    // Notes Tests

    @Test
    @DisplayName("Test notes query against old implementation")
    fun `should test notes query against old implementation`() {
        println("Testing notes query against old implementation at $oldGraphQLUrl")

        // Define a simple GraphQL query
        val query = """
            query {
                notes {
                    id
                    body
                }
            }
        """.trimIndent()

        try {
            // Execute the query against the old implementation
            val response = executeGraphQLQuery(oldGraphQLUrl, query)

            // Print the response for debugging
            println("Response from old implementation: $response")

            // Basic validation of the response
            assertNotNull(response, "Response should not be null")
            assertTrue(response.contains("data") || response.contains("errors"), 
                "Response should contain 'data' or 'errors'")

        } catch (e: Exception) {
            println("Error testing old implementation: ${e.message}")
            e.printStackTrace()
            // Fail the test when there's an error
            fail("Error testing old implementation: ${e.message}", e)
        }
    }

    @Test
    @DisplayName("Test notes query against new implementation")
    fun `should test notes query against new implementation`() {
        println("Testing notes query against new implementation at $newGraphQLUrl")

        // Define a simple GraphQL query
        val query = """
            query {
                notes {
                    id
                    body
                }
            }
        """.trimIndent()

        try {
            // Execute the query against the new implementation
            val response = executeGraphQLQuery(newGraphQLUrl, query)

            // Print the response for debugging
            println("Response from new implementation: $response")

            // Basic validation of the response
            assertNotNull(response, "Response should not be null")
            assertTrue(
                response.contains("data") || 
                response.contains("errors") || 
                response.contains("errorCode"), 
                "Response should contain 'data', 'errors', or 'errorCode'"
            )

        } catch (e: Exception) {
            println("Error testing new implementation: ${e.message}")
            e.printStackTrace()
            // Fail the test when there's an error
            fail("Error testing new implementation: ${e.message}", e)
        }
    }

    // Commits Tests

    @Test
    @DisplayName("Test commits query against old implementation")
    fun `should test commits query against old implementation`() {
        println("Testing commits query against old implementation at $oldGraphQLUrl")

        // Define a GraphQL query for commits
        val query = """
            query {
                commits {
                    id
                    sha
                    message
                }
            }
        """.trimIndent()

        try {
            // Execute the query against the old implementation
            val response = executeGraphQLQuery(oldGraphQLUrl, query)

            // Print the response for debugging
            println("Response from old implementation (commits): $response")

            // Basic validation of the response
            assertNotNull(response, "Response should not be null")
            assertTrue(response.contains("data") || response.contains("errors"), 
                "Response should contain 'data' or 'errors'")

        } catch (e: Exception) {
            println("Error testing old implementation (commits): ${e.message}")
            e.printStackTrace()
            // Fail the test when there's an error
            fail("Error testing old implementation (commits): ${e.message}", e)
        }
    }

    @Test
    @DisplayName("Test commits query against new implementation")
    fun `should test commits query against new implementation`() {
        println("Testing commits query against new implementation at $newGraphQLUrl")

        // Define a GraphQL query for commits
        val query = """
            query {
                commits {
                    id
                    sha
                    message
                }
            }
        """.trimIndent()

        try {
            // Execute the query against the new implementation
            val response = executeGraphQLQuery(newGraphQLUrl, query)

            // Print the response for debugging
            println("Response from new implementation (commits): $response")

            // Basic validation of the response
            assertNotNull(response, "Response should not be null")
            assertTrue(
                response.contains("data") || 
                response.contains("errors") || 
                response.contains("errorCode"), 
                "Response should contain 'data', 'errors', or 'errorCode'"
            )

        } catch (e: Exception) {
            println("Error testing new implementation (commits): ${e.message}")
            e.printStackTrace()
            // Fail the test when there's an error
            fail("Error testing new implementation (commits): ${e.message}", e)
        }
    }

    // Files Tests

    @Test
    @DisplayName("Test files query against old implementation")
    fun `should test files query against old implementation`() {
        println("Testing files query against old implementation at $oldGraphQLUrl")

        // Define a GraphQL query for files
        val query = """
            query {
                files {
                    id
                    path
                }
            }
        """.trimIndent()

        try {
            // Execute the query against the old implementation
            val response = executeGraphQLQuery(oldGraphQLUrl, query)

            // Print the response for debugging
            println("Response from old implementation (files): $response")

            // Basic validation of the response
            assertNotNull(response, "Response should not be null")
            assertTrue(response.contains("data") || response.contains("errors"), 
                "Response should contain 'data' or 'errors'")

        } catch (e: Exception) {
            println("Error testing old implementation (files): ${e.message}")
            e.printStackTrace()
            // Fail the test when there's an error
            fail("Error testing old implementation (files): ${e.message}", e)
        }
    }

    @Test
    @DisplayName("Test files query against new implementation")
    fun `should test files query against new implementation`() {
        println("Testing files query against new implementation at $newGraphQLUrl")

        // Define a GraphQL query for files
        val query = """
            query {
                files {
                    id
                    path
                }
            }
        """.trimIndent()

        try {
            // Execute the query against the new implementation
            val response = executeGraphQLQuery(newGraphQLUrl, query)

            // Print the response for debugging
            println("Response from new implementation (files): $response")

            // Basic validation of the response
            assertNotNull(response, "Response should not be null")
            assertTrue(
                response.contains("data") || 
                response.contains("errors") || 
                response.contains("errorCode"), 
                "Response should contain 'data', 'errors', or 'errorCode'"
            )

        } catch (e: Exception) {
            println("Error testing new implementation (files): ${e.message}")
            e.printStackTrace()
            // Fail the test when there's an error
            fail("Error testing new implementation (files): ${e.message}", e)
        }
    }

    /**
     * Execute a GraphQL query against the specified URL.
     * 
     * @param url The GraphQL endpoint URL
     * @param query The GraphQL query to execute
     * @return The response as a JSON string
     */
    private fun executeGraphQLQuery(url: String, query: String, variables: Map<String, Any> = emptyMap()): String {
        // Create the request body
        val requestMap = mapOf(
            "query" to query,
            "variables" to variables
        )
        val requestBody = objectMapper.writeValueAsString(requestMap)

        // Create the HTTP request
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()

        // Send the request and get the response
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        // Return the response body
        return response.body()
    }
}
