package com.inso_world.binocular.cli.service

import com.inso_world.binocular.cli.BinocularCliConfiguration
import com.inso_world.binocular.cli.exception.ServiceException
import com.inso_world.binocular.cli.index.vcs.VcsBranch
import com.inso_world.binocular.cli.index.vcs.toVcsBranch
import com.inso_world.binocular.cli.uniffi.AnyhowException
import com.inso_world.binocular.cli.uniffi.BinocularCommitVec
import com.inso_world.binocular.cli.uniffi.BinocularException
import com.inso_world.binocular.cli.uniffi.ThreadSafeRepository
import com.inso_world.binocular.cli.uniffi.findCommit
import com.inso_world.binocular.cli.uniffi.traverseBranch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
internal class FfiService(
  @Autowired val binocularCliConfiguration: BinocularCliConfiguration,
) {
  //  private val repo: ThreadSafeRepository
  private var logger: Logger = LoggerFactory.getLogger(FfiService::class.java)

//  init {
//    this.repo = this.findRepo()
//  }

  fun findAllBranches(repo: ThreadSafeRepository): List<VcsBranch> {
    val branches = com.inso_world.binocular.cli.uniffi.findAllBranches(repo)

    return branches.map {
      it.name = it.name.replace("refs/remotes/", "").replace("refs/heads/", "")
      it
    }.map { it.toVcsBranch() }
  }

  fun findRepo(path: String?): ThreadSafeRepository {
    val path = path ?: this.binocularCliConfiguration.index.path
    logger.trace("Searching repository... at '${path}'")
    return try {
      com.inso_world.binocular.cli.uniffi.findRepo(path)
    } catch (e: AnyhowException) {
      throw ServiceException(e)
    }
  }

  fun traverseAllOnBranch(
    repo: ThreadSafeRepository,
    branch: String,
  ): List<BinocularCommitVec> {
    return try {
      traverseBranch(repo, branch)
    } catch (e: BinocularException) {
      throw ServiceException(e)
    }
  }

}
