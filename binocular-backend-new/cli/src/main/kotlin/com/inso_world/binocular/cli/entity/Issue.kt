package com.inso_world.binocular.cli.entity

import com.inso_world.binocular.cli.archive.IssuePojo
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime
import kotlin.io.encoding.ExperimentalEncodingApi

@Entity
@Table(name = "issues", uniqueConstraints = [UniqueConstraint(columnNames = arrayOf("iid", "fk_project_id"))])
data class Issue(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = true)
    val gitlabId: Long? = null,
    @Column(nullable = false)
    val title: String,
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
//    TODO check why user 1771 is not in the data
    @JoinColumn(name = "fk_author_project_member_id", referencedColumnName = "id")
    var author: ProjectMember? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_project_id", referencedColumnName = "id")
    var project: Project? = null,
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime,
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime,
    @Column(nullable = true, columnDefinition = "TEXT")
    @Lob
    val description: String? = null,
    @Column(nullable = false)
    val iid: Int,
//    @Column(name = "updated_by_id")
//    val updatedById: Long? = null,
//    @Column(nullable = false)
//    val confidential: Boolean = false,
    @Column(name = "due_date")
    val dueDate: LocalDateTime? = null,
    @Column(name = "time_estimate")
    val timeEstimate: Int? = null,
    @Column(name = "closed_at")
    val closedAt: LocalDateTime? = null,
    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "issue", orphanRemoval = true)
    val timelogs: MutableSet<TimeLog> = emptySet<TimeLog>().toMutableSet(),
) {
    fun addTimelog(tl: TimeLog) {
        timelogs.add(tl)
        tl.issue = this
    }

    override fun toString(): String =
        "Issue(id=$id, title='$title', createdAt=$createdAt, updatedAt=$updatedAt, description=$description, iid=$iid, dueDate=$dueDate, timeEstimate=$timeEstimate, closedAt=$closedAt)"
}

@OptIn(ExperimentalEncodingApi::class)
fun IssuePojo.toEntity(): Issue =
    Issue(
        gitlabId = this.id,
        title = this.title,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        description = this.description,
        iid = this.iid,
        dueDate = this.dueDate,
        timeEstimate = this.timeEstimate,
        closedAt = this.closedAt,
    )
