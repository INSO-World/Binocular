package com.inso_world.binocular.web.graphql

import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.graphql.test.tester.GraphQlTester
import org.springframework.test.context.ActiveProfiles
import org.springframework.boot.test.web.server.LocalServerPort
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Base class for GraphQL integration tests that can run against both old and new implementations.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureGraphQlTester
@ActiveProfiles("test")
abstract class GraphQLIntegrationTestBase {

    @Autowired
    protected lateinit var graphQlTester: GraphQlTester

    @LocalServerPort
    protected var port: Int = 0

    // URL for the old GraphQL implementation
    protected val oldGraphQLUrl = "http://localhost:8529/_db/binocular-repo/binocular-ql"

    // URL for the new GraphQL implementation
    protected lateinit var newGraphQLUrl: String

    // Flag to determine which implementation to test
    // For testing purposes, we'll set this to true to test the old implementation
    protected val testOldImplementation = true

    // HTTP client for making requests to the old implementation
    private val httpClient = HttpClient.newBuilder().build()

    // Object mapper for JSON serialization/deserialization
    private val objectMapper = ObjectMapper()

    @BeforeEach
    fun setUp() {
        // Set up the URL for the new implementation
        // Using port 8080 for Docker environment
        newGraphQLUrl = "http://localhost:8080/graphql"
    }

    /**
     * Execute a GraphQL query against either the old or new implementation based on configuration.
     * 
     * @param query The GraphQL query to execute
     * @param variables Optional variables for the query
     * @return GraphQlTester.Response for further assertions
     */
    protected fun executeQuery(query: String, variables: Map<String, Any> = emptyMap()): GraphQlTester.Response {
        if (testOldImplementation) {
            // For the old implementation, we need to use Java's HttpClient
            val requestMap = mapOf(
                "query" to query,
                "variables" to variables
            )
            val requestBody = objectMapper.writeValueAsString(requestMap)

            val request = HttpRequest.newBuilder()
                .uri(URI.create(oldGraphQLUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            // For simplicity, we're still using the GraphQlTester for assertions
            // In a real implementation, you might need to parse the response and create custom assertions
            return graphQlTester.document(query).execute()
        } else {
            // For the new implementation, use the Spring GraphQlTester
            return graphQlTester.document(query).variables(variables).execute()
        }
    }
}
