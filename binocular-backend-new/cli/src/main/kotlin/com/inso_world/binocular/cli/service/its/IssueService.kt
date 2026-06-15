package com.inso_world.binocular.cli.service.its

import com.inso_world.binocular.core.service.IssueInfrastructurePort
import com.inso_world.binocular.github.dto.issue.ItsGitHubIssue
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Project
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class IssueService(
    @Autowired private val issuePort: IssueInfrastructurePort,
) {
    fun getOrCreate(e: Issue): Issue =
        e.id?.let { id ->
            this.issuePort.findById(id)
        } ?: this.issuePort.create(e)

    fun checkExisting(
        loadedIssues: Collection<ItsGitHubIssue>,
        project: Project
    ): Pair<Collection<Issue>, Collection<ItsGitHubIssue>> {
        val allIds: List<String> =
            loadedIssues
                .stream()
                .map { it.id }
                .collect(Collectors.toList())

        val existingEntities: Iterable<Issue> = issuePort.findExistingGid(allIds, project)

        val refIdsToRemove = existingEntities.map { it.id }
        val missingIds = loadedIssues.filterNot { refIdsToRemove.contains(it.id) }

        return Pair(existingEntities.toList(), missingIds.toList())
    }
}
