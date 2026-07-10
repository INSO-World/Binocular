package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.data.MockTestDataProvider
import com.inso_world.binocular.core.integration.base.TestDataProvider
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import kotlin.test.assertFalse

internal class PaginationTest : BaseInfrastructureSpringTest() {
    @Autowired
    lateinit var projectPort: ProjectInfrastructurePort

    @Autowired
    lateinit var repositoryPort: RepositoryInfrastructurePort

    @BeforeEach
    fun setup() {
        infrastructureDataSetup.teardown()
        infrastructureDataSetup.setup()
    }

    @Test
    fun `project pagination returns correct metadata`() {
        val page0: Page<Project> = projectPort.findAll(PageRequest.of(0, 2))
        assertAll(
            { assertEquals(2, page0.content.size, "content.size") },
            { assertEquals(7, page0.totalElements, "totalElements") },
            { assertEquals(4, page0.totalPages, "totalPages") },
            { assertEquals(2, page0.size, "size") },
            { assertEquals(0, page0.number, "number") },
            { assertTrue(page0.first) },
            { assertFalse(page0.last) },
        )

        val page3: Page<Project> = projectPort.findAll(PageRequest.of(3, 2))
        // last page has 1 element (7 total)
        assertAll(
            { assertEquals(1, page3.content.size, "content.size") },
            { assertEquals(7, page3.totalElements, "totalElements") },
            { assertEquals(4, page3.totalPages, "totalPages") },
            { assertEquals(2, page3.size, "size") },
            { assertEquals(3, page3.number, "number") },
            { assertFalse(page3.first) },
            { assertTrue(page3.last) },
        )
    }

    @Test
    fun `repository pagination returns correct metadata`() {
        val page0: Page<Repository> = repositoryPort.findAll(PageRequest.of(0, 3))
        assertAll(
            { assertEquals(3, page0.content.size, "content.size") },
            { assertEquals(7, page0.totalElements, "totalElements") },
            { assertEquals(3, page0.totalPages, "totalPages") },
            { assertEquals(3, page0.size, "size") },
            { assertEquals(0, page0.number, "number") },
            { assertTrue(page0.first) },
            { assertFalse(page0.last) },
        )

        val page3: Page<Repository> = repositoryPort.findAll(PageRequest.of(2, 3))
        assertAll(
            { assertEquals(1, page3.content.size, "content.size") },
            { assertEquals(7, page3.totalElements, "totalElements") },
            { assertEquals(3, page3.totalPages, "totalPages") },
            { assertEquals(3, page3.size, "size") },
            { assertEquals(2, page3.number, "number") },
            { assertFalse(page3.first) },
            { assertTrue(page3.last) },
        )

        // sanity: ensure provider data present
        val expectedRepoIds = TestDataProvider.testRepositories.map { it.localPath }.toSet()
        val actualRepoIds = repositoryPort.findAll().map { it.localPath }.toSet()
        assertThat(actualRepoIds).containsAll(expectedRepoIds)
    }
}
