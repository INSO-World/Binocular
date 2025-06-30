package com.inso_world.binocular.web.persistence.entity.sql

import com.inso_world.binocular.web.entity.Build
import jakarta.persistence.*
import java.util.*

/**
 * SQL-specific Build entity.
 */
@Entity
@Table(name = "builds")
class BuildEntity {
  @Id
  var id: String? = null

  var sha: String? = null
  var ref: String? = null
  var status: String? = null
  var tag: String? = null
  @Column(name = "username")
  var user: String? = null

  @Column(name = "user_full_name")
  var userFullName: String? = null

  @Column(name = "created_at")
  @Temporal(TemporalType.TIMESTAMP)
  var createdAt: Date? = null

  @Column(name = "updated_at")
  @Temporal(TemporalType.TIMESTAMP)
  var updatedAt: Date? = null

  @Column(name = "started_at")
  @Temporal(TemporalType.TIMESTAMP)
  var startedAt: Date? = null

  @Column(name = "finished_at")
  @Temporal(TemporalType.TIMESTAMP)
  var finishedAt: Date? = null

  @Column(name = "committed_at")
  @Temporal(TemporalType.TIMESTAMP)
  var committedAt: Date? = null

  var duration: Int? = null

  @Column(name = "web_url")
  var webUrl: String? = null

  @OneToMany(mappedBy = "build", cascade = [CascadeType.ALL], orphanRemoval = true)
  var jobs: MutableList<JobEntity> = mutableListOf()

  @ManyToMany(mappedBy = "builds")
  var commits: MutableList<CommitEntity> = mutableListOf()

  // No-arg constructor required by JPA
  constructor()

  // Secondary constructor for convenience
  constructor(
    id: String? = null,
    sha: String? = null,
    ref: String? = null,
    status: String? = null,
    tag: String? = null,
    user: String? = null,
    userFullName: String? = null,
    createdAt: Date? = null,
    updatedAt: Date? = null,
    startedAt: Date? = null,
    finishedAt: Date? = null,
    committedAt: Date? = null,
    duration: Int? = null,
    webUrl: String? = null,
    jobs: MutableList<JobEntity> = mutableListOf(),
    commits: MutableList<CommitEntity> = mutableListOf()
  ) {
    this.id = id
    this.sha = sha
    this.ref = ref
    this.status = status
    this.tag = tag
    this.user = user
    this.userFullName = userFullName
    this.createdAt = createdAt
    this.updatedAt = updatedAt
    this.startedAt = startedAt
    this.finishedAt = finishedAt
    this.committedAt = committedAt
    this.duration = duration
    this.webUrl = webUrl
    this.jobs = jobs
    this.commits = commits
  }
  /**
   * Converts the entity jobs to domain model jobs
   */
  fun getDomainJobs(): List<Build.Job> {
    return jobs.map { jobEntity ->
      Build.Job(
        id = jobEntity.id,
        name = jobEntity.name,
        status = jobEntity.status,
        stage = jobEntity.stage,
        createdAt = jobEntity.createdAt,
        finishedAt = jobEntity.finishedAt,
        webUrl = jobEntity.webUrl
      )
    }
  }

  /**
   * Sets the jobs from domain model jobs
   */
  fun setDomainJobs(jobs: List<Build.Job>) {
    // Clear existing jobs
    this.jobs.clear()

    // Add new jobs
    this.jobs.addAll(jobs.map { job ->
      JobEntity(
        id = job.id,
        name = job.name,
        status = job.status,
        stage = job.stage,
        createdAt = job.createdAt,
        finishedAt = job.finishedAt,
        webUrl = job.webUrl,
        build = this
      )
    })
  }
}
