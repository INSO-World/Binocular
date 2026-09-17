package com.inso_world.binocular.infrastructure.sql.persistence.entity

import com.inso_world.binocular.infrastructure.sql.persistence.converter.KotlinUuidConverter
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.Lob
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.validation.constraints.Size
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDateTime

@Entity
@Table(
    name = "files", 
    uniqueConstraints = [],
)
internal data class FileEntity(
    @Column(nullable = false, updatable = false, unique = true)
    @Convert(KotlinUuidConverter::class)
    val iid: File.Id,
    @Column(name = "path")
    var path: String? = null,
    //@BatchSize(size = 256)
    //@JoinColumn(name = "repository_id", nullable = false, updatable = false)
    //@OnDelete(action = OnDeleteAction.CASCADE)
    //val repository: RepositoryEntity,
) : AbstractEntity<Long, FileEntity.Key>() {
    data class Key(
        val path: String,
    )

    init {}

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    override var id: Long? = null

    override val uniqueKey: Key
        get() = Key((this.path ?: ""))

    override fun equals(other: Any?): Boolean = super.equals(other)

    override fun hashCode(): Int = super.hashCode()

    fun toDomain(
        repository: Repository,
    ): File {

        return File(
            path = (this.path ?: "")
        ).apply {
            this.id = this@FileEntity.id?.toString()
        }
    }

    override fun toString(): String =
        "FileEntity(id=$id, path='$path')"
}

internal fun File.toEntity(
    //repository: RepositoryEntity,
): FileEntity =
    FileEntity(
        iid = this.iid,
        path = this.path,
        //repository = repository,
    ).apply {
        this.id = this@toEntity.id?.trim()?.toLongOrNull()
    }
