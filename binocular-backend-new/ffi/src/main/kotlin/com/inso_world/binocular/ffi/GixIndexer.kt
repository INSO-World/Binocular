package com.inso_world.binocular.ffi

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.index.GitIndexer
import com.inso_world.binocular.ffi.extensions.toDomain
import com.inso_world.binocular.ffi.internal.GixDiffAlgorithm
import com.inso_world.binocular.ffi.internal.GixDiffInput
import com.inso_world.binocular.ffi.pojos.toFfi
import com.inso_world.binocular.ffi.pojos.toModel
import com.inso_world.binocular.ffi.util.Utils
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.CommitDiff
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Stats
import com.inso_world.binocular.model.vcs.BlameEntry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.file.Path
import kotlin.math.min
import kotlin.streams.asSequence

@Service
class GixIndexer : GitIndexer {

    @Autowired
    private lateinit var cfg: FfiConfig

    companion object Companion {
        private val logger by logger()
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

    override fun findRepo(path: Path, project: Project): Repository {
        logger.trace("Searching repository... at '{}'", path)
        val repo =
            com.inso_world.binocular.ffi.internal
                .findRepo(path.toString().trim())
                .toModel(project)
        return repo
    }

    override fun traverseBranch(
        repo: Repository,
        branchName: String,
    ): Pair<Branch, List<Commit>> {
        logger.trace("Traversing $branchName with skipMerges={}, useMailmap={}", cfg.gix.skipMerges, cfg.gix.useMailmap)
        val branchTraversalResult =
            com.inso_world.binocular.ffi.internal
                .traverseBranch(
                    repo.toFfi(),
                    branchName,
                    skipMerges = cfg.gix.skipMerges,
                    useMailmap = cfg.gix.useMailmap,
                )

        val commits: List<Commit> = branchTraversalResult.commits.toDomain(repo)
        val branch: Branch = with(commits.associateBy { it.sha }.getValue(branchTraversalResult.branch.target)) {
            branchTraversalResult.branch.toDomain(
                repo,
                this
            )
        }

        return Pair(branch, commits)
    }

    override fun findAllBranches(repo: Repository): List<Branch> {
        val binocularRepo = repo.toFfi()
        return com.inso_world.binocular.ffi.internal
            .findAllBranches(binocularRepo)
            .map {
                val head = com.inso_world.binocular.ffi.internal.findCommit(
                    binocularRepo,
                    it.target,
                    useMailmap = cfg.gix.useMailmap,
                ).toDomain(repo)
                val branch = it.toDomain(repo, head)
                branch
            }
    }

    override fun findCommit(
        repo: Repository,
        hash: String,
    ): Commit =
        com.inso_world.binocular.ffi.internal
            .findCommit(
                repo.toFfi(),
                hash,
                useMailmap = cfg.gix.useMailmap
            )
            .toDomain(repo)

    override fun traverse(
        repo: Repository,
        source: Commit,
        target: Commit?,
    ): List<Commit> =
        com.inso_world.binocular.ffi.internal
            .traverseHistory(
                repo.toFfi(), source.sha,
                target?.sha,
                useMailmap = cfg.gix.useMailmap
            )
            .toDomain(repo)

    override fun calculateDiff(
        repo: Repository,
        pairs: Set<Pair<Commit, Commit?>>,
    ): Sequence<CommitDiff> {
        val threadsInUse = getThreadsInUse()

        logger.info("Using $threadsInUse threads to calculate diffs")

        val inputPairs =
            pairs.map { c ->
                return@map GixDiffInput(c.first.sha, c.second?.sha)
            }
        val commitCache =
            pairs
                .map { it.first }
                .union(pairs.mapNotNull { it.second })
                .associateBy(Commit::sha)

        val ffiRepo = repo.toFfi()

        val diffs =
            com.inso_world.binocular.ffi.internal
                .diffs(
                    ffiRepo,
                    commitPairs = inputPairs.toList(),
                    maxThreads = threadsInUse.toUByte(),
                    diffAlgorithm = ALGORITHM,
                ).parallelStream()
                .map {
                    val child =
                        requireNotNull(commitCache[it.commit.oid]) {
                            "Child commit ${it.commit} not present in cache"
                        }
                    val parent = commitCache[it.parent?.oid]

                    val changes =
                        it.files
                            .map { f ->

                                val stats =
                                    Stats(
                                        additions = f.insertions.toLong(),
                                        deletions = f.deletions.toLong(),
                                    )

                                return@map f.toDomain(stats, child, parent)
                            }.toSet()

                    CommitDiff(
                        source = child,
                        target = parent,
                        files = changes,
                        repo,
                    )
                }
        return diffs.asSequence()
    }

    override fun calculateBlames(repo: Repository): Sequence<BlameEntry> {
        val threadsInUse = getThreadsInUse()
        logger.info("Using $threadsInUse threads to calculate diffs")

        val ffiRepo = repo.toFfi()

        TODO()
//        val values =
//            com.inso_world.binocular.ffi.internal
//                .blames(
//                    ffiRepo,
//                    emptyMap(),
//                    maxThreads = threadsInUse.toUByte(),
//                    diffAlgorithm = ALGORITHM,
//                ).parallelStream()
//                .map { it }
//
//        return values
    }

    private fun getThreadsInUse(): Int =
        run {
            val cores = Runtime.getRuntime().availableProcessors()
            min(cores, 12)
        }
}
