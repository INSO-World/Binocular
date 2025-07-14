package com.inso_world.binocular.infrastructure.sql.persistence.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import java.util.Objects

@Entity
@Table(name = "projects")
internal data class ProjectEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
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
    ) var repo: RepositoryEntity? = null,
) {
    //    fun addMember(pm: ProjectMember) {
//        this.members.add(pm)
//        pm.projects.add(this)
//    }

//    fun addIssue(e: Issue) {
//        this.issues.add(e)
//        e.project = this
//    }

    override fun toString(): String = "Project(id=$id, name=$name, description=$description)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProjectEntity

//        if (id != other.id) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (repo != other.repo) return false

        return true
    }

    override fun hashCode(): Int = Objects.hash(id, name, description, repo)

//    fun addMergeRequest(it: MergeRequest) {
//        this.mergeRequests.add(it)
//        it.project = this
//    }
}

// fun ProjectPojo.toEntity(name: String): Project =
//    Project(
//        description = description,
//        name = name,
//    )
