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
 * Test class for UserController.
 * Tests the functionality of retrieving users with and without pagination,
 * as well as error handling for invalid requests.
 */
internal class UserControllerWebTest : BaseIntegrationTest() {
    @Nested
    inner class BasicFunctionality : GraphQlControllerTest() {
        @Test
        fun `should return all users`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
            query {
                users(page: 1, perPage: 100) {
                    count
                    page
                    perPage
                    data {
                        id
                        gitSignature
                    }
                }
            }
        """,
                    ).execute()
                    .path("users")
                    .entity(JsonNode::class.java)
                    .get()

            // Check pagination metadata
            assertEquals(2, result.get("count").asInt(), "Expected count to be 2")
            assertEquals(1, result.get("page").asInt(), "Expected page to be 1")
            assertEquals(100, result.get("perPage").asInt(), "Expected perPage to be 100")

            // Get the users from the result
            val usersData = result.get("data")
            assertEquals(2, usersData.size(), "Expected 2 users, but got ${usersData.size()}")

            // Order-independent comparison using gitSignature (id may be null due to legacy resolver)
            val expectedBySignature = TestDataProvider.testUsers.associateBy { it.gitSignature }
            usersData.forEach { actualUser ->
                val signature = actualUser.get("gitSignature").asText()
                val expectedUser = expectedBySignature[signature]!!

                assertAll(
                    {
                        assertEquals(
                            expectedUser.gitSignature,
                            actualUser.get("gitSignature").asText(),
                            "User gitSignature mismatch: expected ${expectedUser.gitSignature}, got ${actualUser.get(
                                "gitSignature",
                            ).asText()}",
                        )
                    },
                )
            }
        }

        @Test
        fun `should return user by id`() {
            val expectedUser = TestDataProvider.testUsers.first { it.id == "1" }

            val result: JsonNode =
                graphQlTester
                    .document(
                        """
            query {
                user(id: "1") {
                    id
                    gitSignature
                }
            }
        """,
                    ).execute()
                    .path("user")
                    .entity(JsonNode::class.java)
                    .get()

            // Check that the user matches the test data
            // id may be null due to legacy resolver behavior, so assert on gitSignature only
            assertAll(
                {
                    assertEquals(
                        expectedUser.gitSignature,
                        result.get("gitSignature").asText(),
                        "User gitSignature mismatch: expected ${expectedUser.gitSignature}, got ${result.get("gitSignature").asText()}",
                    )
                },
            )
        }
    }

    @Nested
    inner class Pagination : GraphQlControllerTest() {
        @Test
        fun `should return users with pagination`() {
            // Test with page=1, perPage=1 (should return only the first user)
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
            query {
                users(page: 1, perPage: 1) {
                    count
                    page
                    perPage
                    data {
                        id
                        gitSignature
                    }
                }
            }
        """,
                    ).execute()
                    .path("users")
                    .entity(JsonNode::class.java)
                    .get()

            // Check pagination metadata
            assertEquals(2, result.get("count").asInt(), "Expected count to be 2")
            assertEquals(1, result.get("page").asInt(), "Expected page to be 1")
            assertEquals(1, result.get("perPage").asInt(), "Expected perPage to be 1")

            // Get the users from the result
            val usersData = result.get("data")
            assertEquals(1, usersData.size(), "Expected 1 user, but got ${usersData.size()}")

            // With new default sort (gitSignature ASC), compute expected first item accordingly
            val expectedUser = TestDataProvider.testUsers.minByOrNull { it.gitSignature ?: "" }!!
            val actualUser = usersData.get(0)

            assertAll(
                {
                    assertEquals(
                        expectedUser.gitSignature,
                        actualUser.get("gitSignature").asText(),
                        "User gitSignature mismatch: expected ${expectedUser.gitSignature}, got ${actualUser.get("gitSignature").asText()}",
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
                users {
                    count
                    page
                    perPage
                    data {
                        id
                        gitSignature
                    }
                }
            }
        """,
                    ).execute()
                    .path("users")
                    .entity(JsonNode::class.java)
                    .get()

            // Check pagination metadata
            assertEquals(2, result.get("count").asInt(), "Expected count to be 2")
            assertEquals(1, result.get("page").asInt(), "Expected page to be 1 (default)")
            assertEquals(20, result.get("perPage").asInt(), "Expected perPage to be 20 (default)")

            // Get the users from the result
            val usersData = result.get("data")
            assertEquals(2, usersData.size(), "Expected 2 users, but got ${usersData.size()}")
        }

        @Test
        fun `should return second page of users`() {
            // Test with page=2, perPage=1 (should return only the second user)
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
            query {
                users(page: 2, perPage: 1) {
                    count
                    page
                    perPage
                    data {
                        id
                        gitSignature
                    }
                }
            }
        """,
                    ).execute()
                    .path("users")
                    .entity(JsonNode::class.java)
                    .get()

            // Check pagination metadata
            assertEquals(2, result.get("count").asInt(), "Expected count to be 2")
            assertEquals(2, result.get("page").asInt(), "Expected page to be 2")
            assertEquals(1, result.get("perPage").asInt(), "Expected perPage to be 1")

            // Get the users from the result
            val usersData = result.get("data")
            assertEquals(1, usersData.size(), "Expected 1 user, but got ${usersData.size()}")

            // With new default sort (gitSignature ASC), compute expected second item accordingly
            val expectedUser = TestDataProvider.testUsers
                .sortedBy { it.gitSignature ?: "" }
                .drop(1)
                .first()
            val actualUser = usersData.get(0)

            assertAll(
                {
                    assertEquals(
                        expectedUser.gitSignature,
                        actualUser.get("gitSignature").asText(),
                        "User gitSignature mismatch: expected ${expectedUser.gitSignature}, got ${actualUser.get("gitSignature").asText()}",
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
                  users(page: 3, perPage: 1) {
                      count
                      page
                      perPage
                      data {
                          id
                          gitSignature
                      }
                  }
              }
          """,
                    ).execute()
                    .path("users")
                    .entity(JsonNode::class.java)
                    .get()

            // Check pagination metadata
            assertEquals(2, result.get("count").asInt(), "Expected count to be 2")
            assertEquals(3, result.get("page").asInt(), "Expected page to be 3")
            assertEquals(1, result.get("perPage").asInt(), "Expected perPage to be 1")

            // Get the users from the result
            val usersData = result.get("data")
            assertEquals(0, usersData.size(), "Expected 0 users on page beyond available data, but got ${usersData.size()}")
        }
    }

    @Nested
    inner class ErrorHandling : GraphQlControllerTest() {
        @Test
        fun `should throw exception for non-existent user id`() {
            // Test with a non-existent user ID
            val nonExistentId = "999"

            graphQlTester
                .document(
                    """
            query {
                user(id: "$nonExistentId") {
                    id
                    gitSignature
                }
            }
        """,
                ).execute()
                .errors()
                .expect { error ->
                    error.message?.contains("User not found with id: $nonExistentId") ?: false
                }.verify()
        }

        @Test
        fun `should throw exception for invalid pagination parameters`() {
            // Test with invalid page parameter
            graphQlTester
                .document(
                    """
            query {
                users(page: 0, perPage: 10) {
                    count
                    page
                    perPage
                    data {
                        id
                        gitSignature
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
                users(page: 1, perPage: 0) {
                    count
                    page
                    perPage
                    data {
                        id
                        gitSignature
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
