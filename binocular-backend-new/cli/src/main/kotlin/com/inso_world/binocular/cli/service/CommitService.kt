package com.inso_world.binocular.cli.service

import com.inso_world.binocular.cli.exception.ServiceException
import com.inso_world.binocular.cli.index.vcs.VcsCommit
import com.inso_world.binocular.core.exception.BinocularInfrastructureException
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class CommitService(
    @Autowired private val commitPort: CommitInfrastructurePort,
) {
    private val logger: Logger = LoggerFactory.getLogger(CommitService::class.java)

    fun checkExisting(
        repo: Repository,
        minedCommits: Collection<VcsCommit>,
    ): Pair<Collection<Commit>, Collection<VcsCommit>> {
        val allShas: List<String> =
            minedCommits
                .stream()
                .map { m -> m.sha }
                .collect(Collectors.toList())

        val existingEntities: Iterable<Commit> = commitPort.findExistingSha(repo, allShas)

        val refIdsToRemove = existingEntities.map { it.sha }
        val missingShas = minedCommits.filterNot { it.sha in refIdsToRemove } // .stream().collect(Collectors.toSet())

        return Pair(existingEntities.toList(), missingShas.toList())
    }

    fun findAll(
        repo: Repository,
        @Min(0) page: Int = 0,
        @Min(0) @Max(2048) perPage: Int = 100,
    ): Iterable<Commit> {
        logger.trace("Loading all commits...")
        val page = if (page < 0) 0 else page
        val perPage = if (perPage < 0) 0 else perPage
        logger.debug("page is $page, perPage is $perPage")
        val pageable: Pageable = PageRequest.of(page, perPage)

        try {
            return commitPort.findAll(repo, pageable)
        } catch (e: BinocularInfrastructureException) {
            throw ServiceException(e)
        }
    }

    fun findHeadForBranch(
        repo: Repository,
        branch: String,
    ): Commit? {
        logger.trace("Finding head for branch $branch in repository ${repo.name}...")
        return commitPort.findHeadForBranch(repo, branch)
    }
}
