package com.inso_world.binocular.infrastructure.sql.persistence.entity

import com.inso_world.binocular.infrastructure.sql.persistence.converter.KotlinUuidConverter
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.vcs.Remote
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "remotes",
    indexes = [Index(name = "idx_remote_name", columnList = "name")],
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["repository_id", "name"]),
    ],
)
internal data class RemoteEntity(
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    var url: String,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repository_id", nullable = false, updatable = false)
    val repository: RepositoryEntity,
    @Column(nullable = false, updatable = false, unique = true)
    @Convert(KotlinUuidConverter::class)
    val iid: Remote.Id
) : AbstractEntity<Long, RemoteEntity.Key>() {

    data class Key(val repositoryIid: Repository.Id, val name: String)

    init {
        repository.remotes.add(this)
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    override var id: Long? = null

    fun toDomain(repository: Repository): Remote =
        Remote(
            name = this.name,
            url = this.url,
            repository = repository
        ).apply {
            this.id = this@RemoteEntity.id?.toString()
        }

    override fun equals(other: Any?): Boolean = super.equals(other)

    override fun hashCode(): Int = super.hashCode()

    override fun toString(): String = "RemoteEntity(id=$id, name='$name', url='$url')"

    override val uniqueKey: Key
        get() = Key(
            repository.iid,
            name
        )
}

internal fun Remote.toEntity(repository: RepositoryEntity): RemoteEntity =
    RemoteEntity(
        iid = this.iid,
        name = this.name,
        url = this.url,
        repository = repository,
    ).apply {
        id = this@toEntity.id?.trim()?.toLongOrNull()
    }
