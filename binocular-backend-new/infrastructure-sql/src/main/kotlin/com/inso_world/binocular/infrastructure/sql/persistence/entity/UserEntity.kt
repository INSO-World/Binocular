package com.inso_world.binocular.infrastructure.sql.persistence.entity

import com.inso_world.binocular.infrastructure.sql.persistence.converter.KotlinUuidConverter
import com.inso_world.binocular.model.Reference
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.PreRemove
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.BatchSize
import java.util.Objects

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
    @Column(nullable = false)
    var name: String,
    @Column(nullable = false)
    var email: String? = null,
    @BatchSize(size = 256)
    @OneToMany(fetch = FetchType.LAZY, targetEntity = CommitEntity::class, cascade = [CascadeType.ALL])
    var committedCommits: MutableSet<CommitEntity> = mutableSetOf(),
    @BatchSize(size = 256)
    @OneToMany(fetch = FetchType.LAZY, targetEntity = CommitEntity::class, cascade = [CascadeType.ALL])
    var authoredCommits: MutableSet<CommitEntity> = mutableSetOf(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repository_id", nullable = false, updatable = false)
    var repository: RepositoryEntity,
    @Column(nullable = false, updatable = false, unique = true)
    @Convert(KotlinUuidConverter::class)
    val iid: User.Id
) : AbstractEntity<Long, UserEntity.Key>() {
    data class Key(val repositoryIid: Repository.Id, val email: String)

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    override var id: Long? = null

    init {
        repository.user.add(this)
    }

    override val uniqueKey: Key
        get() = Key(
            repositoryIid = repository.iid,
            requireNotNull(email)
        )

    @PreRemove
    fun preRemove() {
//        this.repository?.user?.remove(this)
//        this.repository = null
    }

    fun addCommittedCommit(commit: CommitEntity) {
        committedCommits.add(commit)
        commit.committer = this
    }

    fun addAuthoredCommit(commit: CommitEntity) {
        authoredCommits.add(commit)
        commit.author = this
    }

    fun toDomain(repository: Repository): User =
        User(
            name = this.name,
            repository = repository,
        ).apply {
            this.id = this@UserEntity.id?.toString()
            this.email = this@UserEntity.email
        }

    override fun toString(): String = "UserEntity(id=$id, name='$name', email='$email', repositoryId=${repository.id}, committedCommits=${committedCommits.map { it.sha }}, authoredCommits=${authoredCommits.map { it.sha }})"

    override fun equals(other: Any?): Boolean = super.equals(other)

    override fun hashCode(): Int = super.hashCode()
}

internal fun User.toEntity(repository: RepositoryEntity): UserEntity =
    UserEntity(
        iid = this.iid,
        email = this.email,
        name = this.name,
        repository = repository,
        committedCommits = mutableSetOf(),
        authoredCommits = mutableSetOf(),
    ).apply {
        id = this@toEntity.id?.trim()?.toLongOrNull()
    }
