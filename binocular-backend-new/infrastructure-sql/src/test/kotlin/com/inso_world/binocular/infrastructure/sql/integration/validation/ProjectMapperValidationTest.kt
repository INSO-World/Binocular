// package com.inso_world.binocular.infrastructure.sql.integration.validation
//
// import com.inso_world.binocular.infrastructure.sql.persistence.mapper.ProjectMapper
// import com.inso_world.binocular.model.Project
// import jakarta.validation.Validation
// import jakarta.validation.Validator
// import org.junit.jupiter.api.Assertions.assertTrue
// import org.junit.jupiter.api.Assertions.assertFalse
// import org.junit.jupiter.api.BeforeAll
// import org.junit.jupiter.api.Test
//
// class ProjectMapperValidationTest {
//    companion object {
//        lateinit var validator: Validator
//        lateinit var projectMapper: ProjectMapper
//
//        @JvmStatic
//        @BeforeAll
//        fun setup() {
//            validator = Validation.buildDefaultValidatorFactory().validator
//            val proxyFactory = TODO("Provide RelationshipProxyFactory mock or minimal impl")
//            projectMapper = ProjectMapper(proxyFactory)
//        }
//    }
//
//    @Test
//    fun `projectMapper toEntity and toDomain validation`() {
//        val invalidProject = Project(id = null, name = "") // assuming name is @NotBlank
//        val entity = projectMapper.toEntity(invalidProject)
//        val violationsEntity = validator.validate(entity)
//        assertFalse(violationsEntity.isEmpty(), "Invalid ProjectEntity should have violations")
//        val backToDomain = projectMapper.toDomain(entity)
//        val violationsDomain = validator.validate(backToDomain)
//        assertFalse(violationsDomain.isEmpty(), "Invalid Project should have violations")
//
//        val validProject = Project(id = "1", name = "valid")
//        val validEntity = projectMapper.toEntity(validProject)
//        val validViolationsEntity = validator.validate(validEntity)
//        assertTrue(validViolationsEntity.isEmpty(), "Valid ProjectEntity should have no violations")
//        val validBackToDomain = projectMapper.toDomain(validEntity)
//        val validViolationsDomain = validator.validate(validBackToDomain)
//        assertTrue(validViolationsDomain.isEmpty(), "Valid Project should have no violations")
//    }
// }
