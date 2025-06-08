package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.web.BaseDbTest
import com.inso_world.binocular.web.entity.Milestone
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for MilestoneController.
 * Tests the functionality of retrieving milestones with and without pagination,
 * as well as error handling for invalid requests.
 */
class MilestoneControllerWebTest : BaseDbTest() {

  @Nested
  inner class BasicFunctionality {
    @Test
    fun `should return all milestones`() {
        // Test data is set up in setupTestData
        val result = graphQlTester.document("""
            query {
                milestones(page: 1, perPage: 100) {
                    id
                    iid
                    title
                    description
                    createdAt
                    updatedAt
                    startDate
                    dueDate
                    state
                    expired
                    webUrl
                }
            }
        """)
        .execute()
        .path("milestones")
        .entityList(Milestone::class.java)

        // Check size
        result.hasSize(2)

        // Get the milestones from the result
        val milestones = result.get()

        // Check that the milestones match the test data
        testMilestones.forEachIndexed { index, expectedMilestone ->
            val actualMilestone = milestones[index]

            assertAll(
                { assert(actualMilestone.id == expectedMilestone.id) { "Milestone ID mismatch: expected ${expectedMilestone.id}, got ${actualMilestone.id}" } },
                { assert(actualMilestone.iid == expectedMilestone.iid) { "Milestone iid mismatch: expected ${expectedMilestone.iid}, got ${actualMilestone.iid}" } },
                { assert(actualMilestone.title == expectedMilestone.title) { "Milestone title mismatch: expected ${expectedMilestone.title}, got ${actualMilestone.title}" } },
                { assert(actualMilestone.description == expectedMilestone.description) { "Milestone description mismatch: expected ${expectedMilestone.description}, got ${actualMilestone.description}" } },
                { assert(actualMilestone.createdAt == expectedMilestone.createdAt) { "Milestone createdAt mismatch: expected ${expectedMilestone.createdAt}, got ${actualMilestone.createdAt}" } },
                { assert(actualMilestone.updatedAt == expectedMilestone.updatedAt) { "Milestone updatedAt mismatch: expected ${expectedMilestone.updatedAt}, got ${actualMilestone.updatedAt}" } },
                { assert(actualMilestone.startDate == expectedMilestone.startDate) { "Milestone startDate mismatch: expected ${expectedMilestone.startDate}, got ${actualMilestone.startDate}" } },
                { assert(actualMilestone.dueDate == expectedMilestone.dueDate) { "Milestone dueDate mismatch: expected ${expectedMilestone.dueDate}, got ${actualMilestone.dueDate}" } },
                { assert(actualMilestone.state == expectedMilestone.state) { "Milestone state mismatch: expected ${expectedMilestone.state}, got ${actualMilestone.state}" } },
                { assert(actualMilestone.expired == expectedMilestone.expired) { "Milestone expired mismatch: expected ${expectedMilestone.expired}, got ${actualMilestone.expired}" } },
                { assert(actualMilestone.webUrl == expectedMilestone.webUrl) { "Milestone webUrl mismatch: expected ${expectedMilestone.webUrl}, got ${actualMilestone.webUrl}" } }
            )
        }
    }

    @Test
    fun `should return milestone by id`() {
        // Test data is set up in setupTestData
        val expectedMilestone = testMilestones.first { it.id == "1" }

        val result = graphQlTester.document("""
            query {
                milestone(id: "1") {
                    id
                    iid
                    title
                    description
                    createdAt
                    updatedAt
                    startDate
                    dueDate
                    state
                    expired
                    webUrl
                }
            }
        """)
        .execute()
        .path("milestone")

        // Check that the milestone exists
        result.hasValue()

        // Get the milestone from the result
        val actualMilestone = result.entity(Milestone::class.java).get()

        // Check that the milestone matches the test data
        assertAll(
            { assert(actualMilestone.id == expectedMilestone.id) { "Milestone ID mismatch: expected ${expectedMilestone.id}, got ${actualMilestone.id}" } },
            { assert(actualMilestone.iid == expectedMilestone.iid) { "Milestone iid mismatch: expected ${expectedMilestone.iid}, got ${actualMilestone.iid}" } },
            { assert(actualMilestone.title == expectedMilestone.title) { "Milestone title mismatch: expected ${expectedMilestone.title}, got ${actualMilestone.title}" } },
            { assert(actualMilestone.description == expectedMilestone.description) { "Milestone description mismatch: expected ${expectedMilestone.description}, got ${actualMilestone.description}" } },
            { assert(actualMilestone.createdAt == expectedMilestone.createdAt) { "Milestone createdAt mismatch: expected ${expectedMilestone.createdAt}, got ${actualMilestone.createdAt}" } },
            { assert(actualMilestone.updatedAt == expectedMilestone.updatedAt) { "Milestone updatedAt mismatch: expected ${expectedMilestone.updatedAt}, got ${actualMilestone.updatedAt}" } },
            { assert(actualMilestone.startDate == expectedMilestone.startDate) { "Milestone startDate mismatch: expected ${expectedMilestone.startDate}, got ${actualMilestone.startDate}" } },
            { assert(actualMilestone.dueDate == expectedMilestone.dueDate) { "Milestone dueDate mismatch: expected ${expectedMilestone.dueDate}, got ${actualMilestone.dueDate}" } },
            { assert(actualMilestone.state == expectedMilestone.state) { "Milestone state mismatch: expected ${expectedMilestone.state}, got ${actualMilestone.state}" } },
            { assert(actualMilestone.expired == expectedMilestone.expired) { "Milestone expired mismatch: expected ${expectedMilestone.expired}, got ${actualMilestone.expired}" } },
            { assert(actualMilestone.webUrl == expectedMilestone.webUrl) { "Milestone webUrl mismatch: expected ${expectedMilestone.webUrl}, got ${actualMilestone.webUrl}" } }
        )
    }
  }

  @Nested
  inner class Pagination {
    @Test
    fun `should return milestones with pagination`() {
        // Test with page=1, perPage=1 (should return only the first milestone)
        val result = graphQlTester.document("""
            query {
                milestones(page: 1, perPage: 1) {
                    id
                    iid
                    title
                    description
                    createdAt
                    updatedAt
                    startDate
                    dueDate
                    state
                    expired
                    webUrl
                }
            }
        """)
        .execute()
        .path("milestones")
        .entityList(Milestone::class.java)

        // Check size
        result.hasSize(1)

        // Get the milestones from the result
        val milestones = result.get()

        // Check that the milestone matches the first test milestone
        val expectedMilestone = testMilestones.first()
        val actualMilestone = milestones.first()

        assertAll(
            { assert(actualMilestone.id == expectedMilestone.id) { "Milestone ID mismatch: expected ${expectedMilestone.id}, got ${actualMilestone.id}" } },
            { assert(actualMilestone.iid == expectedMilestone.iid) { "Milestone iid mismatch: expected ${expectedMilestone.iid}, got ${actualMilestone.iid}" } },
            { assert(actualMilestone.title == expectedMilestone.title) { "Milestone title mismatch: expected ${expectedMilestone.title}, got ${actualMilestone.title}" } },
            { assert(actualMilestone.description == expectedMilestone.description) { "Milestone description mismatch: expected ${expectedMilestone.description}, got ${actualMilestone.description}" } },
            { assert(actualMilestone.createdAt == expectedMilestone.createdAt) { "Milestone createdAt mismatch: expected ${expectedMilestone.createdAt}, got ${actualMilestone.createdAt}" } },
            { assert(actualMilestone.updatedAt == expectedMilestone.updatedAt) { "Milestone updatedAt mismatch: expected ${expectedMilestone.updatedAt}, got ${actualMilestone.updatedAt}" } },
            { assert(actualMilestone.startDate == expectedMilestone.startDate) { "Milestone startDate mismatch: expected ${expectedMilestone.startDate}, got ${actualMilestone.startDate}" } },
            { assert(actualMilestone.dueDate == expectedMilestone.dueDate) { "Milestone dueDate mismatch: expected ${expectedMilestone.dueDate}, got ${actualMilestone.dueDate}" } },
            { assert(actualMilestone.state == expectedMilestone.state) { "Milestone state mismatch: expected ${expectedMilestone.state}, got ${actualMilestone.state}" } },
            { assert(actualMilestone.expired == expectedMilestone.expired) { "Milestone expired mismatch: expected ${expectedMilestone.expired}, got ${actualMilestone.expired}" } },
            { assert(actualMilestone.webUrl == expectedMilestone.webUrl) { "Milestone webUrl mismatch: expected ${expectedMilestone.webUrl}, got ${actualMilestone.webUrl}" } }
        )
    }

    @Test
    fun `should handle null pagination parameters`() {
        // Test with null page and perPage parameters (should use defaults)
        val result = graphQlTester.document("""
            query {
                milestones {
                    id
                    title
                }
            }
        """)
        .execute()
        .path("milestones")
        .entityList(Milestone::class.java)

        // Check size (should return all milestones with default pagination)
        result.hasSize(2)
    }

    @Test
    fun `should return second page of milestones`() {
        // Test with page=2, perPage=1 (should return only the second milestone)
        val result = graphQlTester.document("""
            query {
                milestones(page: 2, perPage: 1) {
                    id
                    iid
                    title
                    description
                    createdAt
                    updatedAt
                    startDate
                    dueDate
                    state
                    expired
                    webUrl
                }
            }
        """)
        .execute()
        .path("milestones")
        .entityList(Milestone::class.java)

        // Check size
        result.hasSize(1)

        // Get the milestones from the result
        val milestones = result.get()

        // Check that the milestone matches the second test milestone
        val expectedMilestone = testMilestones[1]
        val actualMilestone = milestones.first()

        assertAll(
            { assert(actualMilestone.id == expectedMilestone.id) { "Milestone ID mismatch: expected ${expectedMilestone.id}, got ${actualMilestone.id}" } },
            { assert(actualMilestone.iid == expectedMilestone.iid) { "Milestone iid mismatch: expected ${expectedMilestone.iid}, got ${actualMilestone.iid}" } },
            { assert(actualMilestone.title == expectedMilestone.title) { "Milestone title mismatch: expected ${expectedMilestone.title}, got ${actualMilestone.title}" } },
            { assert(actualMilestone.description == expectedMilestone.description) { "Milestone description mismatch: expected ${expectedMilestone.description}, got ${actualMilestone.description}" } },
            { assert(actualMilestone.createdAt == expectedMilestone.createdAt) { "Milestone createdAt mismatch: expected ${expectedMilestone.createdAt}, got ${actualMilestone.createdAt}" } },
            { assert(actualMilestone.updatedAt == expectedMilestone.updatedAt) { "Milestone updatedAt mismatch: expected ${expectedMilestone.updatedAt}, got ${actualMilestone.updatedAt}" } },
            { assert(actualMilestone.startDate == expectedMilestone.startDate) { "Milestone startDate mismatch: expected ${expectedMilestone.startDate}, got ${actualMilestone.startDate}" } },
            { assert(actualMilestone.dueDate == expectedMilestone.dueDate) { "Milestone dueDate mismatch: expected ${expectedMilestone.dueDate}, got ${actualMilestone.dueDate}" } },
            { assert(actualMilestone.state == expectedMilestone.state) { "Milestone state mismatch: expected ${expectedMilestone.state}, got ${actualMilestone.state}" } },
            { assert(actualMilestone.expired == expectedMilestone.expired) { "Milestone expired mismatch: expected ${expectedMilestone.expired}, got ${actualMilestone.expired}" } },
            { assert(actualMilestone.webUrl == expectedMilestone.webUrl) { "Milestone webUrl mismatch: expected ${expectedMilestone.webUrl}, got ${actualMilestone.webUrl}" } }
        )
    }
  }

  @Nested
  inner class ErrorHandling {
    @Test
    fun `should throw exception for non-existent milestone id`() {
        // Test with a non-existent milestone ID
        val nonExistentId = "999"

        graphQlTester.document("""
            query {
                milestone(id: "$nonExistentId") {
                    id
                    title
                }
            }
        """)
        .execute()
        .errors()
        .expect { error ->
            error.message?.contains("Milestone not found with id: $nonExistentId") ?: false
        }
        .verify()
    }

    @Test
    fun `should throw exception for invalid pagination parameters`() {
        // Test with invalid page parameter
        graphQlTester.document("""
            query {
                milestones(page: 0, perPage: 10) {
                    id
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
                milestones(page: 1, perPage: 0) {
                    id
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
