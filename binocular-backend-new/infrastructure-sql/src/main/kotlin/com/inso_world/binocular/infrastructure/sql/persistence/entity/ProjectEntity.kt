package com.inso_world.binocular.infrastructure.sql.persistence.entity

import com.inso_world.binocular.infrastructure.sql.persistence.converter.KotlinUuidConverter
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.validation.constraints.NotBlank

@Entity
@Table(name = "projects", uniqueConstraints = [UniqueConstraint(columnNames = ["name"])])
internal data class ProjectEntity(
    @Column(nullable = false, unique = true, updatable = false) @field:NotBlank val name: String,
    @Column(nullable = false, updatable = false, unique = true)
    @Convert(KotlinUuidConverter::class)
    val iid: Project.Id
) : AbstractEntity<Long, ProjectEntity.Key>() {

    @Column(nullable = true, unique = false, length = MAX_DESCRIPTION_LENGTH)
    var description: String? = null
        set(value) {
            require(value == null || value.length <= MAX_DESCRIPTION_LENGTH) {
                "Description must not exceed $MAX_DESCRIPTION_LENGTH characters."
            }
            field = value
        }

    data class Key(val name: String) // value object for lookups

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    override var id: Long? = null

    @Column(name = "fk_repository_iid", nullable = true, unique = true)
    @Convert(converter = KotlinUuidConverter::class)
    var repoId: Repository.Id? = null

    override fun toString(): String = "ProjectEntity(id=$id, iid=$iid, name=$name, description=$description, repoId=$repoId)"

    override val uniqueKey: ProjectEntity.Key
        get() = ProjectEntity.Key(this.name)

    // Entities compare by immutable identity only
    override fun equals(other: Any?) = super.equals(other)
    override fun hashCode(): Int = super.hashCode()

    fun toDomain(): Project = Project(
        name = this.name,
    ).apply {
        this.id = this@ProjectEntity.id?.toString()
        this.description = this@ProjectEntity.description
        this.repoId = this@ProjectEntity.repoId
    }

    companion object {
        private const val MAX_DESCRIPTION_LENGTH = 255
    }
}

internal fun Project.toEntity(): ProjectEntity = ProjectEntity(
    iid = this.iid,
    name = this@toEntity.name,
).apply {
    id = this@toEntity.id?.trim()?.toLongOrNull()
    description = this@toEntity.description
}
