package com.inso_world.binocular.cli.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.Objects

@Entity
@Table(
    name = "branches",
    indexes = [Index(name = "idx_name", columnList = "name")],
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["repository_id", "name"]),
    ],
)
data class Branch(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false)
    val name: String,
    @ManyToMany(mappedBy = "branches", fetch = FetchType.LAZY, cascade = [CascadeType.REFRESH])
    var commits: MutableSet<Commit> = mutableSetOf(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var repository: Repository? = null,
) {
    fun addCommit(commit: Commit) {
        this.commits.add(commit)
        commit.branches.add(this)
    }

    fun removeCommit(commit: Commit) {
        this.commits.remove(commit)
        commit.branches.remove(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Branch

        return name == other.name && repository?.name == other.repository?.name
    }

    override fun toString(): String = super.toString()

    override fun hashCode(): Int = Objects.hash(name, repository?.name)
}
