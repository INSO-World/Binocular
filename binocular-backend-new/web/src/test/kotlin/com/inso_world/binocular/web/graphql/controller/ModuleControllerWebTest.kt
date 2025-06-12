package com.inso_world.binocular.web.graphql.controller

import com.fasterxml.jackson.databind.JsonNode
import com.inso_world.binocular.web.BaseDbTest
import com.inso_world.binocular.web.entity.Module
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for ModuleController.
 * Tests the functionality of retrieving modules with and without pagination,
 * as well as error handling for invalid requests.
 */
class ModuleControllerWebTest : BaseDbTest() {

  @Nested
  inner class BasicFunctionality {
    @Test
    fun `should return all modules`() {
        val result: JsonNode = graphQlTester.document("""
            query {
                modules(page: 1, perPage: 100) {
                    count
                    page
                    perPage
                    data {
                        id
                        path
                    }
                }
            }
        """)
        .execute()
        .path("modules")
        .entity(JsonNode::class.java)
        .get()

        // Check pagination metadata
        assertEquals(2, result.get("count").asInt(), "Expected count to be 2")
        assertEquals(1, result.get("page").asInt(), "Expected page to be 1")
        assertEquals(100, result.get("perPage").asInt(), "Expected perPage to be 100")

        // Get the modules from the result
        val modulesData = result.get("data")
        assertEquals(2, modulesData.size(), "Expected 2 modules, but got ${modulesData.size()}")

        // Check that the modules match the test data
        testModules.forEachIndexed { index, expectedModule ->
            val actualModule = modulesData.get(index)

            assertAll(
                { assertEquals(expectedModule.id, actualModule.get("id").asText(), "Module ID mismatch: expected ${expectedModule.id}, got ${actualModule.get("id").asText()}") },
                { assertEquals(expectedModule.path, actualModule.get("path").asText(), "Module path mismatch: expected ${expectedModule.path}, got ${actualModule.get("path").asText()}") }
            )
        }
    }

    @Test
    fun `should return module by id`() {
        val expectedModule = testModules.first { it.id == "1" }

        val result: JsonNode = graphQlTester.document("""
            query {
                module(id: "1") {
                    id
                    path
                }
            }
        """)
        .execute()
        .path("module")
        .entity(JsonNode::class.java)
        .get()

        // Check that the module matches the test data
        assertAll(
            { assertEquals(expectedModule.id, result.get("id").asText(), "Module ID mismatch: expected ${expectedModule.id}, got ${result.get("id").asText()}") },
            { assertEquals(expectedModule.path, result.get("path").asText(), "Module path mismatch: expected ${expectedModule.path}, got ${result.get("path").asText()}") }
        )
    }
  }

  @Nested
  inner class Pagination {
    @Test
    fun `should return modules with pagination`() {
        // Test with page=1, perPage=1 (should return only the first module)
        val result: JsonNode = graphQlTester.document("""
            query {
                modules(page: 1, perPage: 1) {
                    count
                    page
                    perPage
                    data {
                        id
                        path
                    }
                }
            }
        """)
        .execute()
        .path("modules")
        .entity(JsonNode::class.java)
        .get()

        // Check pagination metadata
        assertEquals(2, result.get("count").asInt(), "Expected count to be 2")
        assertEquals(1, result.get("page").asInt(), "Expected page to be 1")
        assertEquals(1, result.get("perPage").asInt(), "Expected perPage to be 1")

        // Get the modules from the result
        val modulesData = result.get("data")
        assertEquals(1, modulesData.size(), "Expected 1 module, but got ${modulesData.size()}")

        // Check that the module matches the first test module
        val expectedModule = testModules.first()
        val actualModule = modulesData.get(0)

        assertAll(
            { assertEquals(expectedModule.id, actualModule.get("id").asText(), "Module ID mismatch: expected ${expectedModule.id}, got ${actualModule.get("id").asText()}") },
            { assertEquals(expectedModule.path, actualModule.get("path").asText(), "Module path mismatch: expected ${expectedModule.path}, got ${actualModule.get("path").asText()}") }
        )
    }

    @Test
    fun `should handle null pagination parameters`() {
        // Test with null page and perPage parameters (should use defaults)
        val result: JsonNode = graphQlTester.document("""
            query {
                modules {
                    count
                    page
                    perPage
                    data {
                        id
                        path
                    }
                }
            }
        """)
        .execute()
        .path("modules")
        .entity(JsonNode::class.java)
        .get()

        // Check pagination metadata
        assertEquals(2, result.get("count").asInt(), "Expected count to be 2")
        assertEquals(1, result.get("page").asInt(), "Expected page to be 1 (default)")
        assertEquals(20, result.get("perPage").asInt(), "Expected perPage to be 20 (default)")

        // Get the modules from the result
        val modulesData = result.get("data")
        assertEquals(2, modulesData.size(), "Expected 2 modules, but got ${modulesData.size()}")
    }

    @Test
    fun `should return second page of modules`() {
        // Test with page=2, perPage=1 (should return only the second module)
        val result: JsonNode = graphQlTester.document("""
            query {
                modules(page: 2, perPage: 1) {
                    count
                    page
                    perPage
                    data {
                        id
                        path
                    }
                }
            }
        """)
        .execute()
        .path("modules")
        .entity(JsonNode::class.java)
        .get()

        // Check pagination metadata
        assertEquals(2, result.get("count").asInt(), "Expected count to be 2")
        assertEquals(2, result.get("page").asInt(), "Expected page to be 2")
        assertEquals(1, result.get("perPage").asInt(), "Expected perPage to be 1")

        // Get the modules from the result
        val modulesData = result.get("data")
        assertEquals(1, modulesData.size(), "Expected 1 module, but got ${modulesData.size()}")

        // Check that the module matches the second test module
        val expectedModule = testModules[1]
        val actualModule = modulesData.get(0)

        assertAll(
            { assertEquals(expectedModule.id, actualModule.get("id").asText(), "Module ID mismatch: expected ${expectedModule.id}, got ${actualModule.get("id").asText()}") },
            { assertEquals(expectedModule.path, actualModule.get("path").asText(), "Module path mismatch: expected ${expectedModule.path}, got ${actualModule.get("path").asText()}") }
        )
    }

    @Test
    fun `should return empty list for page beyond available data`() {
      val result: JsonNode = graphQlTester.document(
        """
              query {
                  modules(page: 3, perPage: 1) {
                      count
                      page
                      perPage
                      data {
                          id
                          path
                      }
                  }
              }
          """
      )
        .execute()
        .path("modules")
        .entity(JsonNode::class.java)
        .get()

      // Check pagination metadata
      assertEquals(2, result.get("count").asInt(), "Expected count to be 2")
      assertEquals(3, result.get("page").asInt(), "Expected page to be 3")
      assertEquals(1, result.get("perPage").asInt(), "Expected perPage to be 1")

      // Get the modules from the result
      val modulesData = result.get("data")
      assertEquals(0, modulesData.size(), "Expected 0 modules on page beyond available data, but got ${modulesData.size()}")
    }
  }

  @Nested
  inner class ErrorHandling {
    @Test
    fun `should throw exception for non-existent module id`() {
        // Test with a non-existent module ID
        val nonExistentId = "999"

        graphQlTester.document("""
            query {
                module(id: "$nonExistentId") {
                    id
                    path
                }
            }
        """)
        .execute()
        .errors()
        .expect { error ->
            error.message?.contains("Module not found with id: $nonExistentId") ?: false
        }
        .verify()
    }

    @Test
    fun `should throw exception for invalid pagination parameters`() {
        // Test with invalid page parameter
        graphQlTester.document("""
            query {
                modules(page: 0, perPage: 10) {
                    count
                    page
                    perPage
                    data {
                        id
                        path
                    }
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
                modules(page: 1, perPage: 0) {
                    count
                    page
                    perPage
                    data {
                        id
                        path
                    }
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
