package com.inso_world.binocular.cli.service

import com.inso_world.binocular.cli.entity.Commit
import com.inso_world.binocular.cli.entity.Repository
import com.inso_world.binocular.cli.exception.PersistenceException
import com.inso_world.binocular.cli.exception.ServiceException
import com.inso_world.binocular.cli.index.vcs.VcsCommit
import com.inso_world.binocular.cli.persistence.dao.sql.interfaces.ICommitDao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class CommitService(
    @Autowired private val commitDao: ICommitDao,
    @Autowired private val userService: UserService,
//  @Autowired private val commitUserConnectionService: CommitUserConnectionService,
) {
    private val logger: Logger = LoggerFactory.getLogger(CommitService::class.java)

//  @Deprecated
//  fun checkExisting(minedCommits: List<VcsCommit>): Stream<VcsCommit> {
//    val allShas: List<String> = minedCommits.stream()
//      .map { m -> m.sha }
//      .collect(Collectors.toList())
//
//    val existingShas: Set<String> = commitDao.findExistingSha(allShas)
//    val missingShas = (allShas - existingShas).stream().collect(Collectors.toSet());
//
//    return minedCommits.stream().filter { m -> missingShas.contains(m.sha) }
//  }

    fun checkExisting(
        repo: Repository,
        minedCommits: Collection<VcsCommit>,
    ): Pair<Collection<Commit>, Collection<VcsCommit>> {
        val allShas: List<String> =
            minedCommits
                .stream()
                .map { m -> m.sha }
                .collect(Collectors.toList())

        val existingEntities: Set<Commit> = commitDao.findExistingSha(repo, allShas)

        val refIdsToRemove = existingEntities.map { it.sha }.toSet()
        val missingShas = minedCommits.filterNot { it.sha in refIdsToRemove }.stream().collect(Collectors.toSet())

        return Pair(existingEntities.toList(), missingShas.toList())
    }

    fun findAll(
        repo: Repository,
        page: Int = 1,
        perPage: Int = 100,
    ): Iterable<Commit> {
        logger.trace("Loading all commits...")
        val page = if (page < 0) 0 else page
        val perPage = if (perPage < 0) 0 else perPage
        logger.debug("page is $page, perPage is $perPage")
        val pageable: Pageable = PageRequest.of(page - 1, perPage)

        try {
            return commitDao.findAllByRepo(repo, pageable)
        } catch (e: PersistenceException) {
            throw ServiceException(e)
        }
    }

    fun saveAll(commits: Iterable<VcsCommit>): Iterable<Commit> {
        throw NotImplementedError()
//    logger.trace("Saving all commits...")
//
//
// //    val entities = commits.map { it.toEntity() }
// //      .stream().collect(Collectors.toSet())
//    val transformedCommits = this.transformCommits(commits)
//
//    val commits = commitDao.saveAll(transformedCommits)
//
//    return commits
    }

    fun findHeadForBranch(
        repo: Repository,
        branch: String,
    ): Commit? {
        logger.trace("Finding head for branch $branch in repository ${repo.name}...")
        return this.commitDao.findHeadForBranch(repo, branch)
    }
}
