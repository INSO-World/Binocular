package com.inso_world.binocular.infrastructure.sql.persistence.entity

import com.inso_world.binocular.model.Commit
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
import jakarta.persistence.Lob
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.PreRemove
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.hibernate.annotations.BatchSize
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
    val sha: String,
    @Column(name = "author_dt")
    val authorDateTime: LocalDateTime? = null,
    @Column(name = "commit_dt")
    val commitDateTime: LocalDateTime? = null,
    @Column(columnDefinition = "TEXT")
    @Lob
    var message: String? = null,
    @Column(name = "web_url")
    var webUrl: String? = null,
    @Deprecated("do not use")
    var branch: String? = null,
    @BatchSize(size = 256)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "commit_parents",
        joinColumns = [JoinColumn(name = "child_id")],
        inverseJoinColumns = [JoinColumn(name = "parent_id")],
        uniqueConstraints = [UniqueConstraint(columnNames = ["child_id", "parent_id"])],
        indexes = [
            Index(name = "child_idx", columnList = "child_id"),
            Index(name = "parent_idx", columnList = "parent_id"),
            Index(name = "combined_idx", columnList = "child_id,parent_id"),
        ],
    )
    var parents: MutableSet<CommitEntity> = mutableSetOf(),
    @BatchSize(size = 256)
    @ManyToMany(mappedBy = "parents", fetch = FetchType.LAZY)
    var children: MutableSet<CommitEntity> = mutableSetOf(),
    @BatchSize(size = 256)
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
//    @ManyToOne(fetch = FetchType.LAZY, optional = true, cascade = [CascadeType.PERSIST])
//    var author: UserEntity? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repository_id", nullable = false, updatable = false)
    var repository: RepositoryEntity? = null,
) : AbstractEntity() {
    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = [CascadeType.PERSIST])
    var committer: UserEntity? = null
        set(value) {
            if (value == this.committer) {
                return
            }
            if (this.committer != null) {
                throw IllegalArgumentException("Committer already set for Commit $sha: $committer")
            }
            field = value
            field!!.committedCommits.add(this)
            field
        }
//        get() = field

    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = [CascadeType.PERSIST])
    var author: UserEntity? = null
        set(
            @NotNull value,
        ) {
            if (value == this.author) {
                return
            }
            if (this.author != null) {
                throw IllegalArgumentException("Author already set for Commit $sha: $author")
            }
            field = value
            field!!.authoredCommits.add(this)
            field
        }
//        get() = author

    override fun uniqueKey(): String = this.sha

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CommitEntity) return false
        return sha != null && sha == other.sha
    }

    fun addParent(parent: CommitEntity) {
        this.parents.add(parent)
        parent.children.add(this)
    }

    fun addChild(child: CommitEntity) {
        this.children.add(child)
        child.parents.add(this)
    }

    fun addBranch(branch: BranchEntity) {
        this.branches.add(branch)
        branch.commits.add(this)
    }

    override fun hashCode(): Int = Objects.hashCode(sha)

    fun toDomain(): Commit =
        Commit(
            id = this.id?.toString(),
            sha = this.sha,
            commitDateTime = this.commitDateTime,
            authorDateTime = this.authorDateTime,
            message = this.message,
            webUrl = this.webUrl,
        )

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

internal fun Commit.toEntity(): CommitEntity =
    CommitEntity(
        id = this.id?.toLong(),
        sha = this.sha,
        commitDateTime = this.commitDateTime,
        authorDateTime = this.authorDateTime,
        message = this.message,
        webUrl = this.webUrl,
        repository = null,
        parents = mutableSetOf(),
        children = mutableSetOf(),
        branches = mutableSetOf(),
//        committer = null,
//        author = null,
    )
