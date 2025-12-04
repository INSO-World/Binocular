package com.inso_world.binocular.infrastructure.sql.persistence.entity

import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity.Key
import com.inso_world.binocular.model.Project
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
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
    override var id: Long? = null,
    val gid: String, // external GitHub id
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

    // project connection
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, updatable = false)
    var project: ProjectEntity,

// @OneToMany(mappedBy = "issue", cascade = [CascadeType.ALL], orphanRemoval = true)
// var labels: MutableList<LabelEntity> = mutableListOf()
// @OneToMany(mappedBy = "issue", cascade = [CascadeType.ALL], orphanRemoval = true)
// var mentions: MutableList<MentionEntity> = mutableListOf()

    @ManyToMany
    @JoinTable(
        name = "issue_account_connections",
        joinColumns = [JoinColumn(name = "issue_id")],
        inverseJoinColumns = [JoinColumn(name = "account_id")],
    )
    var accounts: MutableList<AccountEntity> = mutableListOf(),

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
) : AbstractEntity<Long, IssueEntity.Key>() {

    data class Key(val projectIid: Project.Id, val gid: String) // value object for lookups

//    /**
//     * Gets the mentions as domain model mentions
//     */
//    fun getDomainMentions(): List<Mention> {
//        return mentions.map {
//            Mention(
//                commit = it.commit,
//                createdAt = it.createdAt,
//                closes = it.closes
//            )
//        }
//    }
//
//    /**
//     * Sets the mentions from domain model mentions
//     */
//    fun setDomainMentions(mentions: List<Mention>) {
//        this.mentions.clear()
//        this.mentions.addAll(mentions.map {
//            MentionEntity(
//                null,
//                it.commit,
//                it.createdAt,
//                it.closes,
//                null,
//                this,
//                null,
//                null
//            )
//        })
//    }
//
//    /**
//     * Gets the labels as domain model labels
//     */
//    fun getDomainLabels(): List<String> {
//        return labels.map { it.value }
//    }
//
//    /**
//     * Sets the labels from domain model labels
//     */
//    fun setDomainLabels(labels: List<String>) {
//        this.labels.clear()
//        this.labels.addAll(labels.map { LabelEntity(null, it, this, null) })
//    }

    // TODO map labels and mentions
    fun toDomain(project: Project) = com.inso_world.binocular.model.Issue(
        project = project,
        id = this.id?.toString(),
        platformIid = this.iid,
        gid = this.gid,
        title = this.title,
        description = this.description,
        createdAt = this.createdAt,
        closedAt = this.closedAt,
        updatedAt = this.updatedAt,
        state = this.state,
        webUrl = this.webUrl,
//        labels = getDomainLabels(),
//        mentions = getDomainMentions(),
        // These relationships will be populated by the mapper
//        accounts = emptyList(),
        commits = emptyList(),
        milestones = emptyList(),
//        notes = emptyList(),
//        users = emptyList(),
    )

    override val uniqueKey: Key
        get() = Key(project.iid, gid)
}

internal fun com.inso_world.binocular.model.Issue.toEntity(owner: ProjectEntity): IssueEntity {
    val entity = IssueEntity(
        id = this.id?.toLong(),
        iid = this.platformIid,
        title = this.title,
        description = this.description,
        createdAt = this.createdAt,
        closedAt = this.closedAt,
        updatedAt = this.updatedAt,
        state = this.state,
        webUrl = this.webUrl,
        gid = this.gid,
        project = owner
    )

    // Set labels and mentions
//    entity.setDomainLabels(this.labels)
//    entity.setDomainMentions(this.mentions)

    return entity
}

