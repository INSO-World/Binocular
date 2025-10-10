package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.integration.base.TestDataProvider
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest

class PaginationTest : BaseInfrastructureSpringTest() {

    @Autowired
    lateinit var projectPort: ProjectInfrastructurePort

    @Autowired
    lateinit var repositoryPort: RepositoryInfrastructurePort

    @Test
    fun `project pagination returns correct metadata`() {
        val page0: Page<Project> = projectPort.findAll(PageRequest.of(0, 2))
        assertEquals(2, page0.content.size)
        assertEquals(5, page0.totalElements)
        assertEquals(3, page0.totalPages)
        assertEquals(2, page0.size)
        assertEquals(0, page0.number)
        assertTrue(page0.first)
        assertTrue(!page0.last)

        val page2: Page<Project> = projectPort.findAll(PageRequest.of(2, 2))
        // last page has 1 element (5 total)
        assertEquals(1, page2.content.size)
        assertEquals(5, page2.totalElements)
        assertEquals(3, page2.totalPages)
        assertEquals(2, page2.size)
        assertEquals(2, page2.number)
        assertTrue(!page2.first)
        assertTrue(page2.last)
    }

    @Test
    fun `repository pagination returns correct metadata`() {
        val page0: Page<Repository> = repositoryPort.findAll(PageRequest.of(0, 3))
        assertEquals(3, page0.content.size)
        assertEquals(7, page0.totalElements)
        assertEquals(3, page0.totalPages)
        assertEquals(3, page0.size)
        assertEquals(0, page0.number)
        assertTrue(page0.first)
        assertTrue(!page0.last)

        val page2: Page<Repository> = repositoryPort.findAll(PageRequest.of(2, 3))
        assertEquals(1, page2.content.size)
        assertEquals(7, page2.totalElements)
        assertEquals(3, page2.totalPages)
        assertEquals(3, page2.size)
        assertEquals(2, page2.number)
        assertTrue(!page2.first)
        assertTrue(page2.last)

        // sanity: ensure provider data present
        val expectedRepoIds = TestDataProvider.testRepositories.mapNotNull { it.id }.toSet()
        val actualRepoIds = repositoryPort.findAll().mapNotNull { it.id }.toSet()
        assertTrue(actualRepoIds.containsAll(expectedRepoIds))
    }
}
