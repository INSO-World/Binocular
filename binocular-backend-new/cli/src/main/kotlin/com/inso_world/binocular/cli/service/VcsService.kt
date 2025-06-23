package com.inso_world.binocular.cli.service

import com.inso_world.binocular.cli.exception.CliException
import com.inso_world.binocular.cli.exception.ServiceException
import com.inso_world.binocular.cli.index.vcs.toDto
import com.inso_world.binocular.ffi.pojos.BinocularCommitPojo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class VcsService(
    @Autowired private val repoService: RepositoryService,
) {
    private var logger: Logger = LoggerFactory.getLogger(VcsService::class.java)

    @Autowired
    private lateinit var ffiService: FfiService

    fun indexRepository(
        repoPath: String?,
        branch: String,
    ) {
        val vcsRepo =
            try {
                this.ffiService.findRepo(repoPath)
            } catch (e: ServiceException) {
                throw CliException(e)
            }
        logger.info("Found repository: ${vcsRepo.gitDir}")

//    val repo: Repository = transactionTemplate.execute { repoService.getOrCreate(vcsRepo.gitDir) } as Repository
//    logger.info("Searching commits for branch '$branchName'")
//
//    val branchEntity = this.repoService.findBranch(repo, branchName)
//    val branchName = branchEntity?.name ?: branchName
//
        val vcsCommits = this.ffiService.traverseAllOnBranch(vcsRepo, branch).map(BinocularCommitPojo::toDto)
        logger.debug("Existing commits: ${vcsCommits.count()} commit(s) found on branch $branch")
//
// //    val commitEntities = this.repoService.transformCommits(repo, vcsCommits)
//    transactionTemplate.execute {
        this.repoService.addCommits(vcsRepo, vcsCommits, branch)
        logger.debug("Commits added to database.")
//    }
    }
}
