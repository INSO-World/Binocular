package com.inso_world.binocular.cli.service

import com.inso_world.binocular.cli.exception.CliException
import com.inso_world.binocular.cli.exception.ServiceException
import com.inso_world.binocular.cli.index.vcs.toDtos
import com.inso_world.binocular.model.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

//
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
        project: Project,
    ) {
        logger.trace(">>> indexRepository({}, {}, {})", repoPath, branch, project)
        val vcsRepo =
            try {
                this.ffiService.findRepo(repoPath)
            } catch (e: ServiceException) {
                throw CliException(e)
            }
        logger.info("Found repository: ${vcsRepo.gitDir}")

        val vcsCommits = this.ffiService.traverseAllOnBranch(vcsRepo, branch).toDtos()

        val shas = vcsCommits.map { it.sha }
        val parentShas = vcsCommits.flatMap { it.parents }

        logger.debug(
            "Existing commits: ${shas.count()}+${ parentShas.count() }=${(shas + parentShas).distinct().count()} commit(s) found on branch $branch",
        )
        this.repoService.addCommits(vcsRepo, vcsCommits, project)
        logger.trace("<<< indexRepository({}, {}, {})", repoPath, branch, project)
    }
}
