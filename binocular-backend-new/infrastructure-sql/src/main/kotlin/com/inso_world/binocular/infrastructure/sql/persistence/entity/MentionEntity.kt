package com.inso_world.binocular.infrastructure.sql.persistence.entity

import jakarta.persistence.*
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
