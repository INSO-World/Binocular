package com.inso_world.binocular.infrastructure.sql.persistence.entity

import com.inso_world.binocular.model.Project
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank

@Entity
@Table(name = "projects")
internal data class ProjectEntity(
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) var id: Long? = null,
    @Column(nullable = false, unique = true) @field:NotBlank val name: String,
    @Column(nullable = true, unique = false) val description: String? = null,
//    @ManyToMany(mappedBy = "projects", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
//    var members: MutableSet<ProjectMember> = mutableSetOf(),
//    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "project", orphanRemoval = true)
//    var feature: ProjectFeature? = null,
//    @OneToMany(
//        fetch = FetchType.LAZY,
//        cascade = [CascadeType.ALL],
//        mappedBy = "project",
//        orphanRemoval = true,
//    ) var issues: MutableSet<IssueEntity> = mutableSetOf(),
//    @OneToMany(
//        fetch = FetchType.LAZY,
//        cascade = [CascadeType.ALL],
//        mappedBy = "project",
//        orphanRemoval = true,
//    ) var mergeRequests: MutableSet<MergeRequest> = mutableSetOf(),
    @OneToOne(
        fetch = FetchType.LAZY,
        cascade = [CascadeType.REMOVE, CascadeType.PERSIST],
        optional = true,
        mappedBy = "project",
    )
    @JoinColumn(name = "fk_repository", referencedColumnName = "id")
    var repo: RepositoryEntity? = null,
) : AbstractEntity() {
    //    fun addMember(pm: ProjectMember) {
//        this.members.add(pm)
//        pm.projects.add(this)
//    }

//    fun addIssue(e: Issue) {
//        this.issues.add(e)
//        e.project = this
//    }

    override fun toString(): String = "Project(id=$id, name=$name, description=$description, repo=$repo)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProjectEntity

//        if (id != other.id) return false
        if (name != other.name) return false
        if (description != other.description) return false
//        if (repo?.id != other.repo?.id) return false

        return true
    }

    override fun hashCode(): Int = super.hashCode()

    override fun uniqueKey(): String = this.name

    fun toDomain(): Project =
        Project(
            id = this.id?.toString(),
            name = this.name,
            description = this.description,
            repo = null,
        )
}

internal fun Project.toEntity(): ProjectEntity =
    ProjectEntity(
        id = this.id?.toLong(),
        name = this.name,
        description = this.description,
        repo = null,
    )
