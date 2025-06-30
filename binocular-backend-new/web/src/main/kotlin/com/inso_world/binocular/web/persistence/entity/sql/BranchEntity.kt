package com.inso_world.binocular.web.persistence.entity.sql

import jakarta.persistence.*

/**
 * SQL-specific Branch entity.
 */
@Entity
@Table(name = "branches")
class BranchEntity {
  @Id
  var id: String? = null

  var branch: String? = null

  var active: Boolean = false

  @Column(name = "tracks_file_renames")
  var tracksFileRenames: Boolean = false

  @Column(name = "latest_commit")
  var latestCommit: String? = null

  @ManyToMany(mappedBy = "branches")
  var files: MutableList<FileEntity> = mutableListOf()

  // No-arg constructor required by JPA
  constructor()

  // Secondary constructor for convenience
  constructor(
    id: String? = null,
    branch: String? = null,
    active: Boolean = false,
    tracksFileRenames: Boolean = false,
    latestCommit: String? = null,
    files: MutableList<FileEntity> = mutableListOf()
  ) {
    this.id = id
    this.branch = branch
    this.active = active
    this.tracksFileRenames = tracksFileRenames
    this.latestCommit = latestCommit
    this.files = files
  }
}
