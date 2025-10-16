package com.inso_world.binocular.infrastructure.test.repository.base

import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Repository
import org.junit.jupiter.api.BeforeEach

internal class BasePortOctoDataTest : BasePortWithDataTest() {

    protected lateinit var octoRepo: Repository

    @BeforeEach
    fun setupBase() {
        this.octoRepo = requireNotNull(
            prepare(
                "${FIXTURES_PATH}/${OCTO_REPO}",
                projectName = OCTO_PROJECT_NAME,
                branch = Branch(name = "master")
            ).repo
        ) {
            "${FIXTURES_PATH}/${OCTO_REPO} repository cannot be null"
        }
    }

}
