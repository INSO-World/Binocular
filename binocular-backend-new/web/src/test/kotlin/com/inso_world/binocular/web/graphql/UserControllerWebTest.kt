package com.inso_world.binocular.web.graphql

import com.inso_world.binocular.web.BaseDbTest
import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.exception.NotFoundException
import com.inso_world.binocular.web.exception.ValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UserControllerWebTest : BaseDbTest() {

    @Test
    fun `should return all users`() {
        // Test data is set up in BaseDbTest
        val result = graphQlTester.document("""
            query {
                users(page: 1, perPage: 100) {
                    id
                    gitSignature
                }
            }
        """)
        .execute()
        .path("users")
        .entityList(User::class.java)

        // Check size
        result.hasSize(2)

        // Get the users from the result
        val users = result.get()

        // Check that the users match the test data
        testUsers.forEachIndexed { index, expectedUser ->
            val actualUser = users[index]
            assert(actualUser.id == expectedUser.id) { "User ID mismatch: expected ${expectedUser.id}, got ${actualUser.id}" }
            assert(actualUser.gitSignature == expectedUser.gitSignature) { "User gitSignature mismatch: expected ${expectedUser.gitSignature}, got ${actualUser.gitSignature}" }
        }
    }

    @Test
    fun `should return user by id`() {
        // Test data is set up in BaseDbTest
        val expectedUser = testUsers.first { it.id == "1" }

        val result = graphQlTester.document("""
            query {
                user(id: "1") {
                    id
                    gitSignature
                }
            }
        """)
        .execute()
        .path("user")

        // Check that the user exists
        result.hasValue()

        // Get the user from the result
        val actualUser = result.entity(User::class.java).get()

        // Check that the user matches the test data
        assert(actualUser.id == expectedUser.id) { "User ID mismatch: expected ${expectedUser.id}, got ${actualUser.id}" }
        assert(actualUser.gitSignature == expectedUser.gitSignature) { "User gitSignature mismatch: expected ${expectedUser.gitSignature}, got ${actualUser.gitSignature}" }
    }

    @Test
    fun `should return users with pagination`() {
        // Test with page=1, perPage=1 (should return only the first user)
        val result = graphQlTester.document("""
            query {
                users(page: 1, perPage: 1) {
                    id
                    gitSignature
                }
            }
        """)
        .execute()
        .path("users")
        .entityList(User::class.java)

        // Check size
        result.hasSize(1)

        // Get the users from the result
        val users = result.get()

        // Check that the user matches the first test user
        val expectedUser = testUsers.first()
        val actualUser = users.first()
        assert(actualUser.id == expectedUser.id) { "User ID mismatch: expected ${expectedUser.id}, got ${actualUser.id}" }
        assert(actualUser.gitSignature == expectedUser.gitSignature) { "User gitSignature mismatch: expected ${expectedUser.gitSignature}, got ${actualUser.gitSignature}" }
    }

    @Test
    fun `should throw exception for non-existent user id`() {
        // Test with a non-existent user ID
        val nonExistentId = "999"

        graphQlTester.document("""
            query {
                user(id: "$nonExistentId") {
                    id
                    gitSignature
                }
            }
        """)
        .execute()
        .errors()
        .expect { error ->
            error.message?.contains("User not found with id: $nonExistentId") ?: false
        }
        .verify()
    }

    @Test
    fun `should throw exception for invalid pagination parameters`() {
        // Test with invalid page parameter
        graphQlTester.document("""
            query {
                users(page: 0, perPage: 10) {
                    id
                    gitSignature
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
                users(page: 1, perPage: 0) {
                    id
                    gitSignature
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
                users {
                    id
                    gitSignature
                }
            }
        """)
        .execute()
        .path("users")
        .entityList(User::class.java)

        // Check size (should return all users with default pagination)
        result.hasSize(2)
    }

    @Test
    fun `should return second page of users`() {
        // Test with page=2, perPage=1 (should return only the second user)
        val result = graphQlTester.document("""
            query {
                users(page: 2, perPage: 1) {
                    id
                    gitSignature
                }
            }
        """)
        .execute()
        .path("users")
        .entityList(User::class.java)

        // Check size
        result.hasSize(1)

        // Get the users from the result
        val users = result.get()

        // Check that the user matches the second test user
        val expectedUser = testUsers[1]
        val actualUser = users.first()
        assert(actualUser.id == expectedUser.id) { "User ID mismatch: expected ${expectedUser.id}, got ${actualUser.id}" }
        assert(actualUser.gitSignature == expectedUser.gitSignature) { "User gitSignature mismatch: expected ${expectedUser.gitSignature}, got ${actualUser.gitSignature}" }
    }
}
