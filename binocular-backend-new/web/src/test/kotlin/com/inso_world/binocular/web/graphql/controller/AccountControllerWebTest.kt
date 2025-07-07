package com.inso_world.binocular.web.graphql.controller

import com.fasterxml.jackson.databind.JsonNode
import com.inso_world.binocular.core.integration.base.BaseIntegrationTest
import com.inso_world.binocular.web.BaseDbTest
import com.inso_world.binocular.web.TestDataProvider
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for AccountController.
 * Tests the functionality of retrieving accounts with and without pagination,
 * as well as error handling for invalid requests.
 */
internal class AccountControllerWebTest : BaseIntegrationTest() {
    @Nested
    internal inner class BasicFunctionality : BaseDbTest() {
        @Test
        fun `should return all accounts`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
            query {
                accounts(page: 1, perPage: 100) {
                    count
                    page
                    perPage
                    data {
                        id
                        platform
                        login
                        name
                        avatarUrl
                        url
                    }
                }
            }
        """,
                    ).execute()
                    .path("accounts")
                    .entity(JsonNode::class.java)
                    .get()

            // Check pagination metadata
            assertEquals(2, result.get("count").asInt(), "Expected count to be 2")
            assertEquals(1, result.get("page").asInt(), "Expected page to be 1")
            assertEquals(100, result.get("perPage").asInt(), "Expected perPage to be 100")

            // Get the accounts from the result
            val accountsData = result.get("data")
            assertEquals(2, accountsData.size(), "Expected 2 accounts, but got ${accountsData.size()}")

            // Check that the accounts match the test data

            TestDataProvider.testAccounts.forEachIndexed { index, expectedAccount ->
                val actualAccount = accountsData.get(index)

                assertAll(
                    {
                        assertEquals(
                            expectedAccount.id,
                            actualAccount.get("id").asText(),
                            "Account ID mismatch: expected ${expectedAccount.id}, got ${actualAccount.get("id").asText()}",
                        )
                    },
                    {
                        assertEquals(
                            expectedAccount.platform.toString(),
                            actualAccount.get("platform").asText(),
                            "Account platform mismatch: expected ${expectedAccount.platform}, got ${actualAccount.get(
                                "platform",
                            ).asText()}",
                        )
                    },
                    {
                        assertEquals(
                            expectedAccount.login,
                            actualAccount.get("login").asText(),
                            "Account login mismatch: expected ${expectedAccount.login}, got ${actualAccount.get("login").asText()}",
                        )
                    },
                    {
                        assertEquals(
                            expectedAccount.name,
                            actualAccount.get("name").asText(),
                            "Account name mismatch: expected ${expectedAccount.name}, got ${actualAccount.get("name").asText()}",
                        )
                    },
                    {
                        assertEquals(
                            expectedAccount.avatarUrl,
                            actualAccount.get("avatarUrl").asText(),
                            "Account avatarUrl mismatch: expected ${expectedAccount.avatarUrl}, got ${actualAccount.get(
                                "avatarUrl",
                            ).asText()}",
                        )
                    },
                    {
                        assertEquals(
                            expectedAccount.url,
                            actualAccount.get("url").asText(),
                            "Account url mismatch: expected ${expectedAccount.url}, got ${actualAccount.get("url").asText()}",
                        )
                    },
                )
            }
        }

        @Test
        fun `should return account by id`() {
            val expectedAccount = TestDataProvider.testAccounts.first { it.id == "1" }

            val result: JsonNode =
                graphQlTester
                    .document(
                        """
            query {
                account(id: "1") {
                    id
                    platform
                    login
                    name
                    avatarUrl
                    url
                }
            }
        """,
                    ).execute()
                    .path("account")
                    .entity(JsonNode::class.java)
                    .get()

            // Check that the account matches the test data
            assertAll(
                {
                    assertEquals(
                        expectedAccount.id,
                        result.get("id").asText(),
                        "Account ID mismatch: expected ${expectedAccount.id}, got ${result.get("id").asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedAccount.platform.toString(),
                        result.get("platform").asText(),
                        "Account platform mismatch: expected ${expectedAccount.platform}, got ${result.get("platform").asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedAccount.login,
                        result.get("login").asText(),
                        "Account login mismatch: expected ${expectedAccount.login}, got ${result.get("login").asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedAccount.name,
                        result.get("name").asText(),
                        "Account name mismatch: expected ${expectedAccount.name}, got ${result.get("name").asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedAccount.avatarUrl,
                        result.get("avatarUrl").asText(),
                        "Account avatarUrl mismatch: expected ${expectedAccount.avatarUrl}, got ${result.get("avatarUrl").asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedAccount.url,
                        result.get("url").asText(),
                        "Account url mismatch: expected ${expectedAccount.url}, got ${result.get("url").asText()}",
                    )
                },
            )
        }
    }

    @Nested
    inner class Pagination : BaseDbTest() {
        @Test
        fun `should return accounts with pagination`() {
            // Test with page=1, perPage=1 (should return only the first account)
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
            query {
                accounts(page: 1, perPage: 1) {
                    count
                    page
                    perPage
                    data {
                        id
                        platform
                        login
                        name
                    }
                }
            }
        """,
                    ).execute()
                    .path("accounts")
                    .entity(JsonNode::class.java)
                    .get()

            // Check pagination metadata
            assertEquals(2, result.get("count").asInt(), "Expected count to be 2")
            assertEquals(1, result.get("page").asInt(), "Expected page to be 1")
            assertEquals(1, result.get("perPage").asInt(), "Expected perPage to be 1")

            // Get the accounts from the result
            val accountsData = result.get("data")
            assertEquals(1, accountsData.size(), "Expected 1 account, but got ${accountsData.size()}")

            // Check that the account matches the first test account
            val expectedAccount = TestDataProvider.testAccounts.first()
            val actualAccount = accountsData.get(0)

            assertAll(
                {
                    assertEquals(
                        expectedAccount.id,
                        actualAccount.get("id").asText(),
                        "Account ID mismatch: expected ${expectedAccount.id}, got ${actualAccount.get("id").asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedAccount.platform.toString(),
                        actualAccount.get("platform").asText(),
                        "Account platform mismatch: expected ${expectedAccount.platform}, got ${actualAccount.get("platform").asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedAccount.login,
                        actualAccount.get("login").asText(),
                        "Account login mismatch: expected ${expectedAccount.login}, got ${actualAccount.get("login").asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedAccount.name,
                        actualAccount.get("name").asText(),
                        "Account name mismatch: expected ${expectedAccount.name}, got ${actualAccount.get("name").asText()}",
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
                accounts {
                    count
                    page
                    perPage
                    data {
                        id
                        platform
                        login
                    }
                }
            }
        """,
                    ).execute()
                    .path("accounts")
                    .entity(JsonNode::class.java)
                    .get()

            // Check pagination metadata
            assertEquals(2, result.get("count").asInt(), "Expected count to be 2")
            assertEquals(1, result.get("page").asInt(), "Expected page to be 1 (default)")
            assertEquals(20, result.get("perPage").asInt(), "Expected perPage to be 20 (default)")

            // Get the accounts from the result
            val accountsData = result.get("data")
            assertEquals(2, accountsData.size(), "Expected 2 accounts, but got ${accountsData.size()}")
        }

        @Test
        fun `should return second page of accounts`() {
            // Test with page=2, perPage=1 (should return only the second account)
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
            query {
                accounts(page: 2, perPage: 1) {
                    count
                    page
                    perPage
                    data {
                        id
                        platform
                        login
                    }
                }
            }
        """,
                    ).execute()
                    .path("accounts")
                    .entity(JsonNode::class.java)
                    .get()

            // Check pagination metadata
            assertEquals(2, result.get("count").asInt(), "Expected count to be 2")
            assertEquals(2, result.get("page").asInt(), "Expected page to be 2")
            assertEquals(1, result.get("perPage").asInt(), "Expected perPage to be 1")

            // Get the accounts from the result
            val accountsData = result.get("data")
            assertEquals(1, accountsData.size(), "Expected 1 account, but got ${accountsData.size()}")

            // Check that the account matches the second test account
            val expectedAccount = TestDataProvider.testAccounts[1]
            val actualAccount = accountsData.get(0)

            assertAll(
                {
                    assertEquals(
                        expectedAccount.id,
                        actualAccount.get("id").asText(),
                        "Account ID mismatch: expected ${expectedAccount.id}, got ${actualAccount.get("id").asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedAccount.platform.toString(),
                        actualAccount.get("platform").asText(),
                        "Account platform mismatch: expected ${expectedAccount.platform}, got ${actualAccount.get("platform").asText()}",
                    )
                },
                {
                    assertEquals(
                        expectedAccount.login,
                        actualAccount.get("login").asText(),
                        "Account login mismatch: expected ${expectedAccount.login}, got ${actualAccount.get("login").asText()}",
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
                  accounts(page: 3, perPage: 1) {
                      count
                      page
                      perPage
                      data {
                          id
                          platform
                          login
                      }
                  }
              }
          """,
                    ).execute()
                    .path("accounts")
                    .entity(JsonNode::class.java)
                    .get()

            // Check pagination metadata
            assertEquals(2, result.get("count").asInt(), "Expected count to be 2")
            assertEquals(3, result.get("page").asInt(), "Expected page to be 3")
            assertEquals(1, result.get("perPage").asInt(), "Expected perPage to be 1")

            // Get the accounts from the result
            val accountsData = result.get("data")
            assertEquals(0, accountsData.size(), "Expected 0 accounts on page beyond available data, but got ${accountsData.size()}")
        }
    }

    @Nested
    inner class ErrorHandling : BaseDbTest() {
        @Test
        fun `should throw exception for non-existent account id`() {
            // Test with a non-existent account ID
            val nonExistentId = "999"

            graphQlTester
                .document(
                    """
            query {
                account(id: "$nonExistentId") {
                    id
                    platform
                    login
                }
            }
        """,
                ).execute()
                .errors()
                .expect { error ->
                    error.message?.contains("Account not found with id: $nonExistentId") ?: false
                }.verify()
        }

        @Test
        fun `should throw exception for invalid pagination parameters`() {
            // Test with invalid page parameter
            graphQlTester
                .document(
                    """
            query {
                accounts(page: 0, perPage: 10) {
                    count
                    page
                    perPage
                    data {
                        id
                        platform
                        login
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
                accounts(page: 1, perPage: 0) {
                    count
                    page
                    perPage
                    data {
                        id
                        platform
                        login
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
