package com.inso_world.binocular.infrastructure.sql.persistence.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.Objects

/**
 * SQL-specific Branch entity.
 */
@Entity
@Table(name = "branches")
internal data class BranchEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false)
    val name: String,
    @ManyToMany(mappedBy = "branches", fetch = FetchType.LAZY, cascade = [CascadeType.REFRESH])
    var commits: MutableSet<CommitEntity> = mutableSetOf(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
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
}
