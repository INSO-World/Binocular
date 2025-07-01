package com.inso_world.binocular.web.persistence.entity.sql

import com.inso_world.binocular.web.persistence.entity.sql.connections.CommitFileUserConnectionEntity
import jakarta.persistence.*

/**
 * SQL-specific User entity.
 */
@Entity
@Table(name = "users")
class UserEntity {
  @Id
  var id: String? = null

  @Column(name = "git_signature")
  lateinit var gitSignature: String

  @ManyToMany(mappedBy = "users")
  var commits: MutableList<CommitEntity> = mutableListOf()

  @ManyToMany(mappedBy = "users")
  var issues: MutableList<IssueEntity> = mutableListOf()

  // Keep the original relationship for the complex ternary relationship
  @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
  var commitFileConnections: MutableList<CommitFileUserConnectionEntity> = mutableListOf()

  // No-arg constructor required by JPA
  constructor()

  // Secondary constructor for convenience
  constructor(
    id: String? = null,
    gitSignature: String,
    commits: MutableList<CommitEntity> = mutableListOf(),
    issues: MutableList<IssueEntity> = mutableListOf(),
    commitFileConnections: MutableList<CommitFileUserConnectionEntity> = mutableListOf()
  ) {
    this.id = id
    this.gitSignature = gitSignature
    this.commits = commits
    this.issues = issues
    this.commitFileConnections = commitFileConnections
  }
}
