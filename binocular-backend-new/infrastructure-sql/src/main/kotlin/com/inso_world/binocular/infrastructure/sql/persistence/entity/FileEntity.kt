// package com.inso_world.binocular.infrastructure.sql.persistence.entity
//
// import jakarta.persistence.Column
// import jakarta.persistence.Entity
// import jakarta.persistence.Id
// import jakarta.persistence.JoinColumn
// import jakarta.persistence.JoinTable
// import jakarta.persistence.ManyToMany
// import jakarta.persistence.Table
//
// /**
// * SQL-specific File entity.
// */
// @Entity
// @Table(name = "files")
// class FileEntity {
//    @Id
//    var id: String? = null
//
//    lateinit var path: String
//
//    @Column(name = "web_url")
//    lateinit var webUrl: String
//
//    @Column(name = "max_length")
//    var maxLength: Int? = null
//
//    @ManyToMany
//    @JoinTable(
//        name = "branch_file_connections",
//        joinColumns = [JoinColumn(name = "file_id")],
//        inverseJoinColumns = [JoinColumn(name = "branch_id")],
//    )
//    var branches: MutableList<BranchEntity> = mutableListOf()
//
//    @ManyToMany(mappedBy = "files")
//    var commits: MutableList<CommitEntity> = mutableListOf()
//
//    @ManyToMany
//    @JoinTable(
//        name = "module_file_connections",
//        joinColumns = [JoinColumn(name = "file_id")],
//        inverseJoinColumns = [JoinColumn(name = "module_id")],
//    )
//    var modules: MutableList<ModuleEntity> = mutableListOf()
//
//    @ManyToMany
//    @JoinTable(
//        name = "branch_file_file_connections",
//        joinColumns = [JoinColumn(name = "file_id")],
//        inverseJoinColumns = [JoinColumn(name = "branch_file_id")],
//    )
//    var outgoingFiles: MutableList<FileEntity> = mutableListOf()
//
//    @ManyToMany(mappedBy = "outgoingFiles")
//    var incomingFiles: MutableList<FileEntity> = mutableListOf()
//
//    // Keep the original relationship for the complex ternary relationship
// //    @OneToMany(mappedBy = "file", cascade = [CascadeType.ALL], orphanRemoval = true)
// //    var commitUserConnections: MutableList<CommitFileUserConnectionEntity> = mutableListOf()
//
//    // No-arg constructor required by JPA
//    constructor()
//
//    // Secondary constructor for convenience
//    constructor(
//        id: String?,
//        path: String,
//        webUrl: String,
//        maxLength: Int? = null,
//        branches: MutableList<BranchEntity> = mutableListOf(),
//        commits: MutableList<CommitEntity> = mutableListOf(),
//        modules: MutableList<ModuleEntity> = mutableListOf(),
//        outgoingFiles: MutableList<FileEntity> = mutableListOf(),
//        incomingFiles: MutableList<FileEntity> = mutableListOf(),
// //        commitUserConnections: MutableList<CommitFileUserConnectionEntity> = mutableListOf(),
//    ) {
//        this.id = id
//        this.path = path
//        this.webUrl = webUrl
//        this.maxLength = maxLength
//        this.branches = branches
//        this.commits = commits
//        this.modules = modules
//        this.outgoingFiles = outgoingFiles
//        this.incomingFiles = incomingFiles
// //        this.commitUserConnections = commitUserConnections
//    }
// }
