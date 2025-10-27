package com.inso_world.binocular.infrastructure.test.commit

import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import com.inso_world.binocular.model.Commit
import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

internal class CommitValidationTests : BaseInfrastructureSpringTest() {
    @Autowired
    private lateinit var commitPort: CommitInfrastructurePort

    @Test
    fun `save 1 commit without branch, expect Constraint`() {
        assertThrows<ConstraintViolationException> {
            commitPort.create(
                Commit(
                    sha = "B".repeat(40),
                ),
            )
        }
    }
}
