package com.inso_world.binocular.infrastructure.sql.persistence.entity

import com.inso_world.binocular.infrastructure.sql.persistence.converter.KotlinUuidConverter
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Convert
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
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.hibernate.annotations.BatchSize
import java.time.LocalDateTime

@Entity
@Table(
    name = "commits",
    indexes = [Index(name = "idx_sha", columnList = "sha")],
    uniqueConstraints = [],
)
internal data class CommitEntity(
    @Column(nullable = false, updatable = false, unique = true)
    @Convert(KotlinUuidConverter::class)
    val iid: Commit.Id,
    @Column(unique = true, updatable = false)
    @field:Size(min = 40, max = 40)
    val sha: String,
    @Column(name = "author_dt")
    // authorDateTime is var since it can change between two indexing sessions
    var authorDateTime: LocalDateTime? = null,
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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repository_id", nullable = false, updatable = false)
    val repository: RepositoryEntity,
) : AbstractEntity<Long, CommitEntity.Key>() {
    data class Key(val sha: String) // value object for lookups

    init {
        this.repository.commits.add(this)
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    override var id: Long? = null


    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = [CascadeType.ALL])
    var committer: UserEntity? = null
        set(@NotNull value) {
            requireNotNull(value) { "committer cannot be set to null" }
            if (value == this.committer) {
                return
            }
            if (this.committer != null) {
                throw IllegalArgumentException("committer already set for Commit $sha: $committer")
            }
            if (value.repository != this.repository) {
                throw IllegalArgumentException("Repository between $value and Commit do not match: ${this.repository}")
            }
            field = value
            value.committedCommits.add(this)
        }

    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = [CascadeType.ALL])
    var author: UserEntity? = null
        set(@NotNull value) {
            requireNotNull(value) { "author cannot be set to null" }
            if (value == this.author) {
                return
            }
            if (this.author != null) {
                throw IllegalArgumentException("committer already set for Commit $sha: $author")
            }
            if (value.repository != this.repository) {
                throw IllegalArgumentException("Repository between $value and Commit do not match: ${this.repository}")
            }
            field = value
            value.committedCommits.add(this)
        }

    override val uniqueKey: Key
        get() = Key(this.sha)

    override fun equals(other: Any?): Boolean = super.equals(other)

    override fun hashCode(): Int = super.hashCode()

    fun toDomain(repository: Repository, committer: com.inso_world.binocular.model.User): Commit =
        Commit(
            sha = this.sha,
            commitDateTime = this.commitDateTime,
            authorDateTime = this.authorDateTime,
            repository = repository,
            message = this.message,
            committer = committer,
            ).apply {
            this.id = this@CommitEntity.id?.toString()
            this.webUrl = this@CommitEntity.webUrl
        }

    override fun toString(): String =
        "CommitEntity(id=$id, sha='$sha', authorDateTime=$authorDateTime, commitDateTime=$commitDateTime, repository=${repository?.localPath})"
}

internal fun Commit.toEntity(repository: RepositoryEntity): CommitEntity =
    CommitEntity(
        iid = this.iid,
        sha = this.sha,
        commitDateTime = this.commitDateTime,
        authorDateTime = this.authorDateTime,
        message = this.message,
        webUrl = this.webUrl,
        repository = repository,
        parents = mutableSetOf(),
        children = mutableSetOf(),
    ).apply {
        this.id = this@toEntity.id?.trim()?.toLongOrNull()
    }
