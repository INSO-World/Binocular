package com.inso_world.binocular.cli.integration.utils

import com.inso_world.binocular.cli.entity.Commit
import com.inso_world.binocular.cli.entity.Repository
import com.inso_world.binocular.cli.index.vcs.toDto
import com.inso_world.binocular.cli.service.RepositoryService
import com.inso_world.binocular.cli.uniffi.*

internal data class RepositoryConfig(
  val repo: ThreadSafeRepository,
  val startCommit: ObjectId,
  val hashes: List<BinocularCommitVec>
)

internal fun setupRepoConfig(path: String, startSha: String? = "HEAD", branch: String = "master"): RepositoryConfig {
  val repo = findRepo(path)
  val cmt = findCommit(repo, startSha ?: "HEAD")
  val hashes = traverseBranch(repo, branch)
  return RepositoryConfig(
    repo = repo,
    startCommit = cmt,
    hashes = hashes
  )
}

internal fun generateCommits(repoConfig: RepositoryConfig, concreteRepo: Repository): List<Commit> {
  return RepositoryService().transformCommits(concreteRepo, repoConfig.hashes.map { it.toDto() }).toList()
}
