package com.inso_world.binocular.cli.service

import com.inso_world.binocular.cli.exception.CliException
import com.inso_world.binocular.cli.exception.ServiceException
import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.index.GitIndexer
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.io.path.Path

//
@Service
class VcsService(
    @Autowired private val repoService: RepositoryService,
) {
    companion object {
        private val logger by logger()
    }

    @Autowired
    private lateinit var gitIndexer: GitIndexer

    fun indexRepository(
//        TODO make this a real Path
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
                    val repo = this.gitIndexer.findRepo(Path(repoPath), project)
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

        val vcsCommits = this.gitIndexer.traverseBranch(vcsRepo, branch)

        val shas = vcsCommits.second.map { it.sha }
        val parentShas = vcsCommits.second.flatMap { it.parents }

        logger.debug(
            "Existing commits: ${shas.count()}+${parentShas.count()}=${
                (shas + parentShas).distinct().count()
            } commit(s) found on branch $branch",
        )
        this.repoService.addCommits(vcsRepo, vcsCommits.second)
        logger.trace("<<< indexRepository({}, {}, {})", repoPath, branch, project)
    }
}
