package com.inso_world.binocular.infrastructure.sql.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import java.time.LocalDateTime
import java.util.Objects

/**
 * SQL-specific Issue entity.
 */
@Entity
@Table(name = "issues")
internal data class IssueEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long? = null,
    var iid: Int? = null,
    var title: String? = null,
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    var createdAt: LocalDateTime? = null,
    @Column(name = "closed_at")
    @Temporal(TemporalType.TIMESTAMP)
    var closedAt: LocalDateTime? = null,
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    var updatedAt: LocalDateTime? = null,
    var state: String? = null,
    @Column(name = "web_url")
    var webUrl: String? = null,
// @OneToMany(mappedBy = "issue", cascade = [CascadeType.ALL], orphanRemoval = true)
// var labels: MutableList<LabelEntity> = mutableListOf()
// @OneToMany(mappedBy = "issue", cascade = [CascadeType.ALL], orphanRemoval = true)
// var mentions: MutableList<MentionEntity> = mutableListOf()
// @ManyToMany(mappedBy = "issues")
// var accounts: MutableList<AccountEntity> = mutableListOf()
// @ManyToMany
// @JoinTable(
//     name = "issue_commit_connections",
//     joinColumns = [JoinColumn(name = "issue_id")],
//     inverseJoinColumns = [JoinColumn(name = "commit_id")],
// )
// var commits: MutableList<CommitEntity> = mutableListOf()
// @ManyToMany
// @JoinTable(
//     name = "issue_milestone_connections",
//     joinColumns = [JoinColumn(name = "issue_id")],
//     inverseJoinColumns = [JoinColumn(name = "milestone_id")],
// )
// var milestones: MutableList<MilestoneEntity> = mutableListOf()
// @ManyToMany
// @JoinTable(
//     name = "issue_note_connections",
//     joinColumns = [JoinColumn(name = "issue_id")],
//     inverseJoinColumns = [JoinColumn(name = "note_id")],
// )
// var notes: MutableList<NoteEntity> = mutableListOf()
    @ManyToMany
    @JoinTable(
        name = "issue_user_connections",
        joinColumns = [JoinColumn(name = "issue_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")],
    )
    var users: MutableList<UserEntity> = mutableListOf(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IssueEntity

        if (id != other.id) return false
        if (iid != other.iid) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (createdAt != other.createdAt) return false
        if (closedAt != other.closedAt) return false
        if (updatedAt != other.updatedAt) return false
        if (state != other.state) return false
        if (webUrl != other.webUrl) return false
        if (users != other.users) return false

        return true
    }

    override fun hashCode(): Int = Objects.hash(id, iid, title, description, createdAt, closedAt, updatedAt, state, webUrl, users)
}
