package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.data.MockTestDataProvider
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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

    @Test
    fun `project pagination returns correct metadata`() {
        val total = projectPort.findAll().count()
        val pageSize = 2
        val totalPages = (total + pageSize - 1) / pageSize
        val lastPage = totalPages - 1
        val lastPageSize = total - lastPage * pageSize

        val page0: Page<Project> = projectPort.findAll(PageRequest.of(0, pageSize))
        assertAll(
            { assertEquals(pageSize, page0.content.size, "content.size") },
            { assertEquals(total.toLong(), page0.totalElements, "totalElements") },
            { assertEquals(totalPages, page0.totalPages, "totalPages") },
            { assertEquals(pageSize, page0.size, "size") },
            { assertEquals(0, page0.number, "number") },
            { assertTrue(page0.first) },
            { assertFalse(page0.last) },
        )

        val last: Page<Project> = projectPort.findAll(PageRequest.of(lastPage, pageSize))
        assertAll(
            { assertEquals(lastPageSize, last.content.size, "content.size") },
            { assertEquals(total.toLong(), last.totalElements, "totalElements") },
            { assertEquals(totalPages, last.totalPages, "totalPages") },
            { assertEquals(pageSize, last.size, "size") },
            { assertEquals(lastPage, last.number, "number") },
            { assertFalse(last.first) },
            { assertTrue(last.last) },
        )
    }

    @Test
    fun `repository pagination returns correct metadata`() {
        val total = repositoryPort.findAll().count()
        val pageSize = 3
        val totalPages = (total + pageSize - 1) / pageSize
        val lastPage = totalPages - 1
        val lastPageSize = total - lastPage * pageSize

        val page0: Page<Repository> = repositoryPort.findAll(PageRequest.of(0, pageSize))
        assertAll(
            { assertEquals(pageSize, page0.content.size, "content.size") },
            { assertEquals(total.toLong(), page0.totalElements, "totalElements") },
            { assertEquals(totalPages, page0.totalPages, "totalPages") },
            { assertEquals(pageSize, page0.size, "size") },
            { assertEquals(0, page0.number, "number") },
            { assertTrue(page0.first) },
            { assertFalse(page0.last) },
        )

        val last: Page<Repository> = repositoryPort.findAll(PageRequest.of(lastPage, pageSize))
        assertAll(
            { assertEquals(lastPageSize, last.content.size, "content.size") },
            { assertEquals(total.toLong(), last.totalElements, "totalElements") },
            { assertEquals(totalPages, last.totalPages, "totalPages") },
            { assertEquals(pageSize, last.size, "size") },
            { assertEquals(lastPage, last.number, "number") },
            { assertFalse(last.first) },
            { assertTrue(last.last) },
        )

        // sanity: ensure provider data present
        val expectedRepoIds = MockTestDataProvider().testRepositories.map { it.localPath }.toSet()
        val actualRepoIds = repositoryPort.findAll().map { it.localPath }.toSet()
        assertTrue(actualRepoIds.containsAll(expectedRepoIds))
    }
}
