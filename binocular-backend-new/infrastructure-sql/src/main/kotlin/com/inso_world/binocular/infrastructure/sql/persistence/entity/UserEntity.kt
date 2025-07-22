package com.inso_world.binocular.infrastructure.sql.persistence.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

/**
 * SQL-specific User entity.
 */
@Entity
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["repository_id", "email"]),
    ],
)
internal data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false)
    var name: String? = null,
    @Column(nullable = false)
    var email: String? = null,
    @OneToMany(fetch = FetchType.LAZY, targetEntity = CommitEntity::class, cascade = [CascadeType.ALL])
    var committedCommits: MutableSet<CommitEntity> = mutableSetOf(),
    @OneToMany(fetch = FetchType.LAZY, targetEntity = CommitEntity::class, cascade = [CascadeType.ALL])
    var authoredCommits: MutableSet<CommitEntity> = mutableSetOf(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repository_id", nullable = false, updatable = false)
    var repository: RepositoryEntity? = null,
) {
    fun uniqueKey(): String {
        val repo = this.repository ?: throw IllegalStateException("RepositoryEntity required for uniqueKey")
        return "${repo.name},$email"
    }

    fun addCommittedCommit(commit: CommitEntity) {
        committedCommits.add(commit)
        commit.committer = this
    }

    fun addAuthoredCommit(commit: CommitEntity) {
        authoredCommits.add(commit)
        commit.author = this
    }
}
