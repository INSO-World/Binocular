package com.inso_world.binocular.infrastructure.sql.persistence.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.PreRemove
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime
import java.util.Objects

/**
 * SQL-specific Commit entity.
 */
@Entity
@Table(
    name = "commits",
    indexes = [Index(name = "idx_sha", columnList = "sha")],
    uniqueConstraints = [],
)
internal data class CommitEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long? = null,
    @Column(unique = true, updatable = false)
    @field:Size(min = 40, max = 40)
    var sha: String,
    @Column(name = "author_dt")
    val authorDateTime: LocalDateTime? = null,
    @Column(name = "commit_dt")
    val commitDateTime: LocalDateTime? = null,
    @Column(columnDefinition = "TEXT")
    @field:NotBlank
    var message: String? = null,
    @Column(name = "web_url")
    var webUrl: String? = null,
    @Deprecated("do not use")
    var branch: String? = null,
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "commit_parents",
        joinColumns = [JoinColumn(name = "child_id")],
        inverseJoinColumns = [JoinColumn(name = "parent_id")],
        uniqueConstraints = [UniqueConstraint(columnNames = ["child_id", "parent_id"])],
    )
    var parents: MutableSet<CommitEntity> = mutableSetOf(),
    @ManyToMany(mappedBy = "parents", fetch = FetchType.LAZY)
    var children: MutableSet<CommitEntity> = mutableSetOf(),
    @ManyToMany(targetEntity = BranchEntity::class, fetch = FetchType.LAZY, cascade = [])
    @JoinTable(
        name = "commit_branches",
        joinColumns = [JoinColumn(name = "commit_id", nullable = false)],
        inverseJoinColumns = [JoinColumn(name = "branch_id", nullable = false)],
        uniqueConstraints = [
            UniqueConstraint(columnNames = ["commit_id", "branch_id"]),
        ],
    )
    var branches: MutableSet<BranchEntity> = mutableSetOf(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = [CascadeType.PERSIST])
    var committer: UserEntity? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = true, cascade = [CascadeType.PERSIST])
    var author: UserEntity? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repository_id", nullable = false, updatable = false)
    var repository: RepositoryEntity? = null,
) : AbstractEntity() {
    override fun uniqueKey(): String = this.sha

    override fun equals(other: Any?): Boolean = other is CommitEntity && this.sha == other.sha

    override fun hashCode(): Int = Objects.hashCode(sha)

    @PreRemove
    fun preRemove() {
        this.committer?.let { user ->
            user.committedCommits.remove(this)
            user.authoredCommits.remove(this)
        }
        this.author?.let { user ->
            user.committedCommits.remove(this)
            user.authoredCommits.remove(this)
        }

        this.branches.forEach { branch ->
            branch.commits.remove(this)
        }

        // Clear parent/child relationships
        this.parents.clear()
        this.children.forEach { child ->
            child.parents.remove(this)
        }
    }

    override fun toString(): String =
        "CommitEntity(id=$id, sha='$sha', authorDateTime=$authorDateTime, commitDateTime=$commitDateTime, repository=${repository?.name})"
}
