package com.inso_world.binocular.cli.entity

import com.inso_world.binocular.cli.archive.TimeLogPojo
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Table(
    name = "timelogs",
    uniqueConstraints = [
        UniqueConstraint(columnNames = arrayOf("id", "fk_project")),
        UniqueConstraint(columnNames = arrayOf("createdAt", "fk_project")),
//        UniqueConstraint(columnNames = arrayOf("spentAt", "fk_project")),
    ],
)
data class TimeLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "gitlab_id", nullable = true)
    val gitlabId: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_project", referencedColumnName = "id")
    var project: Project? = null,
    @Column(name = "time_spent", nullable = false)
    val timeSpent: Long,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_project_member", referencedColumnName = "id")
    var projectMember: ProjectMember? = null,
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime,
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime,
    @Column(name = "spent_at")
    val spentAt: LocalDateTime? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "fk_issue", referencedColumnName = "id")
    var issue: Issue? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "fk_merge_request", referencedColumnName = "id")
    var mergeRequest: MergeRequest? = null,
) {
    override fun hashCode(): Int = super.hashCode()

    @PrePersist
    fun prePersist() {
        if (!(issue == null).xor(mergeRequest == null)) {
            throw NullPointerException()
        }
    }

    override fun toString(): String =
        "TimeLog(spentAt=$spentAt, updatedAt=$updatedAt, createdAt=$createdAt, projectMember=$projectMember, timeSpent=$timeSpent, project=$project, id=$id)"
}

fun TimeLogPojo.toEntity(): TimeLog =
    TimeLog(
        gitlabId = id,
        timeSpent = timeSpent,
        createdAt = createdAt,
        updatedAt = updatedAt,
        spentAt = spentAt,
    )
