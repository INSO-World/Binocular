package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.web.BaseDbTest
import com.inso_world.binocular.web.entity.Note
import org.junit.jupiter.api.Assertions.assertAll
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
        // Test data is set up in BaseDbTest
        val result = graphQlTester.document("""
            query {
                notes(page: 1, perPage: 100) {
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
        .path("notes")
        .entityList(Note::class.java)

        // Check size
        result.hasSize(2)

        // Get the notes from the result
        val notes = result.get()

        // Check that the notes match the test data
        testNotes.forEachIndexed { index, expectedNote ->
            val actualNote = notes[index]

            assertAll(
                { assert(actualNote.id == expectedNote.id) { "Note ID mismatch: expected ${expectedNote.id}, got ${actualNote.id}" } },
                { assert(actualNote.body == expectedNote.body) { "Note body mismatch: expected ${expectedNote.body}, got ${actualNote.body}" } },
                { assert(actualNote.createdAt == expectedNote.createdAt) { "Note createdAt mismatch: expected ${expectedNote.createdAt}, got ${actualNote.createdAt}" } },
                { assert(actualNote.updatedAt == expectedNote.updatedAt) { "Note updatedAt mismatch: expected ${expectedNote.updatedAt}, got ${actualNote.updatedAt}" } },
                { assert(actualNote.system == expectedNote.system) { "Note system mismatch: expected ${expectedNote.system}, got ${actualNote.system}" } },
                { assert(actualNote.resolvable == expectedNote.resolvable) { "Note resolvable mismatch: expected ${expectedNote.resolvable}, got ${actualNote.resolvable}" } },
                { assert(actualNote.confidential == expectedNote.confidential) { "Note confidential mismatch: expected ${expectedNote.confidential}, got ${actualNote.confidential}" } },
                { assert(actualNote.internal == expectedNote.internal) { "Note internal mismatch: expected ${expectedNote.internal}, got ${actualNote.internal}" } },
                { assert(actualNote.imported == expectedNote.imported) { "Note imported mismatch: expected ${expectedNote.imported}, got ${actualNote.imported}" } },
                { assert(actualNote.importedFrom == expectedNote.importedFrom) { "Note importedFrom mismatch: expected ${expectedNote.importedFrom}, got ${actualNote.importedFrom}" } }
            )
        }
    }

    @Test
    fun `should return note by id`() {
        // Test data is set up in BaseDbTest
        val expectedNote = testNotes.first { it.id == "1" }

        val result = graphQlTester.document("""
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

        // Check that the note exists
        result.hasValue()

        // Get the note from the result
        val actualNote = result.entity(Note::class.java).get()

        // Check that the note matches the test data
        assertAll(
            { assert(actualNote.id == expectedNote.id) { "Note ID mismatch: expected ${expectedNote.id}, got ${actualNote.id}" } },
            { assert(actualNote.body == expectedNote.body) { "Note body mismatch: expected ${expectedNote.body}, got ${actualNote.body}" } },
            { assert(actualNote.createdAt == expectedNote.createdAt) { "Note createdAt mismatch: expected ${expectedNote.createdAt}, got ${actualNote.createdAt}" } },
            { assert(actualNote.updatedAt == expectedNote.updatedAt) { "Note updatedAt mismatch: expected ${expectedNote.updatedAt}, got ${actualNote.updatedAt}" } },
            { assert(actualNote.system == expectedNote.system) { "Note system mismatch: expected ${expectedNote.system}, got ${actualNote.system}" } },
            { assert(actualNote.resolvable == expectedNote.resolvable) { "Note resolvable mismatch: expected ${expectedNote.resolvable}, got ${actualNote.resolvable}" } },
            { assert(actualNote.confidential == expectedNote.confidential) { "Note confidential mismatch: expected ${expectedNote.confidential}, got ${actualNote.confidential}" } },
            { assert(actualNote.internal == expectedNote.internal) { "Note internal mismatch: expected ${expectedNote.internal}, got ${actualNote.internal}" } },
            { assert(actualNote.imported == expectedNote.imported) { "Note imported mismatch: expected ${expectedNote.imported}, got ${actualNote.imported}" } },
            { assert(actualNote.importedFrom == expectedNote.importedFrom) { "Note importedFrom mismatch: expected ${expectedNote.importedFrom}, got ${actualNote.importedFrom}" } }
        )
    }
  }

  @Nested
  inner class Pagination {
    @Test
    fun `should return notes with pagination`() {
        // Test with page=1, perPage=1 (should return only the first note)
        val result = graphQlTester.document("""
            query {
                notes(page: 1, perPage: 1) {
                    id
                    body
                    createdAt
                    updatedAt
                    importedFrom
                }
            }
        """)
        .execute()
        .path("notes")
        .entityList(Note::class.java)

        // Check size
        result.hasSize(1)

        // Get the notes from the result
        val notes = result.get()

        // Check that the note matches the first test note
        val expectedNote = testNotes.first()
        val actualNote = notes.first()

        assertAll(
            { assert(actualNote.id == expectedNote.id) { "Note ID mismatch: expected ${expectedNote.id}, got ${actualNote.id}" } },
            { assert(actualNote.body == expectedNote.body) { "Note body mismatch: expected ${expectedNote.body}, got ${actualNote.body}" } },
            { assert(actualNote.createdAt == expectedNote.createdAt) { "Note createdAt mismatch: expected ${expectedNote.createdAt}, got ${actualNote.createdAt}" } },
            { assert(actualNote.updatedAt == expectedNote.updatedAt) { "Note updatedAt mismatch: expected ${expectedNote.updatedAt}, got ${actualNote.updatedAt}" } },
            { assert(actualNote.importedFrom == expectedNote.importedFrom) { "Note importedFrom mismatch: expected ${expectedNote.importedFrom}, got ${actualNote.importedFrom}" } }
        )
    }

    @Test
    fun `should handle null pagination parameters`() {
        // Test with null page and perPage parameters (should use defaults)
        val result = graphQlTester.document("""
            query {
                notes {
                    id
                    body
                    createdAt
                    updatedAt
                    importedFrom
                }
            }
        """)
        .execute()
        .path("notes")
        .entityList(Note::class.java)

        // Check size (should return all notes with default pagination)
        result.hasSize(2)
    }

    @Test
    fun `should return second page of notes`() {
        // Test with page=2, perPage=1 (should return only the second note)
        val result = graphQlTester.document("""
            query {
                notes(page: 2, perPage: 1) {
                    id
                    body
                    createdAt
                    updatedAt
                    importedFrom
                }
            }
        """)
        .execute()
        .path("notes")
        .entityList(Note::class.java)

        // Check size
        result.hasSize(1)

        // Get the notes from the result
        val notes = result.get()

        // Check that the note matches the second test note
        val expectedNote = testNotes[1]
        val actualNote = notes.first()

        assertAll(
            { assert(actualNote.id == expectedNote.id) { "Note ID mismatch: expected ${expectedNote.id}, got ${actualNote.id}" } },
            { assert(actualNote.body == expectedNote.body) { "Note body mismatch: expected ${expectedNote.body}, got ${actualNote.body}" } },
            { assert(actualNote.createdAt == expectedNote.createdAt) { "Note createdAt mismatch: expected ${expectedNote.createdAt}, got ${actualNote.createdAt}" } },
            { assert(actualNote.updatedAt == expectedNote.updatedAt) { "Note updatedAt mismatch: expected ${expectedNote.updatedAt}, got ${actualNote.updatedAt}" } },
            { assert(actualNote.importedFrom == expectedNote.importedFrom) { "Note importedFrom mismatch: expected ${expectedNote.importedFrom}, got ${actualNote.importedFrom}" } }
        )
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
                    id
                    body
                    createdAt
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
                    id
                    body
                    createdAt
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
