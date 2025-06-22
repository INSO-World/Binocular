package com.inso_world.binocular.cli.index.vcs

import com.inso_world.binocular.cli.entity.Repository
import com.inso_world.binocular.internal.ThreadSafeRepository

data class VcsRepository(
  val name: String
) {
  fun toEntity(): Repository {
    return Repository(
      name = name,
    )
  }
}

fun ThreadSafeRepository.toVcsRepository(): VcsRepository {
  return VcsRepository(
    name = this.gitDir
  )
}
