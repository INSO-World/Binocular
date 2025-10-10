package com.inso_world.binocular.ffi

import com.inso_world.binocular.core.index.GitIndexer
import com.inso_world.binocular.ffi.exception.FfiException
import com.inso_world.binocular.ffi.extensions.toDomain
import com.inso_world.binocular.ffi.extensions.toModel
import com.inso_world.binocular.ffi.internal.AnyhowException
import com.inso_world.binocular.ffi.internal.BinocularDiffInput
import com.inso_world.binocular.ffi.internal.GixDiffAlgorithm
import com.inso_world.binocular.ffi.pojos.toDomain
import com.inso_world.binocular.ffi.pojos.toFfi
import com.inso_world.binocular.ffi.pojos.toModel
import com.inso_world.binocular.ffi.util.Utils
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.CommitDiff
import com.inso_world.binocular.model.Repository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.file.Path
import kotlin.math.min
import kotlin.streams.asSequence

@Service
class BinocularFfi : GitIndexer {
    companion object {
        private var logger: Logger = LoggerFactory.getLogger(BinocularFfi::class.java)
        private val ALGORITHM = GixDiffAlgorithm.HISTOGRAM
    }

    init {
        logger.info("Loading native library...")
        val rp = Utils.loadPlatformLibrary("gix_binocular")
        logger.debug("Loaded library: $rp")
        logger.info("Library loaded successfully.")
    }

    fun hello() {
        com.inso_world.binocular.ffi.internal
            .hello()
    }

    @Throws(FfiException::class)
    override fun findRepo(path: Path): Repository {
        logger.trace("Searching repository... at '{}'", path)
        try {
            val repo =
                com.inso_world.binocular.ffi.internal
                    .findRepo(path.toString().trim())
                    .toModel()
            return repo
        } catch (e: AnyhowException) {
            throw FfiException(e)
        }
    }

    override fun traverseBranch(
        repo: Repository,
        branch: Branch,
    ): List<Commit> {
        val commitVec =
            com.inso_world.binocular.ffi.internal
                .traverseBranch(repo.toFfi(), branch.name)

        val commits = commitVec.toDomain(repo)
        branch.commits.addAll(commits)

        repo.branches.add(branch)
        branch.repository = repo

        return commits
    }

    override fun findAllBranches(repo: Repository): List<Branch> =
        com.inso_world.binocular.ffi.internal
            .findAllBranches(repo.toFfi())
            .map {
                val branch = it.toModel()
                branch
            }

    override fun findCommit(
        repo: Repository,
        hash: String,
    ): String =
        com.inso_world.binocular.ffi.internal
            .findCommit(repo.toFfi(), hash)

    override fun traverse(
        repo: Repository,
        sourceCmt: String,
        trgtCmt: String?,
    ): List<Commit> =
        com.inso_world.binocular.ffi.internal
            .traverse(repo.toFfi(), sourceCmt, trgtCmt)
            .toDomain(repo)
}
