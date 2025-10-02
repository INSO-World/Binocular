package com.inso_world.binocular.cli.service

import com.inso_world.binocular.cli.exception.CliException
import com.inso_world.binocular.cli.exception.ServiceException
import com.inso_world.binocular.core.index.GitIndexer
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.CommitDiff
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

//
@Service
class VcsService(
    @Autowired private val repoService: RepositoryService,
) {
    companion object {
        private var logger: Logger = LoggerFactory.getLogger(VcsService::class.java)
    }

    @Autowired
    private lateinit var gitIndexer: GitIndexer

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
                this.repoService.findRepo(
                    requireNotNull(repoPath) {
                        "Repository path is empty/null"
                    },
                ) ?: run {
                    logger.trace("Repository $repoPath not indexed, looking for .git path")
                    val repo = this.ffiService.findRepo(repoPath)
                    project.repo = repo
                    repo.project = project
                    this.repoService.create(repo)
                }
            } catch (e: ServiceException) {
                throw CliException(e)
            }
        logger.info("Found repository: ${vcsRepo.localPath}")
        val branch =
            requireNotNull(
                this.gitIndexer.findAllBranches(vcsRepo).find { it.name == branch },
            ) {
                "Branch not found: $branch"
            }

        val vcsCommits = this.ffiService.traverseAllOnBranch(vcsRepo, branch)

        val shas = vcsCommits.map { it.sha }
        val parentShas = vcsCommits.flatMap { it.parents }

        logger.debug(
            "Existing commits: ${shas.count()}+${parentShas.count()}=${
                (shas + parentShas).distinct().count()
            } commit(s) found on branch $branch",
        )
        this.repoService.addCommits(vcsRepo, vcsCommits)
        logger.trace("<<< indexRepository({}, {}, {})", repoPath, branch, project)
    }
}
