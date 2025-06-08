package com.inso_world.binocular.web.graphql.error

import com.inso_world.binocular.web.exception.NotFoundException
import com.inso_world.binocular.web.exception.ValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GraphQLValidationUtilsTest {

    @Test
    fun `validatePagination should not throw exception for valid parameters`() {
        // Valid parameters
        GraphQLValidationUtils.validatePagination(1, 10)
        GraphQLValidationUtils.validatePagination(100, 50)
        
        // Null parameters (should use defaults)
        GraphQLValidationUtils.validatePagination(null, null)
        GraphQLValidationUtils.validatePagination(1, null)
        GraphQLValidationUtils.validatePagination(null, 10)
    }

    @Test
    fun `validatePagination should throw exception for invalid page parameter`() {
        val exception = assertThrows<ValidationException> {
            GraphQLValidationUtils.validatePagination(0, 10)
        }
        
        assert(exception.message?.contains("Page must be greater than or equal to 1") == true)
        assert(exception.extensions["parameter"] == "page")
    }

    @Test
    fun `validatePagination should throw exception for invalid perPage parameter`() {
        val exception = assertThrows<ValidationException> {
            GraphQLValidationUtils.validatePagination(1, 0)
        }
        
        assert(exception.message?.contains("PerPage must be greater than or equal to 1") == true)
        assert(exception.extensions["parameter"] == "perPage")
    }

    @Test
    fun `validatePagination should throw exception for negative page parameter`() {
        val exception = assertThrows<ValidationException> {
            GraphQLValidationUtils.validatePagination(-5, 10)
        }
        
        assert(exception.message?.contains("Page must be greater than or equal to 1") == true)
        assert(exception.extensions["parameter"] == "page")
    }

    @Test
    fun `validatePagination should throw exception for negative perPage parameter`() {
        val exception = assertThrows<ValidationException> {
            GraphQLValidationUtils.validatePagination(1, -5)
        }
        
        assert(exception.message?.contains("PerPage must be greater than or equal to 1") == true)
        assert(exception.extensions["parameter"] == "perPage")
    }

    @Test
    fun `requireEntityExists should return entity if it exists`() {
        val entity = "Test Entity"
        val result = GraphQLValidationUtils.requireEntityExists(entity, "TestEntity", "1")
        
        assert(result == entity)
    }

    @Test
    fun `requireEntityExists should throw exception if entity is null`() {
        val exception = assertThrows<NotFoundException> {
            GraphQLValidationUtils.requireEntityExists(null, "TestEntity", "1")
        }
        
        assert(exception.message?.contains("TestEntity not found with id: 1") == true)
    }
}
