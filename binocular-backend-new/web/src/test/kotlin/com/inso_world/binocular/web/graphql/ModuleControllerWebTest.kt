package com.inso_world.binocular.web.graphql

import com.inso_world.binocular.web.BaseDbTest
import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.exception.NotFoundException
import com.inso_world.binocular.web.exception.ValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ModuleControllerWebTest : BaseDbTest() {

    @Test
    fun `should return all modules`() {
        // Test data is set up in BaseDbTest
        val result = graphQlTester.document("""
            query {
                modules(page: 1, perPage: 100) {
                    id
                    path
                }
            }
        """)
        .execute()
        .path("modules")
        .entityList(Module::class.java)

        // Check size
        result.hasSize(2)

        // Get the modules from the result
        val modules = result.get()

        // Check that the modules match the test data
        testModules.forEachIndexed { index, expectedModule ->
            val actualModule = modules[index]
            assert(actualModule.id == expectedModule.id) { "Module ID mismatch: expected ${expectedModule.id}, got ${actualModule.id}" }
            assert(actualModule.path == expectedModule.path) { "Module path mismatch: expected ${expectedModule.path}, got ${actualModule.path}" }
        }
    }

    @Test
    fun `should return module by id`() {
        // Test data is set up in BaseDbTest
        val expectedModule = testModules.first { it.id == "1" }

        val result = graphQlTester.document("""
            query {
                module(id: "1") {
                    id
                    path
                }
            }
        """)
        .execute()
        .path("module")

        // Check that the module exists
        result.hasValue()

        // Get the module from the result
        val actualModule = result.entity(Module::class.java).get()

        // Check that the module matches the test data
        assert(actualModule.id == expectedModule.id) { "Module ID mismatch: expected ${expectedModule.id}, got ${actualModule.id}" }
        assert(actualModule.path == expectedModule.path) { "Module path mismatch: expected ${expectedModule.path}, got ${actualModule.path}" }
    }

    @Test
    fun `should return modules with pagination`() {
        // Test with page=1, perPage=1 (should return only the first module)
        val result = graphQlTester.document("""
            query {
                modules(page: 1, perPage: 1) {
                    id
                    path
                }
            }
        """)
        .execute()
        .path("modules")
        .entityList(Module::class.java)

        // Check size
        result.hasSize(1)

        // Get the modules from the result
        val modules = result.get()

        // Check that the module matches the first test module
        val expectedModule = testModules.first()
        val actualModule = modules.first()
        assert(actualModule.id == expectedModule.id) { "Module ID mismatch: expected ${expectedModule.id}, got ${actualModule.id}" }
        assert(actualModule.path == expectedModule.path) { "Module path mismatch: expected ${expectedModule.path}, got ${actualModule.path}" }
    }

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
                    id
                    path
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
                    id
                    path
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
                modules {
                    id
                    path
                }
            }
        """)
        .execute()
        .path("modules")
        .entityList(Module::class.java)

        // Check size (should return all modules with default pagination)
        result.hasSize(2)
    }

    @Test
    fun `should return second page of modules`() {
        // Test with page=2, perPage=1 (should return only the second module)
        val result = graphQlTester.document("""
            query {
                modules(page: 2, perPage: 1) {
                    id
                    path
                }
            }
        """)
        .execute()
        .path("modules")
        .entityList(Module::class.java)

        // Check size
        result.hasSize(1)

        // Get the modules from the result
        val modules = result.get()

        // Check that the module matches the second test module
        val expectedModule = testModules[1]
        val actualModule = modules.first()
        assert(actualModule.id == expectedModule.id) { "Module ID mismatch: expected ${expectedModule.id}, got ${actualModule.id}" }
        assert(actualModule.path == expectedModule.path) { "Module path mismatch: expected ${expectedModule.path}, got ${actualModule.path}" }
    }
}
