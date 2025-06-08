package com.inso_world.binocular.web.graphql

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.graphql.test.tester.GraphQlTester

/**
 * Integration tests for the Note GraphQL queries.
 * These tests can be run against both the old and new implementations.
 */
class NoteControllerIntegrationTest : GraphQLIntegrationTestBase() {

    @Test
    fun `should get all notes`() {
        // Define the GraphQL query
        val query = """
            query {
                notes {
                    id
                    body
                    createdAt
                    updatedAt
                }
            }
        """.trimIndent()

        // Execute the query
        val response = executeQuery(query)

        // Verify the response contains notes data
        val notes = response.path("data.notes").entityList(Map::class.java).get()
        assertNotNull(notes, "Notes should not be null")

        // If there are notes, verify they have the expected fields
        if (notes.isNotEmpty()) {
            val firstNote = notes[0] as Map<*, *>
            assertNotNull(firstNote["id"], "Note should have an id")
            assertNotNull(firstNote["body"], "Note should have a body")
            assertNotNull(firstNote["createdAt"], "Note should have a createdAt timestamp")
            assertNotNull(firstNote["updatedAt"], "Note should have an updatedAt timestamp")
        }
    }

    @Test
    fun `should get note by id`() {
        // Define the GraphQL query to get the first note's ID
        val query = """
            query {
                notes(perPage: 1) {
                    id
                }
            }
        """.trimIndent()

        // Execute the query to get notes
        val notesResponse = executeQuery(query)

        // Get the list of notes
        val notes = notesResponse.path("data.notes").entityList(Map::class.java).get()

        // Skip the test if there are no notes
        if (notes.isEmpty()) {
            println("No notes found, skipping test")
            return
        }

        // Get the ID of the first note
        val firstNote = notes[0] as Map<*, *>
        val noteId = firstNote["id"] as String

        // Define the query to get a specific note
        val noteQuery = """
            query {
                note(id: "$noteId") {
                    id
                    body
                    createdAt
                    updatedAt
                }
            }
        """.trimIndent()

        // Execute the query
        val response = executeQuery(noteQuery)

        // Get the note data
        val note = response.path("data.note").entity(Map::class.java).get()

        // Verify the response
        assertEquals(noteId, note["id"], "Note ID should match the requested ID")
        assertNotNull(note["body"], "Note should have a body")
        assertNotNull(note["createdAt"], "Note should have a createdAt timestamp")
        assertNotNull(note["updatedAt"], "Note should have an updatedAt timestamp")
    }

    @Test
    fun `should handle pagination for notes`() {
        // Define the GraphQL query with pagination
        val query = """
            query {
                notes(page: 0, perPage: 2) {
                    id
                    body
                }
            }
        """.trimIndent()

        // Execute the query
        val response = executeQuery(query)

        // Get the notes data
        val notes = response.path("data.notes").entityList(Map::class.java).get()

        // Verify we got at most 2 notes (as requested by perPage)
        assertTrue(notes.size <= 2, "Expected at most 2 notes, but got ${notes.size}")
    }

    @Test
    fun `should handle non-existent note id`() {
        // Define the GraphQL query with a non-existent ID
        val query = """
            query {
                note(id: "non-existent-id") {
                    id
                    body
                }
            }
        """.trimIndent()

        // Execute the query
        val response = executeQuery(query)

        try {
            // Try to get the note data
            val note = response.path("data.note").entity(Map::class.java).get()

            // If we get here, the note should be null
            assertNull(note, "Note with non-existent ID should be null")
        } catch (e: Exception) {
            // If we get an exception, that's also acceptable as it means there was an error
            // This handles the case where the implementation returns an error instead of null
            println("Got expected exception for non-existent note ID: ${e.message}")
        }
    }
}
