// package com.inso_world.binocular.cli.entity
//
// import com.inso_world.binocular.cli.archive.ProjectFeaturePojo
// import jakarta.persistence.Column
// import jakarta.persistence.Entity
// import jakarta.persistence.FetchType
// import jakarta.persistence.Id
// import jakarta.persistence.JoinColumn
// import jakarta.persistence.OneToOne
// import jakarta.persistence.Table
// import java.time.LocalDateTime
//
// @Entity
// @Table(name = "project_features")
// data class ProjectFeature(
//    @Id
//    val id: Long? = null,
//    @OneToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "fk_project", referencedColumnName = "id")
//    val project: Project,
//    @Column(name = "created_at", nullable = false)
//    val createdAt: LocalDateTime,
//    @Column(name = "updated_at", nullable = false)
//    val updatedAt: LocalDateTime,
// )
//
// fun ProjectFeaturePojo.toEntity(project: Project): ProjectFeature =
//    ProjectFeature(
//        id = this.id,
//        createdAt = this.createdAt,
//        updatedAt = this.updatedAt,
//        project = project,
//    )
