// package com.inso_world.binocular.cli.entity
//
// import com.inso_world.binocular.cli.archive.ProjectMemberPojo
// import jakarta.persistence.Column
// import jakarta.persistence.Entity
// import jakarta.persistence.FetchType
// import jakarta.persistence.GeneratedValue
// import jakarta.persistence.GenerationType
// import jakarta.persistence.Id
// import jakarta.persistence.JoinColumn
// import jakarta.persistence.JoinTable
// import jakarta.persistence.ManyToMany
// import jakarta.persistence.ManyToOne
// import jakarta.persistence.Table
// import jakarta.persistence.UniqueConstraint
// import java.time.LocalDateTime
// import java.util.Objects
//
// @Entity
// @Table(
//    name = "project_members",
//    uniqueConstraints = [
//        UniqueConstraint(columnNames = ["project_id", "email"]),
//    ],
// )
// data class ProjectMember(
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    val id: Long? = null,
//    @Column(name = "access_level", nullable = false)
//    val accessLevel: Int,
//    @Column(name = "source_type", nullable = false)
//    val sourceType: String,
//    @Column(name = "user_id", nullable = false)
//    val userId: Long,
//    @Column(name = "created_at", nullable = false)
//    val createdAt: LocalDateTime,
//    @Column(name = "updated_at", nullable = false)
//    val updatedAt: LocalDateTime,
//    @Column(name = "created_by_id")
//    val createdById: Long? = null,
//    @Column(name = "username", nullable = false, unique = true)
//    val username: String,
//    @ManyToMany(targetEntity = Project::class, fetch = FetchType.LAZY)
//    @JoinTable(
//        name = "member_project",
//        joinColumns = [JoinColumn(name = "project_members_id", nullable = false)],
//        inverseJoinColumns = [JoinColumn(name = "project_id", nullable = false)],
//        uniqueConstraints = [UniqueConstraint(columnNames = ["project_id", "project_members_id"])],
//    )
//    var projects: MutableSet<Project> = emptySet<Project>().toMutableSet(),
//    @ManyToOne(fetch = FetchType.LAZY, optional = true)
//    @JoinColumn(name = "fk_user")
//    var user: User? = null,
// ) {
//    override fun toString(): String =
//        "ProjectMember(id=$id, accessLevel=$accessLevel, sourceType='$sourceType', userId=$userId, createdAt=$createdAt, updatedAt=$updatedAt, createdById=$createdById, username=$username)"
//
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//
//        other as ProjectMember
//
//        if (id != other.id) return false
//        if (accessLevel != other.accessLevel) return false
//        if (userId != other.userId) return false
//        if (createdById != other.createdById) return false
//        if (sourceType != other.sourceType) return false
//        if (createdAt != other.createdAt) return false
//        if (updatedAt != other.updatedAt) return false
//        if (username != other.username) return false
//
//        return true
//    }
//
//    override fun hashCode(): Int {
//        var result = id?.let { Objects.hash(it) } ?: 0
//        result = 31 * result + Objects.hash(accessLevel)
//        result = 31 * result + Objects.hash(userId)
//        result = 31 * result + (createdById?.let { Objects.hash(it) } ?: 0)
//        result = 31 * result + Objects.hash(sourceType)
//        result = 31 * result + Objects.hash(createdAt)
//        result = 31 * result + Objects.hash(updatedAt)
//        result = 31 * result + Objects.hash(username)
//        return result
//    }
// }
//
// fun ProjectMemberPojo.toEntity(): ProjectMember =
//    ProjectMember(
//        accessLevel = this.accessLevel,
//        sourceType = this.sourceType,
//        userId = this.userId,
//        createdAt = this.createdAt,
//        updatedAt = this.updatedAt,
//        createdById = this.createdById,
//        username = this.user.username ?: throw IllegalStateException("Username null"),
//    )
