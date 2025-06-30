package com.inso_world.binocular.web.persistence.entity.sql

import jakarta.persistence.*

/**
 * SQL-specific Module entity.
 */
@Entity
@Table(name = "modules")
class ModuleEntity {
  @Id
  var id: String? = null

  lateinit var path: String

  @ManyToMany(mappedBy = "modules")
  var commits: MutableList<CommitEntity> = mutableListOf()

  @ManyToMany(mappedBy = "modules")
  var files: MutableList<FileEntity> = mutableListOf()

  @ManyToMany
  @JoinTable(
    name = "module_module_connections",
    joinColumns = [JoinColumn(name = "parent_module_id")],
    inverseJoinColumns = [JoinColumn(name = "child_module_id")]
  )
  var childModules: MutableList<ModuleEntity> = mutableListOf()

  @ManyToMany(mappedBy = "childModules")
  var parentModules: MutableList<ModuleEntity> = mutableListOf()

  // No-arg constructor required by JPA
  constructor()

  // Secondary constructor for convenience
  constructor(
    id: String?,
    path: String,
    commits: MutableList<CommitEntity> = mutableListOf(),
    files: MutableList<FileEntity> = mutableListOf(),
    childModules: MutableList<ModuleEntity> = mutableListOf(),
    parentModules: MutableList<ModuleEntity> = mutableListOf()
  ) {
    this.id = id
    this.path = path
    this.commits = commits
    this.files = files
    this.childModules = childModules
    this.parentModules = parentModules
  }
}
