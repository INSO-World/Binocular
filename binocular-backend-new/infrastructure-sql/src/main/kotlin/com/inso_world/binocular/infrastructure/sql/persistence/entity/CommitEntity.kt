package com.inso_world.binocular.infrastructure.sql.persistence.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Size
import java.time.LocalDateTime
import java.util.Objects

/**
 * SQL-specific Commit entity.
 */
@Entity
@Table(name = "commits")
internal data class CommitEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(unique = true)
    @field:NotEmpty
    @field:Size(min = 40, max = 40)
    var sha: String,
    @Temporal(TemporalType.TIMESTAMP)
    @field:PastOrPresent
    var date: LocalDateTime? = null,
    @Column(columnDefinition = "TEXT")
    var message: String? = null,
    @Column(name = "web_url")
    var webUrl: String? = null,
    var branch: String? = null,
    @ManyToMany(targetEntity = CommitEntity::class, cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinTable(
        name = "commit_parents",
        joinColumns = [JoinColumn(name = "commit_id", nullable = false)],
        inverseJoinColumns = [JoinColumn(name = "parent_id", nullable = true)],
    )
    var parents: List<CommitEntity> = emptyList(),
    @ManyToMany(targetEntity = BranchEntity::class, fetch = FetchType.EAGER)
    @JoinTable(
        name = "commit_branches",
        joinColumns = [JoinColumn(name = "commit_id", nullable = false)],
        inverseJoinColumns = [JoinColumn(name = "branch_id", nullable = true)],
    )
    val branches: MutableSet<BranchEntity> = mutableSetOf(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var repository: RepositoryEntity? = null,
) {
    //    @OneToOne(mappedBy = "commit", cascade = [CascadeType.ALL], orphanRemoval = true)
//    var stats: StatsEntity? = null

//    @ManyToMany
//    @JoinTable(
//        name = "commit_build_connections",
//        joinColumns = [JoinColumn(name = "commit_id")],
//        inverseJoinColumns = [JoinColumn(name = "build_id")],
//    )
//    var builds: MutableList<BuildEntity> = mutableListOf()

//    @ManyToMany
//    @JoinTable(
//        name = "commit_commit_connections",
//        joinColumns = [JoinColumn(name = "from_commit_id")],
//        inverseJoinColumns = [JoinColumn(name = "to_commit_id")],
//    )
//    var childCommits: MutableList<CommitEntity> = mutableListOf()

//    @ManyToMany(mappedBy = "childCommits")
//    var parentCommits: MutableList<CommitEntity> = mutableListOf()

//    @ManyToMany
//    @JoinTable(
//        name = "commit_file_connections",
//        joinColumns = [JoinColumn(name = "commit_id")],
//        inverseJoinColumns = [JoinColumn(name = "file_id")],
//    )
//    var files: MutableList<FileEntity> = mutableListOf()

//    @ManyToMany
//    @JoinTable(
//        name = "commit_module_connections",
//        joinColumns = [JoinColumn(name = "commit_id")],
//        inverseJoinColumns = [JoinColumn(name = "module_id")],
//    )
//    var modules: MutableList<ModuleEntity> = mutableListOf()

//    @ManyToMany
//    @JoinTable(
//        name = "commit_user_connections",
//        joinColumns = [JoinColumn(name = "commit_id")],
//        inverseJoinColumns = [JoinColumn(name = "user_id")],
//    )
//    var users: MutableList<UserEntity> = mutableListOf()

    // Keep the original relationship for the complex ternary relationship
    //    @OneToMany(mappedBy = "commit", cascade = [CascadeType.ALL], orphanRemoval = true)
    //    var fileUserConnections: MutableList<CommitFileUserConnectionEntity> = mutableListOf()

//    @ManyToMany(mappedBy = "commits")
//    var issues: MutableList<IssueEntity> = mutableListOf()

    // No-arg constructor required by JPA
//    constructor()
//
//    // Secondary constructor for convenience
//    constructor(
//        id: String? = null,
//        sha: String,
//        date: LocalDateTime? = null,
//        message: String? = null,
//        webUrl: String? = null,
//        branch: String? = null,
// //        stats: StatsEntity? = null,
// //        builds: MutableList<BuildEntity> = mutableListOf(),
//        childCommits: MutableList<CommitEntity> = mutableListOf(),
//        parentCommits: MutableList<CommitEntity> = mutableListOf(),
// //        files: MutableList<FileEntity> = mutableListOf(),
// //        modules: MutableList<ModuleEntity> = mutableListOf(),
//        users: MutableList<UserEntity> = mutableListOf(),
//        //        fileUserConnections: MutableList<CommitFileUserConnectionEntity> = mutableListOf(),
// //        issues: MutableList<IssueEntity> = mutableListOf(),
//    ) {
//        this.id = id?.toLong()
//        this.sha = sha
//        this.date = date
//        this.message = message
//        this.webUrl = webUrl
//        this.branch = branch
// //        this.stats = stats
// //        this.builds = builds
// //        this.childCommits = childCommits
// //        this.parentCommits = parentCommits
// //        this.files = files
// //        this.modules = modules
// //        this.users = users
//        //        this.fileUserConnections = fileUserConnections
// //        this.issues = issues
//    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommitEntity

        if (id != other.id) return false
        if (sha != other.sha) return false
        if (date != other.date) return false
        if (message != other.message) return false
        if (webUrl != other.webUrl) return false
        if (branch != other.branch) return false
        if (parents != other.parents) return false
        if (branches != other.branches) return false
        if (repository != other.repository) return false

        return true
    }

    override fun hashCode(): Int = Objects.hash(id, sha, date, message, webUrl)
}
