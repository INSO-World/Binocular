package com.inso_world.binocular.web.graphql

import com.inso_world.binocular.web.BaseDbTest
import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.exception.NotFoundException
import com.inso_world.binocular.web.exception.ValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class IssueControllerWebTest : BaseDbTest() {

    @Test
    fun `should return all issues`() {
        // Test data is set up in BaseDbTest
        val result = graphQlTester.document("""
            query {
                issues(page: 1, perPage: 100) {
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
        .path("issues")
        .entityList(Issue::class.java)

        // Check size
        result.hasSize(2)

        // Get the issues from the result
        val issues = result.get()

        // Check that the issues match the test data
        testIssues.forEachIndexed { index, expectedIssue ->
            val actualIssue = issues[index]
            assert(actualIssue.id == expectedIssue.id) { "Issue ID mismatch: expected ${expectedIssue.id}, got ${actualIssue.id}" }
            assert(actualIssue.iid == expectedIssue.iid) { "Issue IID mismatch: expected ${expectedIssue.iid}, got ${actualIssue.iid}" }
            assert(actualIssue.title == expectedIssue.title) { "Issue title mismatch: expected ${expectedIssue.title}, got ${actualIssue.title}" }
            assert(actualIssue.description == expectedIssue.description) { "Issue description mismatch: expected ${expectedIssue.description}, got ${actualIssue.description}" }
            assert(actualIssue.state == expectedIssue.state) { "Issue state mismatch: expected ${expectedIssue.state}, got ${actualIssue.state}" }
            assert(actualIssue.webUrl == expectedIssue.webUrl) { "Issue webUrl mismatch: expected ${expectedIssue.webUrl}, got ${actualIssue.webUrl}" }
            assert(actualIssue.labels == expectedIssue.labels) { "Issue labels mismatch: expected ${expectedIssue.labels}, got ${actualIssue.labels}" }
        }
    }

    @Test
    fun `should return issue by id`() {
        // Test data is set up in BaseDbTest
        val expectedIssue = testIssues.first { it.id == "1" }

        val result = graphQlTester.document("""
            query {
                issue(id: "1") {
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
        .path("issue")

        // Check that the issue exists
        result.hasValue()

        // Get the issue from the result
        val actualIssue = result.entity(Issue::class.java).get()

        // Check that the issue matches the test data
        assert(actualIssue.id == expectedIssue.id) { "Issue ID mismatch: expected ${expectedIssue.id}, got ${actualIssue.id}" }
        assert(actualIssue.iid == expectedIssue.iid) { "Issue IID mismatch: expected ${expectedIssue.iid}, got ${actualIssue.iid}" }
        assert(actualIssue.title == expectedIssue.title) { "Issue title mismatch: expected ${expectedIssue.title}, got ${actualIssue.title}" }
        assert(actualIssue.description == expectedIssue.description) { "Issue description mismatch: expected ${expectedIssue.description}, got ${actualIssue.description}" }
        assert(actualIssue.state == expectedIssue.state) { "Issue state mismatch: expected ${expectedIssue.state}, got ${actualIssue.state}" }
        assert(actualIssue.webUrl == expectedIssue.webUrl) { "Issue webUrl mismatch: expected ${expectedIssue.webUrl}, got ${actualIssue.webUrl}" }
        assert(actualIssue.labels == expectedIssue.labels) { "Issue labels mismatch: expected ${expectedIssue.labels}, got ${actualIssue.labels}" }
    }

    @Test
    fun `should return issues with pagination`() {
        // Test with page=1, perPage=1 (should return only the first issue)
        val result = graphQlTester.document("""
            query {
                issues(page: 1, perPage: 1) {
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
        .path("issues")
        .entityList(Issue::class.java)

        // Check size
        result.hasSize(1)

        // Get the issues from the result
        val issues = result.get()

        // Check that the issue matches the first test issue
        val expectedIssue = testIssues.first()
        val actualIssue = issues.first()
        assert(actualIssue.id == expectedIssue.id) { "Issue ID mismatch: expected ${expectedIssue.id}, got ${actualIssue.id}" }
        assert(actualIssue.iid == expectedIssue.iid) { "Issue IID mismatch: expected ${expectedIssue.iid}, got ${actualIssue.iid}" }
        assert(actualIssue.title == expectedIssue.title) { "Issue title mismatch: expected ${expectedIssue.title}, got ${actualIssue.title}" }
        assert(actualIssue.description == expectedIssue.description) { "Issue description mismatch: expected ${expectedIssue.description}, got ${actualIssue.description}" }
        assert(actualIssue.state == expectedIssue.state) { "Issue state mismatch: expected ${expectedIssue.state}, got ${actualIssue.state}" }
        assert(actualIssue.webUrl == expectedIssue.webUrl) { "Issue webUrl mismatch: expected ${expectedIssue.webUrl}, got ${actualIssue.webUrl}" }
        assert(actualIssue.labels == expectedIssue.labels) { "Issue labels mismatch: expected ${expectedIssue.labels}, got ${actualIssue.labels}" }
    }

    @Test
    fun `should throw exception for non-existent issue id`() {
        // Test with a non-existent issue ID
        val nonExistentId = "999"

        graphQlTester.document("""
            query {
                issue(id: "$nonExistentId") {
                    id
                    iid
                    title
                }
            }
        """)
        .execute()
        .errors()
        .expect { error ->
            error.message?.contains("Issue not found with id: $nonExistentId") ?: false
        }
        .verify()
    }

    @Test
    fun `should throw exception for invalid pagination parameters`() {
        // Test with invalid page parameter
        graphQlTester.document("""
            query {
                issues(page: 0, perPage: 10) {
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
                issues(page: 1, perPage: 0) {
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

    @Test
    fun `should handle null pagination parameters`() {
        // Test with null page and perPage parameters (should use defaults)
        val result = graphQlTester.document("""
            query {
                issues {
                    id
                    iid
                    title
                }
            }
        """)
        .execute()
        .path("issues")
        .entityList(Issue::class.java)

        // Check size (should return all issues with default pagination)
        result.hasSize(2)
    }

    @Test
    fun `should return second page of issues`() {
        // Test with page=2, perPage=1 (should return only the second issue)
        val result = graphQlTester.document("""
            query {
                issues(page: 2, perPage: 1) {
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
        .path("issues")
        .entityList(Issue::class.java)

        // Check size
        result.hasSize(1)

        // Get the issues from the result
        val issues = result.get()

        // Check that the issue matches the second test issue
        val expectedIssue = testIssues[1]
        val actualIssue = issues.first()
        assert(actualIssue.id == expectedIssue.id) { "Issue ID mismatch: expected ${expectedIssue.id}, got ${actualIssue.id}" }
        assert(actualIssue.iid == expectedIssue.iid) { "Issue IID mismatch: expected ${expectedIssue.iid}, got ${actualIssue.iid}" }
        assert(actualIssue.title == expectedIssue.title) { "Issue title mismatch: expected ${expectedIssue.title}, got ${actualIssue.title}" }
        assert(actualIssue.description == expectedIssue.description) { "Issue description mismatch: expected ${expectedIssue.description}, got ${actualIssue.description}" }
        assert(actualIssue.state == expectedIssue.state) { "Issue state mismatch: expected ${expectedIssue.state}, got ${actualIssue.state}" }
        assert(actualIssue.webUrl == expectedIssue.webUrl) { "Issue webUrl mismatch: expected ${expectedIssue.webUrl}, got ${actualIssue.webUrl}" }
        assert(actualIssue.labels == expectedIssue.labels) { "Issue labels mismatch: expected ${expectedIssue.labels}, got ${actualIssue.labels}" }
    }
}
