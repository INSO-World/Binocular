package com.inso_world.binocular.web.graphql.controller

import com.fasterxml.jackson.databind.JsonNode
import com.inso_world.binocular.core.integration.base.BaseIntegrationTest
import com.inso_world.binocular.core.integration.base.TestDataProvider
import com.inso_world.binocular.web.graphql.base.GraphQlControllerTest
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for MilestoneController.
 * Tests the functionality of retrieving milestones with and without pagination,
 * as well as error handling for invalid requests.
 */
internal class MilestoneControllerWebTest : BaseIntegrationTest() {
    @Nested
    inner class BasicFunctionality : GraphQlControllerTest() {
        @Test
        fun `should return all milestones`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
            query {
                milestones(page: 1, perPage: 100) {
                    count
                    page
                    perPage
                    data {
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
            }
        """,
                    ).execute()
                    .path("milestones")
                    .entity(JsonNode::class.java)
                    .get()

            // Check pagination metadata
            assertEquals(2, result.get("count").asInt(), "Expected count to be 2")
            assertEquals(1, result.get("page").asInt(), "Expected page to be 1")
            assertEquals(100, result.get("perPage").asInt(), "Expected perPage to be 100")

            // Get the milestones from the result
            val milestonesData = result.get("data")
            assertEquals(2, milestonesData.size(), "Expected 2 milestones, but got ${milestonesData.size()}")

            // Check that the milestones match the test data
            TestDataProvider.testMilestones.forEachIndexed { index, expectedMilestone ->
                val actualMilestone = milestonesData.get(index)

                assertAll(
                    {
                        assertEquals(
                            expectedMilestone.id,
                            actualMilestone.get("id").asText(),
                            "Milestone ID mismatch: expected ${expectedMilestone.id}, got ${actualMilestone.get("id").asText()}",
                        )
                    },
                    {
                        assertEquals(
                            expectedMilestone.iid,
                            actualMilestone.get("iid").asInt(),
                            "Milestone iid mismatch: expected ${expectedMilestone.iid}, got ${actualMilestone.get("iid").asInt()}",
                        )
                    },
                    {
                        assertEquals(
                            expectedMilestone.title,
                            actualMilestone.get("title").asText(),
                            "Milestone title mismatch: expected ${expectedMilestone.title}, got ${actualMilestone.get("title").asText()}",
                        )
                    },
                    {
                        assertEquals(
                            expectedMilestone.description,
                            actualMilestone.get("description").asText(),
                            "Milestone description mismatch: expected ${expectedMilestone.description}, got ${actualMilestone.get(
                                "description",
                            ).asText()}",
                        )
                    },
                    {
                        assertEquals(
                            expectedMilestone.createdAt,
                            actualMilestone.get("createdAt").asText(),
                            "Milestone createdAt mismatch: expected ${expectedMilestone.createdAt}, got ${actualMilestone.get(
                                "createdAt",
                            ).asText()}",
                        )
                    },
                    {
                        assertEquals(
                            expectedMilestone.updatedAt,
                            actualMilestone.get("updatedAt").asText(),
                            "Milestone updatedAt mismatch: expected ${expectedMilestone.updatedAt}, got ${actualMilestone.get(
                                "updatedAt",
                            ).asText()}",
                        )
                    },
                    {
                        assertEquals(
                            expectedMilestone.startDate,
                            actualMilestone.get("startDate").asText(),
                            "Milestone startDate mismatch: expected ${expectedMilestone.startDate}, got ${actualMilestone.get(
                                "startDate",
                            ).asText()}",
                        )
                    },
                    {
                        assertEquals(
                            expectedMilestone.dueDate,
                            actualMilestone.get("dueDate").asText(),
                            "Milestone dueDate mismatch: expected ${expectedMilestone.dueDate}, got ${actualMilestone.get(
                                "dueDate",
                            ).asText()}",
                        )
                    },
                    {
                        assertEquals(
                            expectedMilestone.state,
                            actualMilestone.get("state").asText(),
                            "Milestone state mismatch: expected ${expectedMilestone.state}, got ${actualMilestone.get("state").asText()}",
                        )
                    },
                    {
                        assertEquals(
                            expectedMilestone.expired,
                            actualMilestone.get("expired").asBoolean(),
                            "Milestone expired mismatch: expected ${expectedMilestone.expired}, got ${actualMilestone.get(
                                "expired",
                            ).asBoolean()}",
                        )
                    },
                    {
                        assertEquals(
                            expectedMilestone.webUrl,
                            actualMilestone.get("webUrl").asText(),
                            "Milestone webUrl mismatch: expected ${expectedMilestone.webUrl}, got ${actualMilestone.get(
                                "webUrl",
                            ).asText()}",
                        )
                    },
                )
            }
        }

        @Test
        fun `should return milestone by id`() {
            val expectedMilestone = TestDataProvider.testMilestones.first { it.id == "1" }

            val result: JsonNode =
                graphQlTester
                    .document(
                        """
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
        """,
                    ).execute()
                    .path("milestone")
                    .entity(JsonNode::class.java)
                    .get()

            // Check that the milestone matches the test data
            assertAll(
                {
                    assertEquals(
                        expectedMilestone.id,
                        result.get("id").asText(),
                        "Milestone ID mismatch: expected ${expectedMilestone.id}, got ${result.get("id").asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.iid,
                        result.get("iid").asInt(),
                        "Milestone iid mismatch: expected ${expectedMilestone.iid}, got ${result.get("iid").asInt()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.title,
                        result.get("title").asText(),
                        "Milestone title mismatch: expected ${expectedMilestone.title}, got ${result.get("title").asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.description,
                        result.get("description").asText(),
                        "Milestone description mismatch: expected ${expectedMilestone.description}, got ${result.get(
                            "description",
                        ).asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.createdAt,
                        result.get("createdAt").asText(),
                        "Milestone createdAt mismatch: expected ${expectedMilestone.createdAt}, got ${result.get("createdAt").asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.updatedAt,
                        result.get("updatedAt").asText(),
                        "Milestone updatedAt mismatch: expected ${expectedMilestone.updatedAt}, got ${result.get("updatedAt").asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.startDate,
                        result.get("startDate").asText(),
                        "Milestone startDate mismatch: expected ${expectedMilestone.startDate}, got ${result.get("startDate").asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.dueDate,
                        result.get("dueDate").asText(),
                        "Milestone dueDate mismatch: expected ${expectedMilestone.dueDate}, got ${result.get("dueDate").asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.state,
                        result.get("state").asText(),
                        "Milestone state mismatch: expected ${expectedMilestone.state}, got ${result.get("state").asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.expired,
                        result.get("expired").asBoolean(),
                        "Milestone expired mismatch: expected ${expectedMilestone.expired}, got ${result.get("expired").asBoolean()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.webUrl,
                        result.get("webUrl").asText(),
                        "Milestone webUrl mismatch: expected ${expectedMilestone.webUrl}, got ${result.get("webUrl").asText()}",
                    )
                },
            )
        }
    }

    @Nested
    inner class Pagination : GraphQlControllerTest() {
        @Test
        fun `should return milestones with pagination`() {
            // Test with page=1, perPage=1 (should return only the first milestone)
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
            query {
                milestones(page: 1, perPage: 1) {
                    count
                    page
                    perPage
                    data {
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
            }
        """,
                    ).execute()
                    .path("milestones")
                    .entity(JsonNode::class.java)
                    .get()

            // Check pagination metadata
            assertEquals(2, result.get("count").asInt(), "Expected count to be 2")
            assertEquals(1, result.get("page").asInt(), "Expected page to be 1")
            assertEquals(1, result.get("perPage").asInt(), "Expected perPage to be 1")

            // Get the milestones from the result
            val milestonesData = result.get("data")
            assertEquals(1, milestonesData.size(), "Expected 1 milestone, but got ${milestonesData.size()}")

            // Check that the milestone matches the first test milestone
            val expectedMilestone = TestDataProvider.testMilestones.first()
            val actualMilestone = milestonesData.get(0)

            assertAll(
                {
                    assertEquals(
                        expectedMilestone.id,
                        actualMilestone.get("id").asText(),
                        "Milestone ID mismatch: expected ${expectedMilestone.id}, got ${actualMilestone.get("id").asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.iid,
                        actualMilestone.get("iid").asInt(),
                        "Milestone iid mismatch: expected ${expectedMilestone.iid}, got ${actualMilestone.get("iid").asInt()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.title,
                        actualMilestone.get("title").asText(),
                        "Milestone title mismatch: expected ${expectedMilestone.title}, got ${actualMilestone.get("title").asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.description,
                        actualMilestone.get("description").asText(),
                        "Milestone description mismatch: expected ${expectedMilestone.description}, got ${actualMilestone.get(
                            "description",
                        ).asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.createdAt,
                        actualMilestone.get("createdAt").asText(),
                        "Milestone createdAt mismatch: expected ${expectedMilestone.createdAt}, got ${actualMilestone.get(
                            "createdAt",
                        ).asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.updatedAt,
                        actualMilestone.get("updatedAt").asText(),
                        "Milestone updatedAt mismatch: expected ${expectedMilestone.updatedAt}, got ${actualMilestone.get(
                            "updatedAt",
                        ).asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.startDate,
                        actualMilestone.get("startDate").asText(),
                        "Milestone startDate mismatch: expected ${expectedMilestone.startDate}, got ${actualMilestone.get(
                            "startDate",
                        ).asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.dueDate,
                        actualMilestone.get("dueDate").asText(),
                        "Milestone dueDate mismatch: expected ${expectedMilestone.dueDate}, got ${actualMilestone.get("dueDate").asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.state,
                        actualMilestone.get("state").asText(),
                        "Milestone state mismatch: expected ${expectedMilestone.state}, got ${actualMilestone.get("state").asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.expired,
                        actualMilestone.get("expired").asBoolean(),
                        "Milestone expired mismatch: expected ${expectedMilestone.expired}, got ${actualMilestone.get(
                            "expired",
                        ).asBoolean()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.webUrl,
                        actualMilestone.get("webUrl").asText(),
                        "Milestone webUrl mismatch: expected ${expectedMilestone.webUrl}, got ${actualMilestone.get("webUrl").asText()}",
                    )
                },
            )
        }

        @Test
        fun `should handle null pagination parameters`() {
            // Test with null page and perPage parameters (should use defaults)
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
            query {
                milestones {
                    count
                    page
                    perPage
                    data {
                        id
                        title
                    }
                }
            }
        """,
                    ).execute()
                    .path("milestones")
                    .entity(JsonNode::class.java)
                    .get()

            // Check pagination metadata
            assertEquals(2, result.get("count").asInt(), "Expected count to be 2")
            assertEquals(1, result.get("page").asInt(), "Expected page to be 1 (default)")
            assertEquals(20, result.get("perPage").asInt(), "Expected perPage to be 20 (default)")

            // Get the milestones from the result
            val milestonesData = result.get("data")
            assertEquals(2, milestonesData.size(), "Expected 2 milestones, but got ${milestonesData.size()}")
        }

        @Test
        fun `should return second page of milestones`() {
            // Test with page=2, perPage=1 (should return only the second milestone)
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
            query {
                milestones(page: 2, perPage: 1) {
                    count
                    page
                    perPage
                    data {
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
            }
        """,
                    ).execute()
                    .path("milestones")
                    .entity(JsonNode::class.java)
                    .get()

            // Check pagination metadata
            assertEquals(2, result.get("count").asInt(), "Expected count to be 2")
            assertEquals(2, result.get("page").asInt(), "Expected page to be 2")
            assertEquals(1, result.get("perPage").asInt(), "Expected perPage to be 1")

            // Get the milestones from the result
            val milestonesData = result.get("data")
            assertEquals(1, milestonesData.size(), "Expected 1 milestone, but got ${milestonesData.size()}")

            // Check that the milestone matches the second test milestone
            val expectedMilestone = TestDataProvider.testMilestones[1]
            val actualMilestone = milestonesData.get(0)

            assertAll(
                {
                    assertEquals(
                        expectedMilestone.id,
                        actualMilestone.get("id").asText(),
                        "Milestone ID mismatch: expected ${expectedMilestone.id}, got ${actualMilestone.get("id").asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.iid,
                        actualMilestone.get("iid").asInt(),
                        "Milestone iid mismatch: expected ${expectedMilestone.iid}, got ${actualMilestone.get("iid").asInt()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.title,
                        actualMilestone.get("title").asText(),
                        "Milestone title mismatch: expected ${expectedMilestone.title}, got ${actualMilestone.get("title").asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.description,
                        actualMilestone.get("description").asText(),
                        "Milestone description mismatch: expected ${expectedMilestone.description}, got ${actualMilestone.get(
                            "description",
                        ).asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.createdAt,
                        actualMilestone.get("createdAt").asText(),
                        "Milestone createdAt mismatch: expected ${expectedMilestone.createdAt}, got ${actualMilestone.get(
                            "createdAt",
                        ).asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.updatedAt,
                        actualMilestone.get("updatedAt").asText(),
                        "Milestone updatedAt mismatch: expected ${expectedMilestone.updatedAt}, got ${actualMilestone.get(
                            "updatedAt",
                        ).asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.startDate,
                        actualMilestone.get("startDate").asText(),
                        "Milestone startDate mismatch: expected ${expectedMilestone.startDate}, got ${actualMilestone.get(
                            "startDate",
                        ).asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.dueDate,
                        actualMilestone.get("dueDate").asText(),
                        "Milestone dueDate mismatch: expected ${expectedMilestone.dueDate}, got ${actualMilestone.get("dueDate").asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.state,
                        actualMilestone.get("state").asText(),
                        "Milestone state mismatch: expected ${expectedMilestone.state}, got ${actualMilestone.get("state").asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.expired,
                        actualMilestone.get("expired").asBoolean(),
                        "Milestone expired mismatch: expected ${expectedMilestone.expired}, got ${actualMilestone.get(
                            "expired",
                        ).asBoolean()}",
                    )
                },
                {
                    assertEquals(
                        expectedMilestone.webUrl,
                        actualMilestone.get("webUrl").asText(),
                        "Milestone webUrl mismatch: expected ${expectedMilestone.webUrl}, got ${actualMilestone.get("webUrl").asText()}",
                    )
                },
            )
        }

        @Test
        fun `should return empty list for page beyond available data`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
              query {
                  milestones(page: 3, perPage: 1) {
                      count
                      page
                      perPage
                      data {
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
              }
          """,
                    ).execute()
                    .path("milestones")
                    .entity(JsonNode::class.java)
                    .get()

            // Check pagination metadata
            assertEquals(2, result.get("count").asInt(), "Expected count to be 2")
            assertEquals(3, result.get("page").asInt(), "Expected page to be 3")
            assertEquals(1, result.get("perPage").asInt(), "Expected perPage to be 1")

            // Get the milestones from the result
            val milestonesData = result.get("data")
            assertEquals(0, milestonesData.size(), "Expected 0 milestones on page beyond available data, but got ${milestonesData.size()}")
        }
    }

    @Nested
    inner class ErrorHandling : GraphQlControllerTest() {
        @Test
        fun `should throw exception for non-existent milestone id`() {
            // Test with a non-existent milestone ID
            val nonExistentId = "999"

            graphQlTester
                .document(
                    """
            query {
                milestone(id: "$nonExistentId") {
                    id
                    title
                }
            }
        """,
                ).execute()
                .errors()
                .expect { error ->
                    error.message?.contains("Milestone not found with id: $nonExistentId") ?: false
                }.verify()
        }

        @Test
        fun `should throw exception for invalid pagination parameters`() {
            // Test with invalid page parameter
            graphQlTester
                .document(
                    """
            query {
                milestones(page: 0, perPage: 10) {
                    count
                    page
                    perPage
                    data {
                        id
                        title
                    }
                }
            }
        """,
                ).execute()
                .errors()
                .expect { error ->
                    error.message?.contains("Page must be greater than or equal to 1") ?: false
                }.verify()

            // Test with invalid perPage parameter
            graphQlTester
                .document(
                    """
            query {
                milestones(page: 1, perPage: 0) {
                    count
                    page
                    perPage
                    data {
                        id
                        title
                    }
                }
            }
        """,
                ).execute()
                .errors()
                .expect { error ->
                    error.message?.contains("PerPage must be greater than or equal to 1") ?: false
                }.verify()
        }
    }
}
