package com.inso_world.binocular.infrastructure.sql.persistence.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.PreRemove
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import java.util.Objects

@Entity(name = "repository")
@Table(name = "repositories")
internal data class RepositoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(unique = true, nullable = false)
    @field:NotBlank
    val name: String,
    @OneToMany(
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        mappedBy = "repository",
    )
    var commits: MutableSet<CommitEntity> = mutableSetOf(),
    @OneToMany(
        fetch = FetchType.LAZY,
        targetEntity = UserEntity::class,
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        mappedBy = "repository",
    )
    val user: MutableSet<UserEntity> = mutableSetOf(),
    @OneToMany(
        fetch = FetchType.LAZY,
        targetEntity = BranchEntity::class,
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        mappedBy = "repository",
    )
    var branches: MutableSet<BranchEntity> = mutableSetOf(),
    @OneToOne(fetch = FetchType.LAZY, optional = false, mappedBy = "repo")
    @JoinColumn(name = "fk_project", referencedColumnName = "id", updatable = false)
    var project: ProjectEntity,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RepositoryEntity

//        if (id != other.id) return false
        if (name != other.name) return false
        if (commits != other.commits) return false
        if (user != other.user) return false
        if (project != other.project) return false

        return true
    }

    override fun hashCode(): Int = Objects.hash(id, name)

    override fun toString(): String = "RepositoryEntity(id=$id, name='$name')"

    @PreRemove
    fun preRemove() {
        project.repo = null
    }
}
