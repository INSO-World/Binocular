package com.inso_world.binocular.cli.index.vcs

import com.inso_world.binocular.cli.entity.User
import com.inso_world.binocular.cli.uniffi.BinocularSig

data class VcsPerson(
  val name: String,
  val email: String,
) {
  fun toEntity(): User {
    return User(
      name = this.name,
      email = this.email,
    )
  }
}

fun BinocularSig.toVcsPerson(): VcsPerson {
  return VcsPerson(
    name = this.name,
    email = this.email,
  )
}
