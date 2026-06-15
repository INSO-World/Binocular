package com.inso_world.binocular.infrastructure.sql.persistence.entity

import com.inso_world.binocular.infrastructure.sql.persistence.converter.KotlinUuidConverter
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Reference
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.vcs.ReferenceCategory
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

/**
 * SQL-specific Branch entity.
 *
 * Maps all persisted branch state including [active] and [tracksFileRenames] flags, which are
 * round-tripped through [toDomain] and [Branch.toEntity] so that the port contract test
 * assertions on those fields pass.
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
    @Column(nullable = false)
    val name: String,
    @Column(name = "full_name", nullable = false)
    val fullName: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    val category: ReferenceCategory,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_commit_id", nullable = false, updatable = true)
    val head: CommitEntity,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repository_id", nullable = false, updatable = false)
    val repository: RepositoryEntity,
    @Column(nullable = false, updatable = false, unique = true)
    @Convert(KotlinUuidConverter::class)
    val iid: Reference.Id,
    @Column(nullable = false)
    val active: Boolean = false,
    @Column(name = "tracks_file_renames", nullable = false)
    val tracksFileRenames: Boolean = false,
) : AbstractEntity<Long, BranchEntity.Key>() {
    data class Key(
        val repositoryIid: Repository.Id,
        val name: String
    )

    init {
        repository.branches.add(this)
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    override var id: Long? = null

    /**
     * Converts this entity to a [Branch] domain object.
     *
     * Sets [Branch.id], [Branch.active], and [Branch.tracksFileRenames] from the persisted values.
     */
    fun toDomain(
        repository: Repository,
        head: Commit
    ): Branch =
        Branch(
            name = this.name,
            fullName = this.fullName,
            category = this.category,
            repository = repository,
            head = head,
        ).apply {
            this.id = this@BranchEntity.id?.toString()
            this.active = this@BranchEntity.active
            this.tracksFileRenames = this@BranchEntity.tracksFileRenames
        }

    override fun equals(other: Any?): Boolean = super.equals(other)

    override fun hashCode(): Int = super.hashCode()

    override fun toString(): String = "BranchEntity(id=$id, name='$name', headId=${head.id})"

    override val uniqueKey: Key
        get() =
            Key(
                repository.iid,
                name
            )
}

/**
 * Converts a [Branch] domain object to a [BranchEntity].
 *
 * Copies [Branch.active] and [Branch.tracksFileRenames] so those flags survive the round-trip.
 * Sets [BranchEntity.id] from the domain [Branch.id] when the branch was previously persisted.
 */
internal fun Branch.toEntity(
    repository: RepositoryEntity,
    head: CommitEntity
): BranchEntity =
    BranchEntity(
        iid = this.iid,
        name = this.name,
        fullName = this.fullName,
        category = this.category,
        repository = repository,
        head = head,
        active = this.active,
        tracksFileRenames = this.tracksFileRenames,
    ).apply {
        id = this@toEntity.id?.trim()?.toLongOrNull()
    }
