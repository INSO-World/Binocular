package com.inso_world.binocular.web.restcontroller

import com.inso_world.binocular.web.BaseDbTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*


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
  fun `should export collections from database`() {
    mockMvc.get("/api/db-export")
    .andExpect {
      status { isOk() }
      content { contentType(MediaType.APPLICATION_JSON) }
      // validation of accounts
      jsonPath("$.accounts")
      jsonPath("$.accounts.length()").value(2)
      // validation of commits
      jsonPath("$.commits")
      jsonPath("$.commits.length()").value(2)
      // validation of branches
      jsonPath("$.branches")
      jsonPath("$.branches.length()").value(2)
      // validation of builds
      jsonPath("$.builds")
      jsonPath("$.builds.length()").value(2)
      // validation of files
      jsonPath("$.files")
      jsonPath("$.files.length()").value(2)
      // validation of issues
      jsonPath("$.issues")
      jsonPath("$.issues.length()").value(2)
      // validation of merge requests
      jsonPath("$.merge_requests")
      jsonPath("$.merge_requests.length()").value(2)
      // validation of modules
      jsonPath("$.modules")
      jsonPath("$.modules.length()").value(2)
      // validation of notes
      jsonPath("$.notes")
      jsonPath("$.notes.length()").value(2)
      // validation of users
      jsonPath("$.users")
      jsonPath("$.users.length()").value(2)
      // validation of milestones
      jsonPath("$.milestones")
      jsonPath("$.milestones.length()").value(2)

      // Todo validation of connections
    }


  }


}
