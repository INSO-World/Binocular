// package com.inso_world.binocular.cli.entity
//
// import com.inso_world.binocular.cli.archive.MergeRequestPojo
// import jakarta.persistence.CascadeType
// import jakarta.persistence.Column
// import jakarta.persistence.Entity
// import jakarta.persistence.FetchType
// import jakarta.persistence.GeneratedValue
// import jakarta.persistence.GenerationType
// import jakarta.persistence.Id
// import jakarta.persistence.JoinColumn
// import jakarta.persistence.Lob
// import jakarta.persistence.ManyToOne
// import jakarta.persistence.OneToMany
// import jakarta.persistence.Table
// import jakarta.persistence.UniqueConstraint
// import java.time.LocalDateTime
//
// @Entity
// @Table(name = "merge_requests", uniqueConstraints = [UniqueConstraint(columnNames = ["iid", "fk_project_id"])])
// data class MergeRequest(
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    val id: Long? = null,
//    @Column(nullable = false)
//    val targetBranchName: String,
//    @ManyToOne(fetch = FetchType.LAZY)
//    var targetBranch: Branch? = null,
//    @Column(nullable = false)
//    val sourceBranchName: String,
//    @ManyToOne(fetch = FetchType.LAZY)
//    var sourceBranch: Branch? = null,
//    @Column(name = "created_at", nullable = false)
//    val createdAt: LocalDateTime,
//    @Column(name = "updated_at", nullable = false)
//    val updatedAt: LocalDateTime,
//    @Column(nullable = false)
//    val title: String,
//    @Column(nullable = false)
//    val iid: Int,
//    @Lob
//    @Column(nullable = false, columnDefinition = "TEXT")
//    val description: String,
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "fk_project_id", referencedColumnName = "id")
//    var project: Project? = null,
//    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "mergeRequest", orphanRemoval = true)
//    val timelogs: MutableSet<TimeLog> = emptySet<TimeLog>().toMutableSet(),
// ) {
//    fun addTimelog(tl: TimeLog) {
//        timelogs.add(tl)
//        tl.mergeRequest = this
//    }
//
//    fun addSourceBranch(branch: Branch) {
//        branch.mergeRequests.add(this)
//        this.sourceBranch = branch
//    }
//
//    fun addTargetBranch(branch: Branch) {
//        branch.mergeRequests.add(this)
//        this.targetBranch = branch
//    }
//
//    override fun toString(): String =
//        "MergeRequest(id=$id, targetBranch='$targetBranch', sourceBranch='$sourceBranch', createdAt=$createdAt, updatedAt=$updatedAt, title='$title', iid=$iid, description='$description')"
// }
//
// fun MergeRequestPojo.toEntity(): MergeRequest =
//    MergeRequest(
// //        id = this.id,
//        targetBranchName = this.targetBranch,
//        sourceBranchName = this.sourceBranch,
//        createdAt = this.createdAt,
//        updatedAt = this.updatedAt,
//        title = this.title,
//        description = this.description,
//        iid = this.iid,
//    )
