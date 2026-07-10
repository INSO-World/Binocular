package com.inso_world.binocular.rdf.service

import com.inso_world.binocular.rdf.arango.ArangoReader
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertTrue

class KnowledgeGraphBuilderTest {
    @Test
    fun `builds knowledge graph and writes turtle file`() {
        val tempFile = File.createTempFile("test-kg", ".ttl")
        try {
            val reader =
                mockk<ArangoReader> {
                    every { readCommits() } returns
                        listOf(
                            mapOf(
                                "_key" to "796",
                                "sha" to "945935697466022f2ddf7b1ea4f8cf9587b12a18",
                                "date" to "2016-11-16T13:22:07.000Z",
                                "message" to "Initial commit"
                            )
                        )
                    every { readUsers() } returns emptyList()
                    every { readFiles() } returns emptyList()
                    every { readModules() } returns emptyList()
                    every { readIssues() } returns emptyList()
                    every { readMergeRequests() } returns emptyList()
                    every { readBuilds() } returns emptyList()
                    every { readBranches() } returns emptyList()
                    every { readCommitsUsers() } returns emptyList()
                    every { readCommitsFiles() } returns emptyList()
                    every { readIssuesCommits() } returns emptyList()
                    every { readCommitsModules() } returns emptyList()
                    every { readCommitsBuilds() } returns emptyList()
                    every { readModulesFiles() } returns emptyList()
                    every { readModulesModules() } returns emptyList()
                }

            val builder = KnowledgeGraphBuilder(reader, tempFile.absolutePath)
            builder.buildAndExport()

            assertTrue(tempFile.exists(), "Turtle file should be created")
            assertTrue(tempFile.length() > 0, "Turtle file should have content")

            val content = tempFile.readText()
            assertTrue(content.contains("Commit"), "Should contain Commit type")
            assertTrue(content.contains("945935697466022f2ddf7b1ea4f8cf9587b12a18"), "Should contain commit SHA")
            assertTrue(content.contains("Initial commit"), "Should contain commit message")
        } finally {
            tempFile.delete()
        }
    }
}
