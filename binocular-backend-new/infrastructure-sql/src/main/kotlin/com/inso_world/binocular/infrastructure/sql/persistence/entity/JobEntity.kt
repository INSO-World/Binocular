// package com.inso_world.binocular.infrastructure.sql.persistence.entity
//
// import jakarta.persistence.Column
// import jakarta.persistence.Entity
// import jakarta.persistence.FetchType
// import jakarta.persistence.Id
// import jakarta.persistence.JoinColumn
// import jakarta.persistence.ManyToOne
// import jakarta.persistence.Table
// import jakarta.persistence.Temporal
// import jakarta.persistence.TemporalType
// import java.time.LocalDateTime
//
// /**
// * SQL-specific Job entity for storing build jobs.
// */
// @Entity
// @Table(name = "jobs")
// class JobEntity {
//    @Id
//    var id: String? = null
//
//    var name: String? = null
//    var status: String? = null
//    var stage: String? = null
//
//    @Column(name = "created_at")
//    @Temporal(TemporalType.TIMESTAMP)
//    var createdAt: LocalDateTime? = null
//
//    @Column(name = "finished_at")
//    @Temporal(TemporalType.TIMESTAMP)
//    var finishedAt: LocalDateTime? = null
//
//    @Column(name = "web_url")
//    var webUrl: String? = null
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "build_id")
//    var build: BuildEntity? = null
//
//    // No-arg constructor required by JPA
//    constructor()
//
//    // Secondary constructor for convenience
//    constructor(
//        id: String? = null,
//        name: String? = null,
//        status: String? = null,
//        stage: String? = null,
//        createdAt: LocalDateTime? = null,
//        finishedAt: LocalDateTime? = null,
//        webUrl: String? = null,
//        build: BuildEntity? = null,
//    ) {
//        this.id = id
//        this.name = name
//        this.status = status
//        this.stage = stage
//        this.createdAt = createdAt
//        this.finishedAt = finishedAt
//        this.webUrl = webUrl
//        this.build = build
//    }
// }
