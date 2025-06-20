package com.inso_world.binocular.cli.integration.utils

import com.inso_world.binocular.cli.entity.Branch
import com.inso_world.binocular.cli.entity.Commit
import com.inso_world.binocular.cli.entity.Repository
import com.inso_world.binocular.cli.entity.User
import com.inso_world.binocular.cli.index.vcs.toDto
import com.inso_world.binocular.cli.uniffi.*

internal data class RepositoryConfig(
  val repo: ThreadSafeRepository,
  val startCommit: ObjectId,
  val hashes: List<BinocularCommitVec>
)

internal fun setupRepoConfig(path: String, startSha: String? = "HEAD"): RepositoryConfig {
  val repo = findRepo(path)
  val cmt = findCommit(repo, startSha ?: "HEAD")
  val hashes = traverseBranch(repo, "master")
  return RepositoryConfig(
    repo = repo,
    startCommit = cmt,
    hashes = hashes
  )
}

internal fun generateCommits(repoConfig: RepositoryConfig, concreteRepo: Repository): List<Commit> {
  val userCache = mutableMapOf<String, User>()
  val branchCache = mutableMapOf<String, Branch>()
  val dtos = repoConfig.hashes.map { it.toDto() }

  val commits = dtos
    .map { vcsCommit ->
      val commitEntity = vcsCommit.toEntity()

      vcsCommit.committer?.let {
        val email = it.email
        val user = userCache.getOrPut(email) {
          val e = it.toEntity()
          e.repository = concreteRepo
          e
        }
        commitEntity.committer = user
      }

      vcsCommit.author?.let {
        val email = it.email
        val user = userCache.getOrPut(email) {
          val e = it.toEntity()
          e.repository = concreteRepo
          e
        }
        commitEntity.author = user
      }

      vcsCommit.branch.let { branchName ->
        val branch = branchCache.getOrPut(branchName) {
          val e = Branch(name = branchName, repository = concreteRepo)
          e
        }
        commitEntity.addBranch(branch)
      }

      commitEntity.repository = concreteRepo
      commitEntity
    }

  val commitMap = commits.associate { it.sha to it }
  commitMap.forEach { (sha, commit) ->
    // Find the VcsCommit that corresponds to this entity
    val vcsCommit = dtos.find { it.sha == sha }

    // Get the parent commits from the map and set them
    val parentCommits = vcsCommit?.parents?.mapNotNull { parentSha ->
      commitMap[parentSha]
    } ?: emptyList()

    // Set the parents on the entity
    commit.parents = parentCommits
  }

  concreteRepo.commits = commits.toMutableSet()
  concreteRepo.branches = branchCache.values.toMutableSet()
  concreteRepo.user = userCache.values.toMutableSet()

  return commits
}
