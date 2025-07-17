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
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.Objects

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false)
    val name: String,
//    @field:NotEmpty // TODO add
    @ManyToMany(mappedBy = "branches", fetch = FetchType.LAZY, cascade = [])
    var commits: MutableSet<CommitEntity> = mutableSetOf(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repository_id", nullable = false)
    var repository: RepositoryEntity? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BranchEntity

        if (id != other.id) return false
        if (name != other.name) return false
        if (commits != other.commits) return false
        if (repository != other.repository) return false

        return true
    }

    override fun hashCode(): Int = Objects.hash(id, name, commits, repository)

    override fun toString(): String = "BranchEntity(id=$id, name='$name', commits=$commits)"

    fun uniqueKey(): String {
        val repo = this.repository ?: throw IllegalStateException("RepositoryEntity required for uniqueKey")
        return "${repo.name},$name"
    }
}
