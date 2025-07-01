package com.inso_world.binocular.web.persistence.entity.sql

import com.inso_world.binocular.web.entity.Platform
import jakarta.persistence.*

/**
 * SQL-specific Account entity.
 */
@Entity
@Table(name = "accounts")
data class AccountEntity(
  @Id
  var id: String? = null,

  @Enumerated(EnumType.STRING)
  var platform: Platform? = null,

  var login: String? = null,
  var name: String? = null,

  @Column(name = "avatar_url")
  var avatarUrl: String? = null,

  var url: String? = null,

  @ManyToMany
  @JoinTable(
    name = "issue_account_connections",
    joinColumns = [JoinColumn(name = "account_id")],
    inverseJoinColumns = [JoinColumn(name = "issue_id")]
  )
  var issues: MutableList<IssueEntity> = mutableListOf(),

  @ManyToMany
  @JoinTable(
    name = "merge_request_account_connections",
    joinColumns = [JoinColumn(name = "account_id")],
    inverseJoinColumns = [JoinColumn(name = "merge_request_id")]
  )
  var mergeRequests: MutableList<MergeRequestEntity> = mutableListOf(),

  @ManyToMany
  @JoinTable(
    name = "note_account_connections",
    joinColumns = [JoinColumn(name = "account_id")],
    inverseJoinColumns = [JoinColumn(name = "note_id")]
  )
  var notes: MutableList<NoteEntity> = mutableListOf()
)
