package com.inso_world.binocular.cli.service

import com.inso_world.binocular.cli.BinocularCliConfiguration
import com.inso_world.binocular.cli.exception.ServiceException
import com.inso_world.binocular.cli.index.vcs.VcsBranch
import com.inso_world.binocular.cli.index.vcs.toVcsBranch
import com.inso_world.binocular.ffi.BinocularFfi
import com.inso_world.binocular.internal.AnyhowException
import com.inso_world.binocular.internal.BinocularCommitVec
import com.inso_world.binocular.internal.BinocularException
import com.inso_world.binocular.internal.ThreadSafeRepository
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
  private val ffi = BinocularFfi()

//  init {
//    this.repo = this.findRepo()
//  }

  fun findAllBranches(repo: ThreadSafeRepository): List<VcsBranch> {
    val branches = ffi.findAllBranches(repo)

    return branches.map {
      it.name = it.name.replace("refs/remotes/", "").replace("refs/heads/", "")
      it
    }.map { it.toVcsBranch() }
  }

  fun findRepo(path: String?): ThreadSafeRepository {
    val path = path ?: this.binocularCliConfiguration.index.path
    logger.trace("Searching repository... at '${path}'")
    return try {
      ffi.findRepo(path)
    } catch (e: AnyhowException) {
      throw ServiceException(e)
    }
  }

  fun traverseAllOnBranch(
    repo: ThreadSafeRepository,
    branch: String,
  ): List<BinocularCommitVec> {
    return try {
      ffi.traverseBranch(repo, branch)
    } catch (e: BinocularException) {
      throw ServiceException(e)
    }
  }

}
