package com.inso_world.binocular.infrastructure.sql.integration.validation

import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.sql.TestApplication
import com.inso_world.binocular.infrastructure.sql.persistence.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.sql.persistence.mapper.RepositoryMapper
import com.inso_world.binocular.model.Commit
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.util.stream.Stream

@SpringBootTest(
    classes = [TestApplication::class],
)
internal class CommitMapperValidationTest {
    @Autowired
    private lateinit var commitMapper: CommitMapper

    @Autowired
    private lateinit var repositoryMapper: RepositoryMapper

    @Autowired
    private lateinit var proxyFactory: RelationshipProxyFactory
    lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        validator = Validation.buildDefaultValidatorFactory().validator
        val proxyFactory = proxyFactory
        val repositoryMapper = repositoryMapper
        commitMapper = CommitMapper(proxyFactory, repositoryMapper, validator)
    }

    companion object {
        @JvmStatic
        fun invalidCommitsForEntity(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    Commit(
                        id = null,
                        sha = "", // invalid: should be 40 chars
                        authorDateTime = LocalDateTime.now(),
                        commitDateTime = LocalDateTime.now(),
                        message = "Valid message",
                        repositoryId = "1",
                    ),
                    "sha",
                ),
                Arguments.of(
                    Commit(
                        id = null,
                        sha = "a".repeat(39), // invalid: should be 40 chars
                        authorDateTime = LocalDateTime.now(),
                        commitDateTime = LocalDateTime.now(),
                        message = "Valid message",
                        repositoryId = "1",
                    ),
                    "sha",
                ),
                Arguments.of(
                    Commit(
                        id = null,
                        sha = "b".repeat(41), // invalid: should be 40 chars
                        authorDateTime = LocalDateTime.now(),
                        commitDateTime = LocalDateTime.now(),
                        message = "Valid message",
                        repositoryId = "1",
                    ),
                    "sha",
                ),
                Arguments.of(
                    Commit(
                        id = null,
                        sha = "c".repeat(40),
                        authorDateTime = LocalDateTime.now(),
                        commitDateTime = null, // invalid: NotNull
                        message = "Valid message",
                        repositoryId = "1",
                    ),
                    "commitDateTime",
                ),
                Arguments.of(
                    Commit(
                        id = null,
                        sha = "c".repeat(40),
                        authorDateTime = LocalDateTime.now(),
                        commitDateTime = LocalDateTime.now().plusSeconds(30), // invalid: Future
                        message = "Valid message",
                        repositoryId = "1",
                    ),
                    "commitDateTime",
                ),
                Arguments.of(
                    Commit(
                        id = null,
                        sha = "d".repeat(40),
                        authorDateTime = LocalDateTime.now(),
                        commitDateTime = LocalDateTime.now(),
                        message = null, // invalid: NotBlank
                        repositoryId = "1",
                    ),
                    "message",
                ),
                Arguments.of(
                    Commit(
                        id = null,
                        sha = "e".repeat(40),
                        authorDateTime = LocalDateTime.now(),
                        commitDateTime = LocalDateTime.now(),
                        message = "Valid message",
                        repositoryId = null, // invalid: NotNull
                    ),
                    "repositoryId",
                ),
            )

//        @JvmStatic
//        fun invalidCommitsForDomain(): Stream<Commit> = invalidCommitsForEntity()
    }

    @ParameterizedTest
    @MethodSource("invalidCommitsForEntity")
    fun `toEntity should produce violations for single invalid property`(
        invalidCommit: Commit,
        propertyPath: String,
    ) {
        val e =
            assertThrows<com.inso_world.binocular.core.exception.BinocularValidationException> {
                val entity = commitMapper.toEntity(invalidCommit, mutableMapOf())
                validator.validate(entity)
            }
        assertThat(e.message).contains("propertyPath=$propertyPath")
//        val entity = commitMapper.toEntity(invalidCommit, mutableMapOf())
//        entity.id = 1
//        val violationsEntity = validator.validate(entity)
//        assertFalse(violationsEntity.isEmpty(), "Invalid CommitEntity should have violations")
    }

    @Test
    fun `toEntity should produce no violations for valid Commit`() {
        val validCommit =
            Commit(
                id = "1",
                sha = "a".repeat(40),
                authorDateTime = LocalDateTime.now(),
                commitDateTime = LocalDateTime.now(),
                message = "Valid commit",
                repositoryId = "1",
            )
        val validEntity = commitMapper.toEntity(validCommit, mutableMapOf())
        val validViolationsEntity = validator.validate(validEntity)
        assertTrue(validViolationsEntity.isEmpty(), "Valid CommitEntity should have no violations")
    }

//    @ParameterizedTest
//    @MethodSource("invalidCommitsForDomain")
//    fun `toDomain should produce violations for single invalid property`(invalidCommit: Commit) {
//        val entity =
//            assertDoesNotThrow {
//                commitMapper.toEntity(invalidCommit, mutableMapOf())
//            }
//        assertThrows<com.inso_world.binocular.core.exception.BinocularValidationException> {
//            commitMapper.toDomain(entity)
//        }
// //        val entity = commitMapper.toEntity(invalidCommit, mutableMapOf())
// //        entity.id = 1
// //        val backToDomain = commitMapper.toDomain(entity)
// //        val violationsDomain = validator.validate(backToDomain)
// //        assertFalse(violationsDomain.isEmpty(), "Invalid Commit should have violations")
//    }

    @Test
    fun `toDomain should produce no violations for valid CommitEntity`() {
        val validCommit =
            Commit(
                id = "1",
                sha = "a".repeat(40),
                authorDateTime = LocalDateTime.now(),
                commitDateTime = LocalDateTime.now(),
                message = "Valid commit",
                repositoryId = "1",
            )
        val validEntity = commitMapper.toEntity(validCommit, mutableMapOf())
        val validBackToDomain = commitMapper.toDomain(validEntity)
        val validViolationsDomain = validator.validate(validBackToDomain)
        assertTrue(validViolationsDomain.isEmpty(), "Valid Commit should have no violations")
    }
}
