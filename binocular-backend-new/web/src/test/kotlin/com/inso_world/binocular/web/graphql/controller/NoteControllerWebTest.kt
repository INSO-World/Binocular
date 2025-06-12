package com.inso_world.binocular.web.graphql.controller

import com.fasterxml.jackson.databind.JsonNode
import com.inso_world.binocular.web.BaseDbTest
import com.inso_world.binocular.web.entity.Note
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for NoteController.
 * Tests the functionality of retrieving notes with and without pagination,
 * as well as error handling for invalid requests.
 */
class NoteControllerWebTest : BaseDbTest() {

  @Nested
  inner class BasicFunctionality {
    @Test
    fun `should return all notes`() {
        val result: JsonNode = graphQlTester.document("""
            query {
                notes(page: 1, perPage: 100) {
                    count
                    page
                    perPage
                    data {
                        id
                        body
                        createdAt
                        updatedAt
                        system
                        resolvable
                        confidential
                        internal
                        imported
                        importedFrom
                    }
                }
            }
        """)
        .execute()
        .path("notes")
        .entity(JsonNode::class.java)
        .get()

        // Check pagination metadata
        assertEquals(2, result.get("count").asInt(), "Expected count to be 2")
        assertEquals(1, result.get("page").asInt(), "Expected page to be 1")
        assertEquals(100, result.get("perPage").asInt(), "Expected perPage to be 100")

        // Get the notes from the result
        val notesData = result.get("data")
        assertEquals(2, notesData.size(), "Expected 2 notes, but got ${notesData.size()}")

        // Check that the notes match the test data
        testNotes.forEachIndexed { index, expectedNote ->
            val actualNote = notesData.get(index)

            assertAll(
                { assertEquals(expectedNote.id, actualNote.get("id").asText(), "Note ID mismatch: expected ${expectedNote.id}, got ${actualNote.get("id").asText()}") },
                { assertEquals(expectedNote.body, actualNote.get("body").asText(), "Note body mismatch: expected ${expectedNote.body}, got ${actualNote.get("body").asText()}") },
                { assertEquals(expectedNote.createdAt, actualNote.get("createdAt").asText(), "Note createdAt mismatch: expected ${expectedNote.createdAt}, got ${actualNote.get("createdAt").asText()}") },
                { assertEquals(expectedNote.updatedAt, actualNote.get("updatedAt").asText(), "Note updatedAt mismatch: expected ${expectedNote.updatedAt}, got ${actualNote.get("updatedAt").asText()}") },
                { assertEquals(expectedNote.system, actualNote.get("system").asBoolean(), "Note system mismatch: expected ${expectedNote.system}, got ${actualNote.get("system").asBoolean()}") },
                { assertEquals(expectedNote.resolvable, actualNote.get("resolvable").asBoolean(), "Note resolvable mismatch: expected ${expectedNote.resolvable}, got ${actualNote.get("resolvable").asBoolean()}") },
                { assertEquals(expectedNote.confidential, actualNote.get("confidential").asBoolean(), "Note confidential mismatch: expected ${expectedNote.confidential}, got ${actualNote.get("confidential").asBoolean()}") },
                { assertEquals(expectedNote.internal, actualNote.get("internal").asBoolean(), "Note internal mismatch: expected ${expectedNote.internal}, got ${actualNote.get("internal").asBoolean()}") },
                { assertEquals(expectedNote.imported, actualNote.get("imported").asBoolean(), "Note imported mismatch: expected ${expectedNote.imported}, got ${actualNote.get("imported").asBoolean()}") },
                { assertEquals(expectedNote.importedFrom, actualNote.get("importedFrom").asText(), "Note importedFrom mismatch: expected ${expectedNote.importedFrom}, got ${actualNote.get("importedFrom").asText()}") }
            )
        }
    }

    @Test
    fun `should return note by id`() {
        val expectedNote = testNotes.first { it.id == "1" }

        val result: JsonNode = graphQlTester.document("""
            query {
                note(id: "1") {
                    id
                    body
                    createdAt
                    updatedAt
                    system
                    resolvable
                    confidential
                    internal
                    imported
                    importedFrom
                }
            }
        """)
        .execute()
        .path("note")
        .entity(JsonNode::class.java)
        .get()

        // Check that the note matches the test data
        assertAll(
            { assertEquals(expectedNote.id, result.get("id").asText(), "Note ID mismatch: expected ${expectedNote.id}, got ${result.get("id").asText()}") },
            { assertEquals(expectedNote.body, result.get("body").asText(), "Note body mismatch: expected ${expectedNote.body}, got ${result.get("body").asText()}") },
            { assertEquals(expectedNote.createdAt, result.get("createdAt").asText(), "Note createdAt mismatch: expected ${expectedNote.createdAt}, got ${result.get("createdAt").asText()}") },
            { assertEquals(expectedNote.updatedAt, result.get("updatedAt").asText(), "Note updatedAt mismatch: expected ${expectedNote.updatedAt}, got ${result.get("updatedAt").asText()}") },
            { assertEquals(expectedNote.system, result.get("system").asBoolean(), "Note system mismatch: expected ${expectedNote.system}, got ${result.get("system").asBoolean()}") },
            { assertEquals(expectedNote.resolvable, result.get("resolvable").asBoolean(), "Note resolvable mismatch: expected ${expectedNote.resolvable}, got ${result.get("resolvable").asBoolean()}") },
            { assertEquals(expectedNote.confidential, result.get("confidential").asBoolean(), "Note confidential mismatch: expected ${expectedNote.confidential}, got ${result.get("confidential").asBoolean()}") },
            { assertEquals(expectedNote.internal, result.get("internal").asBoolean(), "Note internal mismatch: expected ${expectedNote.internal}, got ${result.get("internal").asBoolean()}") },
            { assertEquals(expectedNote.imported, result.get("imported").asBoolean(), "Note imported mismatch: expected ${expectedNote.imported}, got ${result.get("imported").asBoolean()}") },
            { assertEquals(expectedNote.importedFrom, result.get("importedFrom").asText(), "Note importedFrom mismatch: expected ${expectedNote.importedFrom}, got ${result.get("importedFrom").asText()}") }
        )
    }
  }

  @Nested
  inner class Pagination {
    @Test
    fun `should return notes with pagination`() {
        // Test with page=1, perPage=1 (should return only the first note)
        val result: JsonNode = graphQlTester.document("""
            query {
                notes(page: 1, perPage: 1) {
                    count
                    page
                    perPage
                    data {
                        id
                        body
                        createdAt
                        updatedAt
                        importedFrom
                    }
                }
            }
        """)
        .execute()
        .path("notes")
        .entity(JsonNode::class.java)
        .get()

        // Check pagination metadata
        assertEquals(2, result.get("count").asInt(), "Expected count to be 2")
        assertEquals(1, result.get("page").asInt(), "Expected page to be 1")
        assertEquals(1, result.get("perPage").asInt(), "Expected perPage to be 1")

        // Get the notes from the result
        val notesData = result.get("data")
        assertEquals(1, notesData.size(), "Expected 1 note, but got ${notesData.size()}")

        // Check that the note matches the first test note
        val expectedNote = testNotes.first()
        val actualNote = notesData.get(0)

        assertAll(
            { assertEquals(expectedNote.id, actualNote.get("id").asText(), "Note ID mismatch: expected ${expectedNote.id}, got ${actualNote.get("id").asText()}") },
            { assertEquals(expectedNote.body, actualNote.get("body").asText(), "Note body mismatch: expected ${expectedNote.body}, got ${actualNote.get("body").asText()}") },
            { assertEquals(expectedNote.createdAt, actualNote.get("createdAt").asText(), "Note createdAt mismatch: expected ${expectedNote.createdAt}, got ${actualNote.get("createdAt").asText()}") },
            { assertEquals(expectedNote.updatedAt, actualNote.get("updatedAt").asText(), "Note updatedAt mismatch: expected ${expectedNote.updatedAt}, got ${actualNote.get("updatedAt").asText()}") },
            { assertEquals(expectedNote.importedFrom, actualNote.get("importedFrom").asText(), "Note importedFrom mismatch: expected ${expectedNote.importedFrom}, got ${actualNote.get("importedFrom").asText()}") }
        )
    }

    @Test
    fun `should handle null pagination parameters`() {
        // Test with null page and perPage parameters (should use defaults)
        val result: JsonNode = graphQlTester.document("""
            query {
                notes {
                    count
                    page
                    perPage
                    data {
                        id
                        body
                        createdAt
                        updatedAt
                        importedFrom
                    }
                }
            }
        """)
        .execute()
        .path("notes")
        .entity(JsonNode::class.java)
        .get()

        // Check pagination metadata
        assertEquals(2, result.get("count").asInt(), "Expected count to be 2")
        assertEquals(1, result.get("page").asInt(), "Expected page to be 1 (default)")
        assertEquals(20, result.get("perPage").asInt(), "Expected perPage to be 20 (default)")

        // Get the notes from the result
        val notesData = result.get("data")
        assertEquals(2, notesData.size(), "Expected 2 notes, but got ${notesData.size()}")
    }

    @Test
    fun `should return second page of notes`() {
        // Test with page=2, perPage=1 (should return only the second note)
        val result: JsonNode = graphQlTester.document("""
            query {
                notes(page: 2, perPage: 1) {
                    count
                    page
                    perPage
                    data {
                        id
                        body
                        createdAt
                        updatedAt
                        importedFrom
                    }
                }
            }
        """)
        .execute()
        .path("notes")
        .entity(JsonNode::class.java)
        .get()

        // Check pagination metadata
        assertEquals(2, result.get("count").asInt(), "Expected count to be 2")
        assertEquals(2, result.get("page").asInt(), "Expected page to be 2")
        assertEquals(1, result.get("perPage").asInt(), "Expected perPage to be 1")

        // Get the notes from the result
        val notesData = result.get("data")
        assertEquals(1, notesData.size(), "Expected 1 note, but got ${notesData.size()}")

        // Check that the note matches the second test note
        val expectedNote = testNotes[1]
        val actualNote = notesData.get(0)

        assertAll(
            { assertEquals(expectedNote.id, actualNote.get("id").asText(), "Note ID mismatch: expected ${expectedNote.id}, got ${actualNote.get("id").asText()}") },
            { assertEquals(expectedNote.body, actualNote.get("body").asText(), "Note body mismatch: expected ${expectedNote.body}, got ${actualNote.get("body").asText()}") },
            { assertEquals(expectedNote.createdAt, actualNote.get("createdAt").asText(), "Note createdAt mismatch: expected ${expectedNote.createdAt}, got ${actualNote.get("createdAt").asText()}") },
            { assertEquals(expectedNote.updatedAt, actualNote.get("updatedAt").asText(), "Note updatedAt mismatch: expected ${expectedNote.updatedAt}, got ${actualNote.get("updatedAt").asText()}") },
            { assertEquals(expectedNote.importedFrom, actualNote.get("importedFrom").asText(), "Note importedFrom mismatch: expected ${expectedNote.importedFrom}, got ${actualNote.get("importedFrom").asText()}") }
        )
    }

    @Test
    fun `should return empty list for page beyond available data`() {
      val result: JsonNode = graphQlTester.document(
        """
              query {
                  notes(page: 3, perPage: 1) {
                      count
                      page
                      perPage
                      data {
                          id
                          body
                          createdAt
                          updatedAt
                          importedFrom
                      }
                  }
              }
          """
      )
        .execute()
        .path("notes")
        .entity(JsonNode::class.java)
        .get()

      // Check pagination metadata
      assertEquals(2, result.get("count").asInt(), "Expected count to be 2")
      assertEquals(3, result.get("page").asInt(), "Expected page to be 3")
      assertEquals(1, result.get("perPage").asInt(), "Expected perPage to be 1")

      // Get the notes from the result
      val notesData = result.get("data")
      assertEquals(0, notesData.size(), "Expected 0 notes on page beyond available data, but got ${notesData.size()}")
    }
  }

  @Nested
  inner class ErrorHandling {
    @Test
    fun `should throw exception for non-existent note id`() {
        // Test with a non-existent note ID
        val nonExistentId = "999"

        graphQlTester.document("""
            query {
                note(id: "$nonExistentId") {
                    id
                    body
                    createdAt
                }
            }
        """)
        .execute()
        .errors()
        .expect { error ->
            error.message?.contains("Note not found with id: $nonExistentId") ?: false
        }
        .verify()
    }

    @Test
    fun `should throw exception for invalid pagination parameters`() {
        // Test with invalid page parameter
        graphQlTester.document("""
            query {
                notes(page: 0, perPage: 10) {
                    count
                    page
                    perPage
                    data {
                        id
                        body
                        createdAt
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
                notes(page: 1, perPage: 0) {
                    count
                    page
                    perPage
                    data {
                        id
                        body
                        createdAt
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
