package com.inso_world.binocular.web.graphql

import com.inso_world.binocular.web.BaseDbTest
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.exception.NotFoundException
import com.inso_world.binocular.web.exception.ValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FileControllerWebTest : BaseDbTest() {

    @Test
    fun `should return all files`() {
        // Test data is set up in BaseDbTest
        val result = graphQlTester.document("""
            query {
                files(page: 1, perPage: 100) {
                    id
                    path
                    webUrl
                    maxLength
                }
            }
        """)
        .execute()
        .path("files")
        .entityList(File::class.java)

        // Check size
        result.hasSize(2)

        // Get the files from the result
        val files = result.get()

        // Check that the files match the test data
        testFiles.forEachIndexed { index, expectedFile ->
            val actualFile = files[index]
            assert(actualFile.id == expectedFile.id) { "File ID mismatch: expected ${expectedFile.id}, got ${actualFile.id}" }
            assert(actualFile.path == expectedFile.path) { "File path mismatch: expected ${expectedFile.path}, got ${actualFile.path}" }
            assert(actualFile.webUrl == expectedFile.webUrl) { "File webUrl mismatch: expected ${expectedFile.webUrl}, got ${actualFile.webUrl}" }
            assert(actualFile.maxLength == expectedFile.maxLength) { "File maxLength mismatch: expected ${expectedFile.maxLength}, got ${actualFile.maxLength}" }
        }
    }

    @Test
    fun `should return file by id`() {
        // Test data is set up in BaseDbTest
        val expectedFile = testFiles.first { it.id == "1" }

        val result = graphQlTester.document("""
            query {
                file(id: "1") {
                    id
                    path
                    webUrl
                    maxLength
                }
            }
        """)
        .execute()
        .path("file")

        // Check that the file exists
        result.hasValue()

        // Get the file from the result
        val actualFile = result.entity(File::class.java).get()

        // Check that the file matches the test data
        assert(actualFile.id == expectedFile.id) { "File ID mismatch: expected ${expectedFile.id}, got ${actualFile.id}" }
        assert(actualFile.path == expectedFile.path) { "File path mismatch: expected ${expectedFile.path}, got ${actualFile.path}" }
        assert(actualFile.webUrl == expectedFile.webUrl) { "File webUrl mismatch: expected ${expectedFile.webUrl}, got ${actualFile.webUrl}" }
        assert(actualFile.maxLength == expectedFile.maxLength) { "File maxLength mismatch: expected ${expectedFile.maxLength}, got ${actualFile.maxLength}" }
    }

    @Test
    fun `should return files with pagination`() {
        // Test with page=1, perPage=1 (should return only the first file)
        val result = graphQlTester.document("""
            query {
                files(page: 1, perPage: 1) {
                    id
                    path
                    webUrl
                    maxLength
                }
            }
        """)
        .execute()
        .path("files")
        .entityList(File::class.java)

        // Check size
        result.hasSize(1)

        // Get the files from the result
        val files = result.get()

        // Check that the file matches the first test file
        val expectedFile = testFiles.first()
        val actualFile = files.first()
        assert(actualFile.id == expectedFile.id) { "File ID mismatch: expected ${expectedFile.id}, got ${actualFile.id}" }
        assert(actualFile.path == expectedFile.path) { "File path mismatch: expected ${expectedFile.path}, got ${actualFile.path}" }
        assert(actualFile.webUrl == expectedFile.webUrl) { "File webUrl mismatch: expected ${expectedFile.webUrl}, got ${actualFile.webUrl}" }
        assert(actualFile.maxLength == expectedFile.maxLength) { "File maxLength mismatch: expected ${expectedFile.maxLength}, got ${actualFile.maxLength}" }
    }

    @Test
    fun `should throw exception for non-existent file id`() {
        // Test with a non-existent file ID
        val nonExistentId = "999"

        graphQlTester.document("""
            query {
                file(id: "$nonExistentId") {
                    id
                    path
                    webUrl
                }
            }
        """)
        .execute()
        .errors()
        .expect { error ->
            error.message?.contains("File not found with id: $nonExistentId") ?: false
        }
        .verify()
    }

    @Test
    fun `should throw exception for invalid pagination parameters`() {
        // Test with invalid page parameter
        graphQlTester.document("""
            query {
                files(page: 0, perPage: 10) {
                    id
                    path
                    webUrl
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
                files(page: 1, perPage: 0) {
                    id
                    path
                    webUrl
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

    @Test
    fun `should handle null pagination parameters`() {
        // Test with null page and perPage parameters (should use defaults)
        val result = graphQlTester.document("""
            query {
                files {
                    id
                    path
                    webUrl
                }
            }
        """)
        .execute()
        .path("files")
        .entityList(File::class.java)

        // Check size (should return all files with default pagination)
        result.hasSize(2)
    }

    @Test
    fun `should return second page of files`() {
        // Test with page=2, perPage=1 (should return only the second file)
        val result = graphQlTester.document("""
            query {
                files(page: 2, perPage: 1) {
                    id
                    path
                    webUrl
                    maxLength
                }
            }
        """)
        .execute()
        .path("files")
        .entityList(File::class.java)

        // Check size
        result.hasSize(1)

        // Get the files from the result
        val files = result.get()

        // Check that the file matches the second test file
        val expectedFile = testFiles[1]
        val actualFile = files.first()
        assert(actualFile.id == expectedFile.id) { "File ID mismatch: expected ${expectedFile.id}, got ${actualFile.id}" }
        assert(actualFile.path == expectedFile.path) { "File path mismatch: expected ${expectedFile.path}, got ${actualFile.path}" }
        assert(actualFile.webUrl == expectedFile.webUrl) { "File webUrl mismatch: expected ${expectedFile.webUrl}, got ${actualFile.webUrl}" }
        assert(actualFile.maxLength == expectedFile.maxLength) { "File maxLength mismatch: expected ${expectedFile.maxLength}, got ${actualFile.maxLength}" }
    }
}
