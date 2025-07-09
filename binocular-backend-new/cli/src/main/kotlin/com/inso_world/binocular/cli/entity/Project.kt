// package com.inso_world.binocular.cli.entity

// import com.inso_world.binocular.cli.archive.ProjectPojo
// import com.inso_world.binocular.model.Project

//
// import com.inso_world.binocular.cli.archive.ProjectPojo
// import jakarta.persistence.CascadeType
// import jakarta.persistence.Column
// import jakarta.persistence.Entity
// import jakarta.persistence.FetchType
// import jakarta.persistence.GeneratedValue
// import jakarta.persistence.GenerationType
// import jakarta.persistence.Id
// import jakarta.persistence.ManyToMany
// import jakarta.persistence.OneToMany
// import jakarta.persistence.OneToOne
// import jakarta.persistence.Table
// import jakarta.validation.constraints.NotBlank
//
// @Entity
// @Table(name = "projects")
// data class Project(
//    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
//    @Column(nullable = false, unique = true) @field:NotBlank val name: String,
//    @Column(nullable = true, unique = false) val description: String? = null,
//    @ManyToMany(mappedBy = "projects", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
//    var members: MutableSet<ProjectMember> = mutableSetOf(),
//    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "project", orphanRemoval = true)
//    var feature: ProjectFeature? = null,
//    @OneToMany(
//        fetch = FetchType.LAZY,
//        cascade = [CascadeType.ALL],
//        mappedBy = "project",
//        orphanRemoval = true,
//    ) var issues: MutableSet<Issue> = mutableSetOf(),
//    @OneToMany(
//        fetch = FetchType.LAZY,
//        cascade = [CascadeType.ALL],
//        mappedBy = "project",
//        orphanRemoval = true,
//    ) var mergeRequests: MutableSet<MergeRequest> = mutableSetOf(),
//    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE], optional = true, mappedBy = "project")
//    var repo: Repository? = null,
// ) {
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//
//        other as Project
//
//        if (id != other.id) return false
//        if (description != other.description) return false
//
//        return true
//    }
//
//    override fun hashCode(): Int {
//        var result = id?.hashCode() ?: 0
//        result = 31 * result + (description?.hashCode() ?: 0)
//        return result
//    }
//
//    fun addMember(pm: ProjectMember) {
//        this.members.add(pm)
//        pm.projects.add(this)
//    }
//
//    fun addIssue(e: Issue) {
//        this.issues.add(e)
//        e.project = this
//    }
//
//    override fun toString(): String = "Project(id=$id, name=$name, description=$description)"
//
//    fun addMergeRequest(it: MergeRequest) {
//        this.mergeRequests.add(it)
//        it.project = this
//    }
// }
//
// fun ProjectPojo.toEntity(name: String): Project =
//    Project(
//        name = name,
//        description = description,
//    )
