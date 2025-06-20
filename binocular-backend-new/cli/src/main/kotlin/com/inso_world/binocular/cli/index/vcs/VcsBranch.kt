package com.inso_world.binocular.cli.index.vcs

import com.inso_world.binocular.cli.entity.Branch
import com.inso_world.binocular.cli.uniffi.BinocularBranch

data class VcsBranch(
  val name: String
) {

  fun toEntity(): Branch {
    return Branch(
      name = this.name,
    )
  }

}

fun BinocularBranch.toVcsBranch(): VcsBranch {
  return VcsBranch(
    name = this.name
  )
}
