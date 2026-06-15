package com.inso_world.binocular.cli.integration.service

import com.inso_world.binocular.cli.integration.service.base.BaseServiceTest
import com.inso_world.binocular.cli.service.ProjectService
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Mention
import com.inso_world.binocular.model.MergeRequest
import com.inso_world.binocular.model.Milestone
import com.inso_world.binocular.model.Note
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

internal class ProjectBigEntitySaveTest
    @Autowired
    constructor(
        private val projectService: ProjectService
    ) : BaseServiceTest() {
        @Test
        fun `save project with full graph including repository and issues`() {
            // 1. Setup: Build the "Big Entity" in memory
            val project = Project(name = "Big Project Save Test")
            project.description = "A project with everything in it"

            // Attach a Repository
            val repo = Repository(localPath = "/path/to/project-repo", project = project)
            project.repo = repo

            val account1 =
                Account(
                    login = "reporter",
                    name = "The Reporter",
                    platform = com.inso_world.binocular.model.Platform.GitHub
                )
            val account2 =
                Account(
                    login = "assignee",
                    name = "The Assignee",
                    platform = com.inso_world.binocular.model.Platform.GitHub
                )

            val milestone1 =
                Milestone(
                    title = "Release 1.0",
                    state = "open",
                    description = "Finalizing version 1.0"
                )

            // Add Issues
            val issue1 =
                Issue().apply {
                    title = "Issue 1"
                    description = "Description for issue 1"
                    createdAt = LocalDateTime.now()
                    accounts = listOf(account1, account2)
                    milestones = listOf(milestone1)
                }

            val note1 =
                Note(
                    body = "Note 1 for issue 1",
                    createdAt = LocalDateTime.now().toString(),
                    updatedAt = LocalDateTime.now().toString(),
                    importedFrom = "test"
                )
            issue1.notes = listOf(note1)

            val mention1 =
                Mention(
                    commit = "abc123mention",
                    createdAt = LocalDateTime.now(),
                    closes = true
                )
            issue1.mentions = listOf(mention1)

            val issue2 =
                Issue().apply {
                    title = "Issue 2"
                }

            val mr1 =
                MergeRequest(
                    title = "Merge Request 1",
                    description = "Implementing feature X",
                    createdAt = LocalDateTime.now().toString(),
                    state = "opened"
                ).apply {
                    notes =
                        listOf(
                            Note(
                                body = "MR Note",
                                createdAt = LocalDateTime.now().toString(),
                                updatedAt = LocalDateTime.now().toString(),
                                importedFrom = "test"
                            )
                        )
                    milestones = listOf(milestone1)
                    accounts = listOf(account1)
                }

            project.issues.addAll(listOf(issue1, issue2))
            // TODO Where MR connection from project ???

            // 2. Execution: Call the single save entry point
            val savedProject = projectService.save(project)

            // 3. Verification
            assertAll(
                { assertThat(savedProject.id).isNotNull() },
                { assertThat(savedProject.repo).isNotNull() },
                { assertThat(savedProject.repo?.id).isNotNull() },
                { assertThat(savedProject.issues).hasSize(2) },
                {
                    val savedIssue1 = savedProject.issues.find { it.title == "Issue 1" }
                    assertThat(savedIssue1).isNotNull
                    assertThat(savedIssue1?.notes).hasSize(1)
                    assertThat(savedIssue1?.mentions).hasSize(1)
                    assertThat(savedIssue1?.accounts).hasSize(2)
                    assertThat(savedIssue1?.milestones).hasSize(1)
                    assertThat(savedIssue1?.milestones?.first()?.title).isEqualTo("Release 1.0")
                }
            )
        }
    }
