package com.inso_world.binocular.cli.service

import com.inso_world.binocular.cli.config.BinocularCliConfiguration
import com.inso_world.binocular.cli.exception.ServiceException
import com.inso_world.binocular.core.exception.BinocularIndexerException
import com.inso_world.binocular.core.index.GitIndexer
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.io.path.Path
import kotlin.io.path.exists

@Service
@Deprecated("will be removed")
internal class FfiService(
    @Autowired val binocularCliConfiguration: BinocularCliConfiguration,
) {
    private var logger: Logger = LoggerFactory.getLogger(FfiService::class.java)

    @Autowired
    private lateinit var gitIndexer: GitIndexer

    fun findAllBranches(repo: Repository): List<Branch> =
        try {
            gitIndexer
                .findAllBranches(repo)
                .parallelStream()
                .toList()
        } catch (e: BinocularIndexerException) {
            throw ServiceException(e)
        }

    fun findRepo(path: String?): Repository {
        val path =
            run {
                path ?: this.binocularCliConfiguration.index.path
            }.let { indexPath ->
                val path = Path(indexPath).toRealPath()
                require(path.exists()) {
                    "Path $path does not exist"
                }
                path
            }
        logger.trace("Searching repository... at '{}'", path.toRealPath())
        return try {
            gitIndexer.findRepo(path)
        } catch (e: BinocularIndexerException) {
            throw ServiceException(e)
        }
    }

    fun traverseAllOnBranch(
        repo: Repository,
        branch: Branch,
    ): List<Commit> =
        try {
            gitIndexer.traverseBranch(repo, branch)
        } catch (e: BinocularIndexerException) {
            throw ServiceException(e)
        }
}
