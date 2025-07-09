// package com.inso_world.binocular.infrastructure.sql.persistence.entity
//
// import jakarta.persistence.Entity
// import jakarta.persistence.FetchType
// import jakarta.persistence.GeneratedValue
// import jakarta.persistence.GenerationType
// import jakarta.persistence.Id
// import jakarta.persistence.JoinColumn
// import jakarta.persistence.ManyToOne
// import jakarta.persistence.Table
//
// /**
// * SQL-specific Label entity for storing issue and merge request labels.
// */
// @Entity
// @Table(name = "labels")
// data class LabelEntity(
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    var id: Long? = null,
//    var value: String,
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "issue_id")
//    var issue: IssueEntity? = null,
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "merge_request_id")
//    var mergeRequest: MergeRequestEntity? = null,
// ) {
//    // Default constructor for Hibernate
//    constructor() : this(null, "", null, null)
// }
