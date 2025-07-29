package com.inso_world.binocular.infrastructure.sql.persistence.entity

import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
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
import org.hibernate.annotations.BatchSize

@Entity
@Table(name = "repositories")
internal data class RepositoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val id: Long? = null,
    @Column(unique = true, nullable = false)
    @field:NotBlank
    var name: String,
    @BatchSize(size = 256)
    @OneToMany(
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        mappedBy = "repository",
    )
    var commits: MutableSet<CommitEntity> = mutableSetOf(),
    @BatchSize(size = 256)
    @OneToMany(
        fetch = FetchType.LAZY,
        targetEntity = UserEntity::class,
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        mappedBy = "repository",
    )
    var user: MutableSet<UserEntity> = mutableSetOf(),
    @BatchSize(size = 256)
    @OneToMany(
        fetch = FetchType.LAZY,
        targetEntity = BranchEntity::class,
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        mappedBy = "repository",
    )
    var branches: MutableSet<BranchEntity> = mutableSetOf(),
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_project", referencedColumnName = "id", updatable = false, nullable = false)
    var project: ProjectEntity,
) : AbstractEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RepositoryEntity

        if (id != other.id) return false
        if (name != other.name) return false
//        if (commits != other.commits) return false
//        if (user != other.user) return false
//        if (project.uniqueKey() != other.project.uniqueKey()) return false

        return true
    }

    fun addBranch(branch: BranchEntity): Boolean {
        if (branch.repository != null && branch.repository != this) {
            throw IllegalArgumentException("Trying to add a branch where branch.repository != this")
        }

        return this.branches.add(branch).also { added ->
            if (added) branch.repository = this
        }
    }

    fun addCommit(commit: CommitEntity): Boolean {
        if (commit.repository != null && commit.repository != this) {
            throw IllegalArgumentException("Trying to add a commit where commit.repository != this")
        }

        return commits
            .add(commit)
            .also { added ->
                if (added) commit.repository = this
            }
    }

    fun addUser(user: UserEntity): Boolean {
        if (user.repository != null && user.repository != this) {
            throw IllegalArgumentException("Trying to add a user where user.repository != this")
        }

        return this.user
            .add(user)
            .also { added ->
                if (added) user.repository = this
            }
    }

    override fun uniqueKey(): String {
        val project = this.project
        return "${project.name},$name"
    }

    override fun hashCode(): Int = super.hashCode()

    override fun toString(): String = "RepositoryEntity(id=$id, name='$name')"

    fun toDomain(project: Project?): Repository {
        val repo =
            Repository(
                id = this.id?.toString(),
                name = this.name,
                commits = mutableSetOf(),
                branches = mutableSetOf(),
                user = mutableSetOf(),
                project = project,
            )
        project?.repo = repo

        return repo
    }

    @PreRemove
    fun preRemove() {
        project.repo = null
    }
}

internal fun Repository.toEntity(project: ProjectEntity): RepositoryEntity =
    RepositoryEntity(
        id = this.id?.toLong(),
        name = this.name,
        project = project,
    )
