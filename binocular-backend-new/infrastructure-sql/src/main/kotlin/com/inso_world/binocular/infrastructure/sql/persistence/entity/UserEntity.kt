package com.inso_world.binocular.infrastructure.sql.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank

/**
 * SQL-specific User entity.
 */
@Entity
@Table(name = "users")
internal data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "git_signature")
    @field:NotBlank
    var gitSignature: String,
) {
    //    @ManyToMany(mappedBy = "users")
//    var commits: MutableList<CommitEntity> = mutableListOf()

//    @ManyToMany(mappedBy = "users")
//    var issues: MutableList<IssueEntity> = mutableListOf()

    // Keep the original relationship for the complex ternary relationship
    //    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    //    var commitFileConnections: MutableList<CommitFileUserConnectionEntity> = mutableListOf()

    // No-arg constructor required by JPA
//    constructor()
//
//    // Secondary constructor for convenience
//    constructor(
//        id: String? = null,
//        gitSignature: String,
//        commits: MutableList<CommitEntity> = mutableListOf(),
// //        issues: MutableList<IssueEntity> = mutableListOf(),
//        //        commitFileConnections: MutableList<CommitFileUserConnectionEntity> = mutableListOf(),
//    ) {
//        this.id = id?.toLong()
//        this.gitSignature = gitSignature
// //        this.commits = commits
// //        this.issues = issues
//        //        this.commitFileConnections = commitFileConnections
//    }
}
