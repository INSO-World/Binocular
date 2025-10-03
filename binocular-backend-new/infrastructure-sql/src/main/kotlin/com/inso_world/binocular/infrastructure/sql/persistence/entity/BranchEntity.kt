package com.inso_world.binocular.infrastructure.sql.persistence.entity

import com.inso_world.binocular.model.Branch
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.PreRemove
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.BatchSize

/**
 * SQL-specific Branch entity.
 */
@Entity
@Table(
    name = "branches",
    indexes = [Index(name = "idx_name", columnList = "name")],
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["repository_id", "name"]),
    ],
)
internal data class BranchEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long? = null,
    @Column(nullable = false)
    val name: String,
    @BatchSize(size = 256)
    @ManyToMany(mappedBy = "branches", fetch = FetchType.LAZY, cascade = [])
    val commits: MutableSet<CommitEntity> = mutableSetOf(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repository_id", nullable = false, updatable = false)
    var repository: RepositoryEntity? = null,
) : AbstractEntity() {
    @PreRemove
    fun preRemove() {
//        this.repository?.branches?.remove(this)
//        this.repository = null
    }

    fun addCommit(commit: CommitEntity) {
        this.commits.add(commit)
        commit.branches.add(this)
    }

    fun toDomain(): Branch =
        Branch(
            id = this.id?.toString(),
            name = this.name,
            repository = null,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BranchEntity

        if (id != other.id) return false
        if (name != other.name) return false
//        if (repository?.uniqueKey() != other.repository?.uniqueKey()) return false

        return true
    }

    override fun hashCode(): Int = super.hashCode()

    override fun toString(): String = "BranchEntity(id=$id, name='$name', commits=${commits.map { it.sha }})"

    override fun uniqueKey(): String {
        val repo =
            requireNotNull(this.repository) {
                "RepositoryEntity required for uniqueKey"
            }
        return "${repo.localPath},$name"
    }
}

internal fun Branch.toEntity(): BranchEntity =
    BranchEntity(
        id = this.id?.toLong(),
        name = this.name,
        repository = null,
    )
