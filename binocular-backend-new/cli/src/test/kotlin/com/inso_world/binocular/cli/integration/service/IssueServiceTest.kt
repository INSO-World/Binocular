package com.inso_world.binocular.cli.integration.service

import com.inso_world.binocular.cli.integration.service.base.BaseServiceTest
import com.inso_world.binocular.cli.service.ProjectService
import com.inso_world.binocular.cli.service.its.IssueService
import com.inso_world.binocular.github.dto.issue.ItsAssigneeWrapper
import com.inso_world.binocular.github.dto.issue.ItsGitHubIssue
import com.inso_world.binocular.github.dto.issue.ItsUser
import com.inso_world.binocular.github.dto.user.ItsGitHubUser
import com.inso_world.binocular.github.service.GitHubService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import reactor.core.publisher.Mono

internal class IssueServiceTest
    @Autowired
    constructor(
        private val issueService: IssueService,
        private val projectService: ProjectService,
    ) : BaseServiceTest() {
        @MockkBean
        lateinit var gitHubService: GitHubService

        @Test
        fun `checkExisting with empty list returns empty`() {
            val project = projectService.getOrCreateProject("test-project-1")

            val result = issueService.checkExisting(emptyList(), project)

            assertAll(
                { assertThat(result.first).isEmpty() },
                { assertThat(result.second).isEmpty() },
            )
        }

        @Test
        fun `checkExisting when all exist returns no missing`() {
            val project = projectService.getOrCreateProject("test-project-2")

            val issues: List<ItsGitHubIssue> =
                listOf(
                    ItsGitHubIssue(
                        id = "1",
                        number = 11,
                        title = "Issue 1",
                        body = "Test issue 1 description",
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
                        id = "2",
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
                    )
                )

            every {
                gitHubService.loadAllAssignableUsers("test-owner", "test-repo")
            } returns
                Mono.just(
                    listOf(
                        ItsGitHubUser(
                            id = "1",
                            login = "account1",
                            email = "account1@email.com",
                            name = "",
                            url = "",
                            avatarUrl = ""
                        ),
                        ItsGitHubUser(
                            id = "2",
                            login = "account2",
                            email = "account2@email.com",
                            name = "",
                            url = "",
                            avatarUrl = ""
                        ),
                    )
                )

            projectService.addIssues(issues, project, "test-repo", "test-owner")

            val result = issueService.checkExisting(issues, project)
            print(result)

            assertAll(
                { assertThat(result.first).hasSize(2) },
                { assertThat(result.second).isEmpty() },
            )
        }
    }
