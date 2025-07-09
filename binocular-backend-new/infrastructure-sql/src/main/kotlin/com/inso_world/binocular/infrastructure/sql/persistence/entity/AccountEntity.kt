// package com.inso_world.binocular.infrastructure.sql.persistence.entity
//
// import com.inso_world.binocular.model.Platform
// import jakarta.persistence.Column
// import jakarta.persistence.Entity
// import jakarta.persistence.EnumType
// import jakarta.persistence.Enumerated
// import jakarta.persistence.Id
// import jakarta.persistence.Table
//
// /**
// * SQL-specific Account entity.
// */
// @Entity
// @Table(name = "accounts")
// data class AccountEntity(
//    @Id
//    var id: Long? = null,
//    @Enumerated(EnumType.STRING)
//    var platform: Platform? = null,
//    var login: String? = null,
//    var name: String? = null,
//    @Column(name = "avatar_url")
//    var avatarUrl: String? = null,
//    var url: String? = null,
// //    @ManyToMany
// //    @JoinTable(
// //        name = "issue_account_connections",
// //        joinColumns = [JoinColumn(name = "account_id")],
// //        inverseJoinColumns = [JoinColumn(name = "issue_id")],
// //    )
// //    var issues: MutableList<IssueEntity> = mutableListOf(),
// //    @ManyToMany
// //    @JoinTable(
// //        name = "merge_request_account_connections",
// //        joinColumns = [JoinColumn(name = "account_id")],
// //        inverseJoinColumns = [JoinColumn(name = "merge_request_id")],
// //    )
// //    var mergeRequests: MutableList<MergeRequestEntity> = mutableListOf(),
// //    @ManyToMany
// //    @JoinTable(
// //        name = "note_account_connections",
// //        joinColumns = [JoinColumn(name = "account_id")],
// //        inverseJoinColumns = [JoinColumn(name = "note_id")],
// //    )
// //    var notes: MutableList<NoteEntity> = mutableListOf(),
// )
