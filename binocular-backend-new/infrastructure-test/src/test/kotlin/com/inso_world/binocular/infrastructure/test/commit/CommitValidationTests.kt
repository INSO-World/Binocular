package com.inso_world.binocular.infrastructure.test.commit

import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

internal class CommitValidationTests : BaseInfrastructureSpringTest() {
    @Autowired
    private lateinit var repoPort: RepositoryInfrastructurePort

    @Autowired
    private lateinit var projectPort: ProjectInfrastructurePort

    private lateinit var project: Project

    @BeforeEach
    fun setUp() {
        project = projectPort.create(Project(name = "test-project"))
    }

    @Test
    @Disabled("Legacy: Commit can exist without Branch, Branch is just a reference to a certain commit")
    fun `save 1 commit without branch, expect ConstraintViolationException`() {
        val repository = Repository(localPath = "test-repo", project = project)
        val developer = Developer(name = "Test Committer", email = "committer@test.com", repository = repository)
        val cmt =
            Commit(
                sha = "B".repeat(40),
                authorSignature = Signature(developer = developer, timestamp = LocalDateTime.now()),
                message = null,
                repository = repository,
            )

        val ex =
            assertThrows<ConstraintViolationException> {
                repoPort.create(repository)
            }
    }
}
