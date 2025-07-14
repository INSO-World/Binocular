// package com.inso_world.binocular.infrastructure.sql.integration.validation
//
// import com.inso_world.binocular.infrastructure.sql.persistence.mapper.UserMapper
// import com.inso_world.binocular.model.User
// import jakarta.validation.Validation
// import jakarta.validation.Validator
// import org.junit.jupiter.api.Assertions.assertTrue
// import org.junit.jupiter.api.Assertions.assertFalse
// import org.junit.jupiter.api.BeforeAll
// import org.junit.jupiter.api.Test
//
// class UserMapperValidationTest {
//    companion object {
//        lateinit var validator: Validator
//        lateinit var userMapper: UserMapper
//
//        @JvmStatic
//        @BeforeAll
//        fun setup() {
//            validator = Validation.buildDefaultValidatorFactory().validator
//            val proxyFactory = TODO("Provide RelationshipProxyFactory mock or minimal impl")
//            userMapper = UserMapper(proxyFactory)
//        }
//    }
//
//    @Test
//    fun `userMapper toEntity and toDomain validation`() {
//        val invalidUser = User(id = null, name = "") // assuming name is @NotBlank
//        val entity = userMapper.toEntity(invalidUser)
//        val violationsEntity = validator.validate(entity)
//        assertFalse(violationsEntity.isEmpty(), "Invalid UserEntity should have violations")
//        val backToDomain = userMapper.toDomain(entity)
//        val violationsDomain = validator.validate(backToDomain)
//        assertFalse(violationsDomain.isEmpty(), "Invalid User should have violations")
//
//        val validUser = User(id = "1", name = "valid")
//        val validEntity = userMapper.toEntity(validUser)
//        val validViolationsEntity = validator.validate(validEntity)
//        assertTrue(validViolationsEntity.isEmpty(), "Valid UserEntity should have no violations")
//        val validBackToDomain = userMapper.toDomain(validEntity)
//        val validViolationsDomain = validator.validate(validBackToDomain)
//        assertTrue(validViolationsDomain.isEmpty(), "Valid User should have no violations")
//    }
// }
