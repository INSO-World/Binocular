package com.inso_world.binocular.web.persistence.entity.sql

import com.inso_world.binocular.web.entity.Stats
import com.inso_world.binocular.web.persistence.entity.sql.connections.CommitFileUserConnectionEntity
import jakarta.persistence.*
import java.util.*

/**
 * SQL-specific Commit entity.
 */
@Entity
@Table(name = "commits")
class CommitEntity {
  @Id
  var id: String? = null

  @Column(unique = true)
  var sha: String? = null

  @Temporal(TemporalType.TIMESTAMP)
  var date: Date? = null

  @Column(columnDefinition = "TEXT")
  var message: String? = null

  @Column(name = "web_url")
  var webUrl: String? = null

  var branch: String? = null

  @OneToOne(mappedBy = "commit", cascade = [CascadeType.ALL], orphanRemoval = true)
  var stats: StatsEntity? = null

  @ManyToMany
  @JoinTable(
    name = "commit_build_connections",
    joinColumns = [JoinColumn(name = "commit_id")],
    inverseJoinColumns = [JoinColumn(name = "build_id")]
  )
  var builds: MutableList<BuildEntity> = mutableListOf()

  @ManyToMany
  @JoinTable(
    name = "commit_commit_connections",
    joinColumns = [JoinColumn(name = "from_commit_id")],
    inverseJoinColumns = [JoinColumn(name = "to_commit_id")]
  )
  var childCommits: MutableList<CommitEntity> = mutableListOf()

  @ManyToMany(mappedBy = "childCommits")
  var parentCommits: MutableList<CommitEntity> = mutableListOf()

  @ManyToMany
  @JoinTable(
    name = "commit_file_connections",
    joinColumns = [JoinColumn(name = "commit_id")],
    inverseJoinColumns = [JoinColumn(name = "file_id")]
  )
  var files: MutableList<FileEntity> = mutableListOf()

  @ManyToMany
  @JoinTable(
    name = "commit_module_connections",
    joinColumns = [JoinColumn(name = "commit_id")],
    inverseJoinColumns = [JoinColumn(name = "module_id")]
  )
  var modules: MutableList<ModuleEntity> = mutableListOf()

  @ManyToMany
  @JoinTable(
    name = "commit_user_connections",
    joinColumns = [JoinColumn(name = "commit_id")],
    inverseJoinColumns = [JoinColumn(name = "user_id")]
  )
  var users: MutableList<UserEntity> = mutableListOf()

  // Keep the original relationship for the complex ternary relationship
  @OneToMany(mappedBy = "commit", cascade = [CascadeType.ALL], orphanRemoval = true)
  var fileUserConnections: MutableList<CommitFileUserConnectionEntity> = mutableListOf()

  @ManyToMany(mappedBy = "commits")
  var issues: MutableList<IssueEntity> = mutableListOf()

  // No-arg constructor required by JPA
  constructor()

  // Secondary constructor for convenience
  constructor(
    id: String? = null,
    sha: String? = null,
    date: Date? = null,
    message: String? = null,
    webUrl: String? = null,
    branch: String? = null,
    stats: StatsEntity? = null,
    builds: MutableList<BuildEntity> = mutableListOf(),
    childCommits: MutableList<CommitEntity> = mutableListOf(),
    parentCommits: MutableList<CommitEntity> = mutableListOf(),
    files: MutableList<FileEntity> = mutableListOf(),
    modules: MutableList<ModuleEntity> = mutableListOf(),
    users: MutableList<UserEntity> = mutableListOf(),
    fileUserConnections: MutableList<CommitFileUserConnectionEntity> = mutableListOf(),
    issues: MutableList<IssueEntity> = mutableListOf()
  ) {
    this.id = id
    this.sha = sha
    this.date = date
    this.message = message
    this.webUrl = webUrl
    this.branch = branch
    this.stats = stats
    this.builds = builds
    this.childCommits = childCommits
    this.parentCommits = parentCommits
    this.files = files
    this.modules = modules
    this.users = users
    this.fileUserConnections = fileUserConnections
    this.issues = issues
  }
  /**
   * Gets the stats object from the entity.
   */
  fun getStats(): Stats? {
    return stats?.let {
      Stats(
        additions = it.additions,
        deletions = it.deletions
      )
    }
  }

  /**
   * Sets the stats object in the entity.
   */
  fun setStats(stats: Stats?) {
    if (stats != null) {
      if (this.stats == null) {
        this.stats = StatsEntity(
          additions = stats.additions,
          deletions = stats.deletions,
          commit = this
        )
      } else {
        this.stats!!.additions = stats.additions
        this.stats!!.deletions = stats.deletions
      }
    } else {
      this.stats = null
    }
  }
}
