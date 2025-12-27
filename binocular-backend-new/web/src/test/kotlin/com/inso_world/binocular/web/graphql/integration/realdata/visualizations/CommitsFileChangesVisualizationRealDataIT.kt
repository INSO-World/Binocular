package com.inso_world.binocular.web.graphql.integration.realdata.visualizations

import com.inso_world.binocular.web.graphql.integration.realdata.base.BaseGraphQlCompatibilityIT
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import com.inso_world.binocular.web.graphql.integration.realdata.assertions.DateAssertions.assertIsoInstantEquals

class CommitsFileChangesVisualizationRealDataIT : BaseGraphQlCompatibilityIT() {

    @Test
    fun `file commits for README md returns expected snapshot for first item`() {
        val query = """
            query (
              ${'$'}page: Int,
              ${'$'}perPage: Int,
              ${'$'}file: String!
            ) {
              file(path: ${'$'}file) {
                commits(page: ${'$'}page, perPage: ${'$'}perPage) {
                  data {
                    commit {
                      sha
                      shortSha
                      message
                      messageHeader
                      user { id gitSignature __typename }
                      branch
                      parents
                      date
                      webUrl
                      stats { additions deletions __typename }
                      files(page: 1, perPage: 1000) {
                        data {
                          file { path __typename }
                          hunks { newStart newLines oldStart oldLines __typename }
                          __typename
                        }
                        __typename
                      }
                      __typename
                    }
                    __typename
                  }
                }
              }
            }
        """.trimIndent()

        val variables = mapOf(
            "page" to 1,
            "perPage" to 1000,
            "file" to "README.md",
        )

        val root = client.execute(query, variables)
        val file = root.get("file")
        val commits = file.get("commits")
        val dataArr = commits.get("data")
        assertTrue(dataArr.size() > 0, "file.commits.data should contain at least one item")

        val firstItem = dataArr.get(0)
        assertEquals("CommitInFile", firstItem.get("__typename").asText(), "file.commits.data[0].__typename")

        val commit = firstItem.get("commit")
        assertEquals("61903bc02d5c48923c1274800dfee13168d17f93", commit.get("sha").asText(), "commit.sha")
        assertEquals("61903bc", commit.get("shortSha").asText(), "commit.shortSha")
        assertEquals(":memo: Add documentation\n\nIssue: #65 - Add some documentation\n", commit.get("message").asText(), "commit.message")
        assertEquals(":memo: Add documentation", commit.get("messageHeader").asText(), "commit.messageHeader")
        assertEquals("main", commit.get("branch").asText(), "commit.branch")
        assertIsoInstantEquals(commit.get("date").asText(), "2018-03-27T08:18:46.000Z", "commit.date")
        assertEquals("https://github.com/INSO-World/Binocular/commit/61903bc02d5c48923c1274800dfee13168d17f93", commit.get("webUrl").asText(), "commit.webUrl")
        assertEquals("Commit", commit.get("__typename").asText(), "commit.__typename")

        val parents = commit.get("parents")
        assertEquals(1, parents.size(), "commit.parents size")
        assertEquals("24136db1695de8a0bd4a55066359a336ef1c2b7f", parents.get(0).asText(), "commit.parents[0]")

        val user = commit.get("user")
        assertEquals("6599256", user.get("id").asText(), "commit.user.id")
        assertEquals("Roman Decker <roman.decker@gmail.com>", user.get("gitSignature").asText(), "commit.user.gitSignature")
        assertEquals("User", user.get("__typename").asText(), "commit.user.__typename")

        val stats = commit.get("stats")
        assertEquals(1285, stats.get("additions").asInt(), "commit.stats.additions")
        assertEquals(0, stats.get("deletions").asInt(), "commit.stats.deletions")
        assertEquals("Stats", stats.get("__typename").asText(), "commit.stats.__typename")

        val filesContainer = commit.get("files")
        assertEquals("PaginatedFileInCommit", filesContainer.get("__typename").asText(), "commit.files.__typename")
        val filesData = filesContainer.get("data")
        assertTrue(filesData.size() >= 4, "commit.files.data should have at least 4 entries as per snapshot")

        run {
            val item = filesData.get(0)
            assertEquals("FileInCommit", item.get("__typename").asText(), "files.data[0].__typename")
            val ff = item.get("file")
            assertEquals("docs/zivsed-architecture.png", ff.get("path").asText(), "files.data[0].file.path")
            assertEquals("File", ff.get("__typename").asText(), "files.data[0].file.__typename")
            val hunks = item.get("hunks")
            assertEquals(1, hunks.size(), "files.data[0].hunks size")
            val h0 = hunks.get(0)
            assertEquals(1, h0.get("newStart").asInt(), "files.data[0].hunks[0].newStart")
            assertEquals(655, h0.get("newLines").asInt(), "files.data[0].hunks[0].newLines")
            assertEquals(0, h0.get("oldStart").asInt(), "files.data[0].hunks[0].oldStart")
            assertEquals(0, h0.get("oldLines").asInt(), "files.data[0].hunks[0].oldLines")
            assertEquals("Hunk", h0.get("__typename").asText(), "files.data[0].hunks[0].__typename")
        }

        run {
            val item = filesData.get(1)
            assertEquals("FileInCommit", item.get("__typename").asText(), "files.data[1].__typename")
            val ff = item.get("file")
            assertEquals("docs/cors.png", ff.get("path").asText(), "files.data[1].file.path")
            assertEquals("File", ff.get("__typename").asText(), "files.data[1].file.__typename")
            val hunks = item.get("hunks")
            assertEquals(1, hunks.size(), "files.data[1].hunks size")
            val h0 = hunks.get(0)
            assertEquals(1, h0.get("newStart").asInt(), "files.data[1].hunks[0].newStart")
            assertEquals(400, h0.get("newLines").asInt(), "files.data[1].hunks[0].newLines")
            assertEquals(0, h0.get("oldStart").asInt(), "files.data[1].hunks[0].oldStart")
            assertEquals(0, h0.get("oldLines").asInt(), "files.data[1].hunks[0].oldLines")
            assertEquals("Hunk", h0.get("__typename").asText(), "files.data[1].hunks[0].__typename")
        }

        run {
            val item = filesData.get(2)
            assertEquals("FileInCommit", item.get("__typename").asText(), "files.data[2].__typename")
            val ff = item.get("file")
            assertEquals("docs/CONTRIBUTING.md", ff.get("path").asText(), "files.data[2].file.path")
            assertEquals("File", ff.get("__typename").asText(), "files.data[2].file.__typename")
            val hunks = item.get("hunks")
            assertEquals(1, hunks.size(), "files.data[2].hunks size")
            val h0 = hunks.get(0)
            assertEquals(1, h0.get("newStart").asInt(), "files.data[2].hunks[0].newStart")
            assertEquals(124, h0.get("newLines").asInt(), "files.data[2].hunks[0].newLines")
            assertEquals(0, h0.get("oldStart").asInt(), "files.data[2].hunks[0].oldStart")
            assertEquals(0, h0.get("oldLines").asInt(), "files.data[2].hunks[0].oldLines")
            assertEquals("Hunk", h0.get("__typename").asText(), "files.data[2].hunks[0].__typename")
        }

        run {
            val item = filesData.get(3)
            assertEquals("FileInCommit", item.get("__typename").asText(), "files.data[3].__typename")
            val ff = item.get("file")
            assertEquals("README.md", ff.get("path").asText(), "files.data[3].file.path")
            assertEquals("File", ff.get("__typename").asText(), "files.data[3].file.__typename")
            val hunks = item.get("hunks")
            assertEquals(1, hunks.size(), "files.data[3].hunks size")
            val h0 = hunks.get(0)
            assertEquals(1, h0.get("newStart").asInt(), "files.data[3].hunks[0].newStart")
            assertEquals(106, h0.get("newLines").asInt(), "files.data[3].hunks[0].newLines")
            assertEquals(0, h0.get("oldStart").asInt(), "files.data[3].hunks[0].oldStart")
            assertEquals(0, h0.get("oldLines").asInt(), "files.data[3].hunks[0].oldLines")
            assertEquals("Hunk", h0.get("__typename").asText(), "files.data[3].hunks[0].__typename")
        }
    }
}
