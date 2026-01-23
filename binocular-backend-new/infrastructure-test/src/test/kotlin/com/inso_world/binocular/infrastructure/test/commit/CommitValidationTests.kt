package com.inso_world.binocular.infrastructure.test.commit

import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

internal class CommitValidationTests : BaseInfrastructureSpringTest() {
    @Autowired
    private lateinit var commitPort: CommitInfrastructurePort

    @Test
    fun `save 1 commit without branch, expect ConstraintViolationException`() {
        val project = Project(name = "test-project")
        val repository = Repository(localPath = "test-repo", project = project)
        val developer = Developer(name = "Test Committer", email = "committer@test.com", repository = repository)

        assertThrows<ConstraintViolationException> {
            commitPort.create(
                Commit(
                    sha = "B".repeat(40),
                    authorSignature = Signature(developer = developer, timestamp = LocalDateTime.now()),
                    message = null,
                    repository = repository,
                ),
            )
        }
    }
}