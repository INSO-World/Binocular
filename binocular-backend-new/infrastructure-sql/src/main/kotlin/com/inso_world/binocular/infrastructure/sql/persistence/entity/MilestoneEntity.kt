package com.inso_world.binocular.infrastructure.sql.persistence.entity

import com.inso_world.binocular.infrastructure.sql.persistence.converter.KotlinUuidConverter
import com.inso_world.binocular.model.Milestone
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table

/**
 * SQL-specific Milestone entity.
 */
@Entity
@Table(name = "milestones")
internal data class MilestoneEntity(
    @Id
    var id: Long? = null,
    @Convert(KotlinUuidConverter::class)
    var iid: Milestone.Id,
    var platformIid: Int? = null,
    var title: String? = null,
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    @Column(name = "created_at")
    var createdAt: String? = null,
    @Column(name = "updated_at")
    var updatedAt: String? = null,
    @Column(name = "start_date")
    var startDate: String? = null,
    @Column(name = "due_date")
    var dueDate: String? = null,
    var state: String? = null,
    var expired: Boolean? = null,
    @Column(name = "web_url")
    var webUrl: String? = null,
    @ManyToMany(mappedBy = "milestones")
    var issues: MutableList<IssueEntity> = mutableListOf(),
)
