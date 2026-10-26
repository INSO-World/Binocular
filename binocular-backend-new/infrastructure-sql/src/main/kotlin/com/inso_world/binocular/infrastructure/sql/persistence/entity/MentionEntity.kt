package com.inso_world.binocular.infrastructure.sql.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import java.time.LocalDateTime

/**
 * SQL-specific Mention entity for storing issue mentions.
 */
@Entity
@Table(name = "mentions")
internal data class MentionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long? = null,
    var commit: String? = null,
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    var createdAt: LocalDateTime? = null,
    var closes: Boolean? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id")
    var issue: IssueEntity? = null,
)
