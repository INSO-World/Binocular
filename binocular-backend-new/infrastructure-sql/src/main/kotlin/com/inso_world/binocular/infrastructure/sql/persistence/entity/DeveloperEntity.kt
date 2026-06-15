package com.inso_world.binocular.infrastructure.sql.persistence.entity

import com.inso_world.binocular.infrastructure.sql.persistence.converter.KotlinUuidConverter
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Repository
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

/**
 * SQL-specific Developer entity stored in the legacy `users` table.
 *
 * This maps the refactored domain `Developer` (required name + email) while
 * keeping the existing table name/constraints for compatibility.
 */
@Entity
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["repository_id", "email"]),
    ],
)
internal data class DeveloperEntity(
    @Column(nullable = false)
    var name: String,
    @Column(nullable = false)
    var email: String,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repository_id", nullable = false, updatable = false)
    var repository: RepositoryEntity,
    @Column(nullable = false, updatable = false, unique = true)
    @Convert(KotlinUuidConverter::class)
    val iid: Developer.Id,
) : AbstractEntity<Long, DeveloperEntity.Key>() {
    data class Key(
        val repositoryIid: Repository.Id,
        val email: String,
    )

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    override var id: Long? = null

    init {
        repository.developers.add(this)
    }

    override val uniqueKey: Key
        get() = Key(repositoryIid = repository.iid, email = email)

    override fun equals(other: Any?): Boolean = super.equals(other)

    override fun hashCode(): Int = super.hashCode()

    fun toDomain(repository: Repository): Developer =
        Developer(
            name = this.name,
            email = this.email,
            repository = repository,
        ).apply {
            this.id = this@DeveloperEntity.id?.toString()
        }

    override fun toString(): String = "DeveloperEntity(id=$id, iid=$iid, name='$name', email='$email', repositoryId=${repository.id})"
}

/**
 * Converts this [Developer] to a [DeveloperEntity] owned by [repository].
 *
 * Preserves [Developer.id] on the resulting entity, but only when [repository] itself already has
 * a non-null `id` (i.e. an existing repository row is being re-mapped), mirroring
 * [Repository.toEntity]. Without this, re-mapping an already-persisted [Developer] during a
 * subsequent [com.inso_world.binocular.infrastructure.sql.service.RepositoryInfrastructurePortImpl.update]
 * call would produce a transient entity (`id == null`), causing Hibernate to insert a duplicate
 * `users` row and violate the `users_iid_key` unique constraint. Conversely, when [repository] is a
 * fresh insert (`repository.id == null`), `id` is forced to `null` even if [Developer.id] carries a
 * stale value from a previously persisted (and since-deleted) row, since Hibernate would otherwise
 * reject the cascaded insert as a "detached entity passed to persist".
 *
 * @param repository The owning [RepositoryEntity].
 * @return A [DeveloperEntity] with `id` carried over from [Developer.id] when present and [repository] is not new.
 */
internal fun Developer.toEntity(repository: RepositoryEntity): DeveloperEntity =
    DeveloperEntity(
        iid = this.iid,
        email = this.email,
        name = this.name,
        repository = repository,
    ).apply { id = repository.id?.let { this@toEntity.id?.trim()?.toLongOrNull() } }
