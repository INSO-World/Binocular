package com.inso_world.binocular.infrastructure.sql.integration.validation

import com.inso_world.binocular.infrastructure.sql.TestApplication
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IUserDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity
import com.inso_world.binocular.infrastructure.sql.persistence.mapper.*
import com.inso_world.binocular.model.*
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(
    classes = [TestApplication::class],
)
@Transactional
internal class ValidationIntegrationTest
    @Autowired
    constructor(
        val userDao: IUserDao,
        val commitDao: ICommitDao,
    ) {
        @Test
        fun `user entity with blank name should fail validation`() {
            val invalidUser =
                UserEntity(gitSignature = "")
            assertThrows<ConstraintViolationException> {
                userDao.create(invalidUser)
            }
        }

        @Test
        fun `commit entity with null sha should fail validation`() {
            val invalidCommit = CommitEntity(sha = "null")
            assertThrows<ConstraintViolationException> {
                commitDao.create(invalidCommit)
            }
        }
    }
