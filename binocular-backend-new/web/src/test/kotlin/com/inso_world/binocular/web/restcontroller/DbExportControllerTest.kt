package com.inso_world.binocular.web.restcontroller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.inso_world.binocular.web.BaseDbTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import kotlin.test.assertEquals

/**
 * Test class for DbExportController.
 *
 */
@SpringBootTest
@AutoConfigureMockMvc
class DbExportControllerTest : BaseDbTest() {

  @Autowired
  lateinit var mockMvc: MockMvc

  @Test
  fun `should export all collections from database`() {
    val mvcResult = mockMvc.get("/api/db-export")
      .andExpect {
        status { isOk() }
        content { contentType(MediaType.APPLICATION_JSON) }
      }
      .andReturn()

    // parse the JSON
    val jsonString = mvcResult.response.contentAsString
    val jsonNode: JsonNode = jacksonObjectMapper().readTree(jsonString)

    assertAll(
      {
        val accounts = jsonNode["accounts"]
        assertEquals(testAccounts.size, accounts?.size() ?: 0, "accounts size mismatch")
      },
      {
        val commits = jsonNode["commits"]
        assertEquals(testCommits.size, commits?.size() ?: 0, "commits size mismatch")
      },
      {
        val branches = jsonNode["branches"]
        assertEquals(testBranches.size, branches?.size() ?: 0, "branches size mismatch")
      },
      {
        val builds = jsonNode["builds"]
        assertEquals(testBuilds.size, builds?.size() ?: 0, "builds size mismatch")
      },
      {
        val files = jsonNode["files"]
        assertEquals(testFiles.size, files?.size() ?: 0, "files size mismatch")
      },
      {
        val issues = jsonNode["issues"]
        assertEquals(testIssues.size, issues?.size() ?: 0, "issues size mismatch")
      },
      {
        val mergeRequests = jsonNode["mergeRequests"]
          ?: jsonNode["merge_requests"]
        assertEquals(testMergeRequests.size, mergeRequests?.size() ?: 0, "merge_requests size mismatch")
      },
      {
        val modules = jsonNode["modules"]
        assertEquals(testModules.size, modules?.size() ?: 0, "modules size mismatch")
      },
      {
        val notes = jsonNode["notes"]
        assertEquals(testNotes.size, notes?.size() ?: 0, "notes size mismatch")
      },
      {
        val users = jsonNode["users"]
        assertEquals(testUsers.size, users?.size() ?: 0, "users size mismatch")
      },
      {
        val milestones = jsonNode["milestones"]
        assertEquals(testMilestones.size, milestones?.size() ?: 0, "milestones size mismatch")
      }
    )
  }


}
