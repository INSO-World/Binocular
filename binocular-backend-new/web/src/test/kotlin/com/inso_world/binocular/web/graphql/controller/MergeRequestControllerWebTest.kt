package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.web.BaseDbTest
import com.inso_world.binocular.web.entity.MergeRequest
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for MergeRequestController.
 * Tests the functionality of retrieving merge requests with and without pagination,
 * as well as error handling for invalid requests.
 */
class MergeRequestControllerWebTest : BaseDbTest() {

  @Nested
  inner class BasicFunctionality {
    @Test
    fun `should return all merge requests`() {
        // Test data is set up in BaseDbTest
        val result = graphQlTester.document("""
            query {
                mergeRequests(page: 1, perPage: 100) {
                    id
                    iid
                    title
                    description
                    state
                    webUrl
                    labels
                }
            }
        """)
        .execute()
        .path("mergeRequests")
        .entityList(MergeRequest::class.java)

        // Check size
        result.hasSize(2)

        // Get the merge requests from the result
        val mergeRequests = result.get()

        // Check that the merge requests match the test data
        testMergeRequests.forEachIndexed { index, expectedMergeRequest ->
            val actualMergeRequest = mergeRequests[index]

            assertAll(
                { assert(actualMergeRequest.id == expectedMergeRequest.id) { "MergeRequest ID mismatch: expected ${expectedMergeRequest.id}, got ${actualMergeRequest.id}" } },
                { assert(actualMergeRequest.iid == expectedMergeRequest.iid) { "MergeRequest IID mismatch: expected ${expectedMergeRequest.iid}, got ${actualMergeRequest.iid}" } },
                { assert(actualMergeRequest.title == expectedMergeRequest.title) { "MergeRequest title mismatch: expected ${expectedMergeRequest.title}, got ${actualMergeRequest.title}" } },
                { assert(actualMergeRequest.description == expectedMergeRequest.description) { "MergeRequest description mismatch: expected ${expectedMergeRequest.description}, got ${actualMergeRequest.description}" } },
                { assert(actualMergeRequest.state == expectedMergeRequest.state) { "MergeRequest state mismatch: expected ${expectedMergeRequest.state}, got ${actualMergeRequest.state}" } },
                { assert(actualMergeRequest.webUrl == expectedMergeRequest.webUrl) { "MergeRequest webUrl mismatch: expected ${expectedMergeRequest.webUrl}, got ${actualMergeRequest.webUrl}" } },
                { assert(actualMergeRequest.labels == expectedMergeRequest.labels) { "MergeRequest labels mismatch: expected ${expectedMergeRequest.labels}, got ${actualMergeRequest.labels}" } }
            )
        }
    }

    @Test
    fun `should return merge request by id`() {
        // Test data is set up in BaseDbTest
        val expectedMergeRequest = testMergeRequests.first { it.id == "1" }

        val result = graphQlTester.document("""
            query {
                mergeRequest(id: "1") {
                    id
                    iid
                    title
                    description
                    state
                    webUrl
                    labels
                }
            }
        """)
        .execute()
        .path("mergeRequest")

        // Check that the merge request exists
        result.hasValue()

        // Get the merge request from the result
        val actualMergeRequest = result.entity(MergeRequest::class.java).get()

        // Check that the merge request matches the test data
        assertAll(
            { assert(actualMergeRequest.id == expectedMergeRequest.id) { "MergeRequest ID mismatch: expected ${expectedMergeRequest.id}, got ${actualMergeRequest.id}" } },
            { assert(actualMergeRequest.iid == expectedMergeRequest.iid) { "MergeRequest IID mismatch: expected ${expectedMergeRequest.iid}, got ${actualMergeRequest.iid}" } },
            { assert(actualMergeRequest.title == expectedMergeRequest.title) { "MergeRequest title mismatch: expected ${expectedMergeRequest.title}, got ${actualMergeRequest.title}" } },
            { assert(actualMergeRequest.description == expectedMergeRequest.description) { "MergeRequest description mismatch: expected ${expectedMergeRequest.description}, got ${actualMergeRequest.description}" } },
            { assert(actualMergeRequest.state == expectedMergeRequest.state) { "MergeRequest state mismatch: expected ${expectedMergeRequest.state}, got ${actualMergeRequest.state}" } },
            { assert(actualMergeRequest.webUrl == expectedMergeRequest.webUrl) { "MergeRequest webUrl mismatch: expected ${expectedMergeRequest.webUrl}, got ${actualMergeRequest.webUrl}" } },
            { assert(actualMergeRequest.labels == expectedMergeRequest.labels) { "MergeRequest labels mismatch: expected ${expectedMergeRequest.labels}, got ${actualMergeRequest.labels}" } }
        )
    }
  }

  @Nested
  inner class Pagination {
    @Test
    fun `should return merge requests with pagination`() {
        // Test with page=1, perPage=1 (should return only the first merge request)
        val result = graphQlTester.document("""
            query {
                mergeRequests(page: 1, perPage: 1) {
                    id
                    iid
                    title
                    description
                    state
                    webUrl
                    labels
                }
            }
        """)
        .execute()
        .path("mergeRequests")
        .entityList(MergeRequest::class.java)

        // Check size
        result.hasSize(1)

        // Get the merge requests from the result
        val mergeRequests = result.get()

        // Check that the merge request matches the first test merge request
        val expectedMergeRequest = testMergeRequests.first()
        val actualMergeRequest = mergeRequests.first()

        assertAll(
            { assert(actualMergeRequest.id == expectedMergeRequest.id) { "MergeRequest ID mismatch: expected ${expectedMergeRequest.id}, got ${actualMergeRequest.id}" } },
            { assert(actualMergeRequest.iid == expectedMergeRequest.iid) { "MergeRequest IID mismatch: expected ${expectedMergeRequest.iid}, got ${actualMergeRequest.iid}" } },
            { assert(actualMergeRequest.title == expectedMergeRequest.title) { "MergeRequest title mismatch: expected ${expectedMergeRequest.title}, got ${actualMergeRequest.title}" } },
            { assert(actualMergeRequest.description == expectedMergeRequest.description) { "MergeRequest description mismatch: expected ${expectedMergeRequest.description}, got ${actualMergeRequest.description}" } },
            { assert(actualMergeRequest.state == expectedMergeRequest.state) { "MergeRequest state mismatch: expected ${expectedMergeRequest.state}, got ${actualMergeRequest.state}" } },
            { assert(actualMergeRequest.webUrl == expectedMergeRequest.webUrl) { "MergeRequest webUrl mismatch: expected ${expectedMergeRequest.webUrl}, got ${actualMergeRequest.webUrl}" } },
            { assert(actualMergeRequest.labels == expectedMergeRequest.labels) { "MergeRequest labels mismatch: expected ${expectedMergeRequest.labels}, got ${actualMergeRequest.labels}" } }
        )
    }

    @Test
    fun `should handle null pagination parameters`() {
        // Test with null page and perPage parameters (should use defaults)
        val result = graphQlTester.document("""
            query {
                mergeRequests {
                    id
                    iid
                    title
                }
            }
        """)
        .execute()
        .path("mergeRequests")
        .entityList(MergeRequest::class.java)

        // Check size (should return all merge requests with default pagination)
        result.hasSize(2)
    }

    @Test
    fun `should return second page of merge requests`() {
        // Test with page=2, perPage=1 (should return only the second merge request)
        val result = graphQlTester.document("""
            query {
                mergeRequests(page: 2, perPage: 1) {
                    id
                    iid
                    title
                    description
                    state
                    webUrl
                    labels
                }
            }
        """)
        .execute()
        .path("mergeRequests")
        .entityList(MergeRequest::class.java)

        // Check size
        result.hasSize(1)

        // Get the merge requests from the result
        val mergeRequests = result.get()

        // Check that the merge request matches the second test merge request
        val expectedMergeRequest = testMergeRequests[1]
        val actualMergeRequest = mergeRequests.first()

        assertAll(
            { assert(actualMergeRequest.id == expectedMergeRequest.id) { "MergeRequest ID mismatch: expected ${expectedMergeRequest.id}, got ${actualMergeRequest.id}" } },
            { assert(actualMergeRequest.iid == expectedMergeRequest.iid) { "MergeRequest IID mismatch: expected ${expectedMergeRequest.iid}, got ${actualMergeRequest.iid}" } },
            { assert(actualMergeRequest.title == expectedMergeRequest.title) { "MergeRequest title mismatch: expected ${expectedMergeRequest.title}, got ${actualMergeRequest.title}" } },
            { assert(actualMergeRequest.description == expectedMergeRequest.description) { "MergeRequest description mismatch: expected ${expectedMergeRequest.description}, got ${actualMergeRequest.description}" } },
            { assert(actualMergeRequest.state == expectedMergeRequest.state) { "MergeRequest state mismatch: expected ${expectedMergeRequest.state}, got ${actualMergeRequest.state}" } },
            { assert(actualMergeRequest.webUrl == expectedMergeRequest.webUrl) { "MergeRequest webUrl mismatch: expected ${expectedMergeRequest.webUrl}, got ${actualMergeRequest.webUrl}" } },
            { assert(actualMergeRequest.labels == expectedMergeRequest.labels) { "MergeRequest labels mismatch: expected ${expectedMergeRequest.labels}, got ${actualMergeRequest.labels}" } }
        )
    }
  }

  @Nested
  inner class ErrorHandling {
    @Test
    fun `should throw exception for non-existent merge request id`() {
        // Test with a non-existent merge request ID
        val nonExistentId = "999"

        graphQlTester.document("""
            query {
                mergeRequest(id: "$nonExistentId") {
                    id
                    iid
                    title
                }
            }
        """)
        .execute()
        .errors()
        .expect { error ->
            error.message?.contains("MergeRequest not found with id: $nonExistentId") ?: false
        }
        .verify()
    }

    @Test
    fun `should throw exception for invalid pagination parameters`() {
        // Test with invalid page parameter
        graphQlTester.document("""
            query {
                mergeRequests(page: 0, perPage: 10) {
                    id
                    iid
                    title
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
                mergeRequests(page: 1, perPage: 0) {
                    id
                    iid
                    title
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
  }
}
