// package com.inso_world.binocular.infrastructure.sql.integration.validation
//
// import com.inso_world.binocular.infrastructure.sql.persistence.mapper.RepositoryMapper
// import com.inso_world.binocular.model.Repository
// import jakarta.validation.Validation
// import jakarta.validation.Validator
// import org.junit.jupiter.api.Assertions.assertTrue
// import org.junit.jupiter.api.Assertions.assertFalse
// import org.junit.jupiter.api.BeforeAll
// import org.junit.jupiter.api.Test
//
// class RepositoryMapperValidationTest {
//    companion object {
//        lateinit var validator: Validator
//        lateinit var repositoryMapper: RepositoryMapper
//
//        @JvmStatic
//        @BeforeAll
//        fun setup() {
//            validator = Validation.buildDefaultValidatorFactory().validator
//            val proxyFactory = TODO("Provide RelationshipProxyFactory mock or minimal impl")
//            val commitMapper = TODO("Provide CommitMapper mock or minimal impl")
//            val projectMapper = TODO("Provide ProjectMapper mock or minimal impl")
//            repositoryMapper = RepositoryMapper(proxyFactory, commitMapper, projectMapper)
//        }
//    }
//
//    @Test
//    fun `repositoryMapper toEntity and toDomain validation`() {
//        val invalidRepo = Repository(id = null, name = "", projectId = null)
//        val entity = repositoryMapper.toEntity(invalidRepo)
//        val violationsEntity = validator.validate(entity)
//        assertFalse(violationsEntity.isEmpty(), "Invalid RepositoryEntity should have violations")
//        val backToDomain = repositoryMapper.toDomain(entity)
//        val violationsDomain = validator.validate(backToDomain)
//        assertFalse(violationsDomain.isEmpty(), "Invalid Repository should have violations")
//
//        val validRepo = Repository(id = "1", name = "repo", projectId = "1")
//        val validEntity = repositoryMapper.toEntity(validRepo)
//        val validViolationsEntity = validator.validate(validEntity)
//        assertTrue(validViolationsEntity.isEmpty(), "Valid RepositoryEntity should have no violations")
//        val validBackToDomain = repositoryMapper.toDomain(validEntity)
//        val validViolationsDomain = validator.validate(validBackToDomain)
//        assertTrue(validViolationsDomain.isEmpty(), "Valid Repository should have no violations")
//    }
// }
