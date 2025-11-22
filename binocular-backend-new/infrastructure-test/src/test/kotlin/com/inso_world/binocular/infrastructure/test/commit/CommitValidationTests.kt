package com.inso_world.binocular.infrastructure.test.commit

import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

internal class CommitValidationTests : BaseInfrastructureSpringTest() {
    @Autowired
    private lateinit var commitPort: CommitInfrastructurePort

    @Test
    fun `save 1 commit without branch, expect Constraint`() {
        val project = Project(name = "test-project")
        val repository = Repository(localPath = "test-repo", project = project)
        val committer = User(name = "Test Committer", repository = repository).apply { email = "committer@test.com" }

        assertThrows<ConstraintViolationException> {
            commitPort.create(
                Commit(
                    sha = "B".repeat(40),
                    commitDateTime = LocalDateTime.now(),
                    authorDateTime = null,
                    message = null,
                    repository = repository,
                    committer = committer,
                ),
            )
        }
    }
}
