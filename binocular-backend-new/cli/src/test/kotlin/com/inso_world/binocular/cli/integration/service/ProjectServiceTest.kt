package com.inso_world.binocular.cli.integration.service

import com.inso_world.binocular.cli.integration.service.base.BaseServiceTest
import com.inso_world.binocular.cli.service.ProjectService
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.data.ProjectTestDataProvider
import com.inso_world.binocular.github.dto.issue.ItsAssigneeWrapper
import com.inso_world.binocular.github.dto.issue.ItsGitHubIssue
import com.inso_world.binocular.github.dto.issue.ItsUser
import com.inso_world.binocular.model.Account
import org.apache.commons.collections4.CollectionUtils.emptyCollection
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import kotlin.uuid.ExperimentalUuidApi

internal class ProjectServiceTest
    @Autowired
    constructor(
        private val projectService: ProjectService,
        private val projectPort: ProjectInfrastructurePort,
    ) : BaseServiceTest() {
        @Test
        fun `getOrCreateProject creates new project when missing`() {
            val result = projectService.getOrCreateProject("test-project")

            assertAll(
                { assertThat(result).isNotNull },
                { assertThat(result.name).isEqualTo("test-project") },
            )
        }

        @Test
        fun `getOrCreateProject returns existing project`() {
            val created = projectService.getOrCreateProject("existing-project")

            val result = projectService.getOrCreateProject("existing-project")

            assertAll(
                { assertThat(result).isNotNull },
                { assertThat(result.id).isEqualTo(created.id) },
                { assertThat(result.name).isEqualTo("existing-project") },
            )
        }

        @Test
        fun `addIssues does nothing when no new issues`() {
            val project = projectService.getOrCreateProject("issue-test")

            projectService.addIssues(
                issueDtos = emptyList(),
                project = project,
                repo = "repo",
                owner = "owner"
            )

            val reloaded = projectService.findByName("issue-test")

            assertThat(reloaded?.issues).isEmpty()
        }

        @OptIn(ExperimentalUuidApi::class)
        @Test
        fun `transformIssues links accounts and adds issues to project`() {
            val testData = ProjectTestDataProvider()
            val project = testData.projectsByName.getValue("proj-pg-4")

            val issues: List<ItsGitHubIssue> =
                listOf(
                    ItsGitHubIssue(
                        id = "11",
                        number = 11,
                        title = "Issue 11",
                        body = "Test issue 11 description",
                        state = "OPEN",
                        url = "https://github.com/test/repo/issues/1",
                        closedAt = null,
                        createdAt = "2024-01-01T10:00:00Z",
                        updatedAt = "2024-01-01T11:00:00Z",
                        labels = null,
                        milestone = null,
                        author =
                            ItsUser(
                                login = "account1"
                            ),
                        assignees =
                            ItsAssigneeWrapper(
                                nodes =
                                    listOf(
                                        ItsUser(login = "account1"),
                                        ItsUser(login = "account2")
                                    )
                            ),
                        timelineItems = null
                    ),
                    ItsGitHubIssue(
                        id = "12",
                        number = 12,
                        title = "Issue 12",
                        body = "Test issue 12 description",
                        state = "OPEN",
                        url = "https://github.com/test/repo/issues/2",
                        closedAt = null,
                        createdAt = "2024-01-02T10:00:00Z",
                        updatedAt = null,
                        labels = null,
                        milestone = null,
                        author =
                            ItsUser(
                                login = "account2"
                            ),
                        assignees =
                            ItsAssigneeWrapper(
                                nodes =
                                    listOf(
                                        ItsUser(login = "account1"),
                                        ItsUser(login = "account2")
                                    )
                            ),
                        timelineItems = null
                    ),
                    ItsGitHubIssue(
                        id = "13",
                        number = 13,
                        title = "Issue 13",
                        body = null,
                        state = "CLOSED",
                        url = "https://github.com/test/repo/issues/3",
                        closedAt = "2024-01-03T12:00:00Z",
                        createdAt = "2024-01-03T10:00:00Z",
                        updatedAt = "2024-01-03T12:00:00Z",
                        labels = null,
                        milestone = null,
                        author =
                            ItsUser(
                                login = "account3"
                            ),
                        assignees = null,
                        timelineItems = null
                    )
                )

            val accounts =
                Pair(
                    emptyCollection<Account>(),
                    listOf(
                        testData.accountByLogin.getValue("account1"),
                        testData.accountByLogin.getValue("account2"),
                        testData.accountByLogin.getValue("account3")
                    )
                )

            val result = projectService.transformIssues(project, issues, accounts)

            assertAll(
                { assertThat(result).isNotNull },
                { assertThat(project.issues).isNotNull },
                { assertThat(project.issues).hasSize(3) },
                {
                    val issue = project.issues.first { it.gid == "11" }

                    assertThat(issue.title).isEqualTo("Issue 11")
                    assertThat(issue.platformIid).isEqualTo(11)
                    assertThat(issue.author?.login).isEqualTo("account1")

                    assertThat(issue.accounts.map { it.login })
                        .containsExactlyInAnyOrder("account1", "account2")

                    assertThat(issue.project.value).isEqualTo(project.iid.value)
                },
                {
                    val account1 = testData.accountByLogin.getValue("account1")
                    assertThat(account1.issues.map { it.gid })
                        .contains("11", "12")
                }
            )
        }
    }
