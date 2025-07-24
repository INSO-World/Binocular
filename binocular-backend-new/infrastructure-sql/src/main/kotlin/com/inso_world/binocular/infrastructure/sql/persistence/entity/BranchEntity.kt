package com.inso_world.binocular.infrastructure.sql.persistence.entity

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
    @ManyToMany(mappedBy = "branches", fetch = FetchType.LAZY, cascade = [])
    var commits: MutableSet<CommitEntity> = mutableSetOf(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repository_id", nullable = false, updatable = false)
    var repository: RepositoryEntity? = null,
) : AbstractEntity() {
    @PreRemove
    fun preRemove() {
//        this.repository?.branches?.remove(this)
//        this.repository = null
    }

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
        val repo = this.repository ?: throw IllegalStateException("RepositoryEntity required for uniqueKey")
        return "${repo.name},$name"
    }
}
