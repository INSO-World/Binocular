package com.inso_world.binocular.web.persistence.entity.sql

import com.inso_world.binocular.web.entity.Mention
import jakarta.persistence.*
import java.util.*

/**
 * SQL-specific MergeRequest entity.
 */
@Entity
@Table(name = "merge_requests")
data class MergeRequestEntity(
  @Id
  var id: String? = null,

  var iid: Int? = null,
  var title: String? = null,

  @Column(columnDefinition = "TEXT")
  var description: String? = null,

  @Column(name = "created_at")
  var createdAt: String? = null,

  @Column(name = "closed_at")
  var closedAt: String? = null,

  @Column(name = "updated_at")
  var updatedAt: String? = null,

  @ElementCollection
  @CollectionTable(name = "merge_request_labels", joinColumns = [JoinColumn(name = "merge_request_id")])
  @Column(name = "label")
  var labels: List<String> = emptyList(),

  var state: String? = null,

  @Column(name = "web_url")
  var webUrl: String? = null,

  @OneToMany(mappedBy = "mergeRequest", cascade = [CascadeType.ALL], orphanRemoval = true)
  var mentions: MutableList<MentionEntity> = mutableListOf(),

  @ManyToMany(mappedBy = "mergeRequests")
  var accounts: MutableList<AccountEntity> = mutableListOf(),

  @ManyToMany
  @JoinTable(
    name = "merge_request_milestone_connections",
    joinColumns = [JoinColumn(name = "merge_request_id")],
    inverseJoinColumns = [JoinColumn(name = "milestone_id")]
  )
  var milestones: MutableList<MilestoneEntity> = mutableListOf(),

  @ManyToMany
  @JoinTable(
    name = "merge_request_note_connections",
    joinColumns = [JoinColumn(name = "merge_request_id")],
    inverseJoinColumns = [JoinColumn(name = "note_id")]
  )
  var notes: MutableList<NoteEntity> = mutableListOf()
) {
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
        mergeRequest = this
      )
    })
  }
}
