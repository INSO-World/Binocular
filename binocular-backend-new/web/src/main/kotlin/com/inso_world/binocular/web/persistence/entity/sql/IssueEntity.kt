package com.inso_world.binocular.web.persistence.entity.sql

import com.inso_world.binocular.web.entity.Mention
import jakarta.persistence.*
import java.util.*

/**
 * SQL-specific Issue entity.
 */
@Entity
@Table(name = "issues")
class IssueEntity {
  @Id
  var id: String? = null

  var iid: Int? = null

  var title: String? = null

  @Column(columnDefinition = "TEXT")
  var description: String? = null

  @Column(name = "created_at")
  @Temporal(TemporalType.TIMESTAMP)
  var createdAt: Date? = null

  @Column(name = "closed_at")
  @Temporal(TemporalType.TIMESTAMP)
  var closedAt: Date? = null

  @Column(name = "updated_at")
  @Temporal(TemporalType.TIMESTAMP)
  var updatedAt: Date? = null

  var state: String? = null

  @Column(name = "web_url")
  var webUrl: String? = null

  @OneToMany(mappedBy = "issue", cascade = [CascadeType.ALL], orphanRemoval = true)
  var labels: MutableList<LabelEntity> = mutableListOf()

  @OneToMany(mappedBy = "issue", cascade = [CascadeType.ALL], orphanRemoval = true)
  var mentions: MutableList<MentionEntity> = mutableListOf()

  @ManyToMany(mappedBy = "issues")
  var accounts: MutableList<AccountEntity> = mutableListOf()

  @ManyToMany
  @JoinTable(
    name = "issue_commit_connections",
    joinColumns = [JoinColumn(name = "issue_id")],
    inverseJoinColumns = [JoinColumn(name = "commit_id")]
  )
  var commits: MutableList<CommitEntity> = mutableListOf()

  @ManyToMany
  @JoinTable(
    name = "issue_milestone_connections",
    joinColumns = [JoinColumn(name = "issue_id")],
    inverseJoinColumns = [JoinColumn(name = "milestone_id")]
  )
  var milestones: MutableList<MilestoneEntity> = mutableListOf()

  @ManyToMany
  @JoinTable(
    name = "issue_note_connections",
    joinColumns = [JoinColumn(name = "issue_id")],
    inverseJoinColumns = [JoinColumn(name = "note_id")]
  )
  var notes: MutableList<NoteEntity> = mutableListOf()

  @ManyToMany
  @JoinTable(
    name = "issue_user_connections",
    joinColumns = [JoinColumn(name = "issue_id")],
    inverseJoinColumns = [JoinColumn(name = "user_id")]
  )
  var users: MutableList<UserEntity> = mutableListOf()

  // No-arg constructor required by JPA
  constructor()

  // Secondary constructor for convenience
  constructor(
    id: String? = null,
    iid: Int? = null,
    title: String? = null,
    description: String? = null,
    createdAt: Date? = null,
    closedAt: Date? = null,
    updatedAt: Date? = null,
    state: String? = null,
    webUrl: String? = null,
    labels: MutableList<LabelEntity> = mutableListOf(),
    mentions: MutableList<MentionEntity> = mutableListOf(),
    accounts: MutableList<AccountEntity> = mutableListOf(),
    commits: MutableList<CommitEntity> = mutableListOf(),
    milestones: MutableList<MilestoneEntity> = mutableListOf(),
    notes: MutableList<NoteEntity> = mutableListOf(),
    users: MutableList<UserEntity> = mutableListOf()
  ) {
    this.id = id
    this.iid = iid
    this.title = title
    this.description = description
    this.createdAt = createdAt
    this.closedAt = closedAt
    this.updatedAt = updatedAt
    this.state = state
    this.webUrl = webUrl
    this.labels = labels
    this.mentions = mentions
    this.accounts = accounts
    this.commits = commits
    this.milestones = milestones
    this.notes = notes
    this.users = users
  }
  /**
   * Gets the labels as a list of strings
   */
  fun getDomainLabels(): List<String> {
    return labels.map { it.value }
  }

  /**
   * Sets the labels from a list of strings
   */
  fun setDomainLabels(labels: List<String>) {
    // Clear existing labels
    this.labels.clear()

    // Add new labels
    this.labels.addAll(labels.map { label ->
      LabelEntity(
        value = label,
        issue = this
      )
    })
  }

  /**
   * Gets the mentions as domain model mentions
   */
  fun getDomainMentions(): List<Mention> {
    return mentions.map { mentionEntity ->
      Mention(
        commit = mentionEntity.commit,
        createdAt = mentionEntity.createdAt,
        closes = mentionEntity.closes
      )
    }
  }

  /**
   * Sets the mentions from domain model mentions
   */
  fun setDomainMentions(mentions: List<Mention>) {
    // Clear existing mentions
    this.mentions.clear()

    // Add new mentions
    this.mentions.addAll(mentions.map { mention ->
      MentionEntity(
        commit = mention.commit,
        createdAt = mention.createdAt,
        closes = mention.closes,
        issue = this
      )
    })
  }
}
