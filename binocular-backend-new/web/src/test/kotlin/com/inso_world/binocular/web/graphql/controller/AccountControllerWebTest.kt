package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.web.BaseDbTest
import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.exception.NotFoundException
import com.inso_world.binocular.web.exception.ValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AccountControllerWebTest : BaseDbTest() {

    @Test
    fun `should return all accounts`() {
        // Test data is set up in BaseDbTest
        val result = graphQlTester.document("""
            query {
                accounts(page: 1, perPage: 100) {
                    id
                    platform
                    login
                    name
                    avatarUrl
                    url
                }
            }
        """)
        .execute()
        .path("accounts")
        .entityList(Account::class.java)

        // Check size
        result.hasSize(2)

        // Get the accounts from the result
        val accounts = result.get()

        // Check that the accounts match the test data
        testAccounts.forEachIndexed { index, expectedAccount ->
            val actualAccount = accounts[index]
            assert(actualAccount.id == expectedAccount.id) { "Account ID mismatch: expected ${expectedAccount.id}, got ${actualAccount.id}" }
            assert(actualAccount.platform == expectedAccount.platform) { "Account platform mismatch: expected ${expectedAccount.platform}, got ${actualAccount.platform}" }
            assert(actualAccount.login == expectedAccount.login) { "Account login mismatch: expected ${expectedAccount.login}, got ${actualAccount.login}" }
            assert(actualAccount.name == expectedAccount.name) { "Account name mismatch: expected ${expectedAccount.name}, got ${actualAccount.name}" }
            assert(actualAccount.avatarUrl == expectedAccount.avatarUrl) { "Account avatarUrl mismatch: expected ${expectedAccount.avatarUrl}, got ${actualAccount.avatarUrl}" }
            assert(actualAccount.url == expectedAccount.url) { "Account url mismatch: expected ${expectedAccount.url}, got ${actualAccount.url}" }
        }
    }

    @Test
    fun `should return account by id`() {
        // Test data is set up in BaseDbTest
        val expectedAccount = testAccounts.first { it.id == "1" }

        val result = graphQlTester.document("""
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
        """)
        .execute()
        .path("account")

        // Check that the account exists
        result.hasValue()

        // Get the account from the result
        val actualAccount = result.entity(Account::class.java).get()

        // Check that the account matches the test data
        assert(actualAccount.id == expectedAccount.id) { "Account ID mismatch: expected ${expectedAccount.id}, got ${actualAccount.id}" }
        assert(actualAccount.platform == expectedAccount.platform) { "Account platform mismatch: expected ${expectedAccount.platform}, got ${actualAccount.platform}" }
        assert(actualAccount.login == expectedAccount.login) { "Account login mismatch: expected ${expectedAccount.login}, got ${actualAccount.login}" }
        assert(actualAccount.name == expectedAccount.name) { "Account name mismatch: expected ${expectedAccount.name}, got ${actualAccount.name}" }
        assert(actualAccount.avatarUrl == expectedAccount.avatarUrl) { "Account avatarUrl mismatch: expected ${expectedAccount.avatarUrl}, got ${actualAccount.avatarUrl}" }
        assert(actualAccount.url == expectedAccount.url) { "Account url mismatch: expected ${expectedAccount.url}, got ${actualAccount.url}" }
    }

    @Test
    fun `should return accounts with pagination`() {
        // Test with page=1, perPage=1 (should return only the first account)
        val result = graphQlTester.document("""
            query {
                accounts(page: 1, perPage: 1) {
                    id
                    platform
                    login
                    name
                }
            }
        """)
        .execute()
        .path("accounts")
        .entityList(Account::class.java)

        // Check size
        result.hasSize(1)

        // Get the accounts from the result
        val accounts = result.get()

        // Check that the account matches the first test account
        val expectedAccount = testAccounts.first()
        val actualAccount = accounts.first()
        assert(actualAccount.id == expectedAccount.id) { "Account ID mismatch: expected ${expectedAccount.id}, got ${actualAccount.id}" }
        assert(actualAccount.platform == expectedAccount.platform) { "Account platform mismatch: expected ${expectedAccount.platform}, got ${actualAccount.platform}" }
        assert(actualAccount.login == expectedAccount.login) { "Account login mismatch: expected ${expectedAccount.login}, got ${actualAccount.login}" }
        assert(actualAccount.name == expectedAccount.name) { "Account name mismatch: expected ${expectedAccount.name}, got ${actualAccount.name}" }
    }

    @Test
    fun `should throw exception for non-existent account id`() {
        // Test with a non-existent account ID
        val nonExistentId = "999"

        graphQlTester.document("""
            query {
                account(id: "$nonExistentId") {
                    id
                    platform
                    login
                }
            }
        """)
        .execute()
        .errors()
        .expect { error ->
            error.message?.contains("Account not found with id: $nonExistentId") ?: false
        }
        .verify()
    }

    @Test
    fun `should throw exception for invalid pagination parameters`() {
        // Test with invalid page parameter
        graphQlTester.document("""
            query {
                accounts(page: 0, perPage: 10) {
                    id
                    platform
                    login
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
                accounts(page: 1, perPage: 0) {
                    id
                    platform
                    login
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
                accounts {
                    id
                    platform
                    login
                }
            }
        """)
        .execute()
        .path("accounts")
        .entityList(Account::class.java)

        // Check size (should return all accounts with default pagination)
        result.hasSize(2)
    }

    @Test
    fun `should return second page of accounts`() {
        // Test with page=2, perPage=1 (should return only the second account)
        val result = graphQlTester.document("""
            query {
                accounts(page: 2, perPage: 1) {
                    id
                    platform
                    login
                }
            }
        """)
        .execute()
        .path("accounts")
        .entityList(Account::class.java)

        // Check size
        result.hasSize(1)

        // Get the accounts from the result
        val accounts = result.get()

        // Check that the account matches the second test account
        val expectedAccount = testAccounts[1]
        val actualAccount = accounts.first()
        assert(actualAccount.id == expectedAccount.id) { "Account ID mismatch: expected ${expectedAccount.id}, got ${actualAccount.id}" }
        assert(actualAccount.platform == expectedAccount.platform) { "Account platform mismatch: expected ${expectedAccount.platform}, got ${actualAccount.platform}" }
        assert(actualAccount.login == expectedAccount.login) { "Account login mismatch: expected ${expectedAccount.login}, got ${actualAccount.login}" }
    }
}
