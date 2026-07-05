package com.inso_world.binocular.infrastructure.sql.persistence.entity

import com.inso_world.binocular.infrastructure.sql.persistence.converter.KotlinUuidConverter
import com.inso_world.binocular.model.IdentityMatch
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Entity
@Table(
    name = "identity_matches",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "account_id"]),
    ],
)
internal data class IdentityMatchEntity(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var developer: DeveloperEntity,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    var account: AccountEntity,
    @Column(name = "match_layer", nullable = false)
    var matchLayer: String,
    @Column(nullable = false)
    var confidence: Double,
    @Column(nullable = true)
    var signals: String? = null,
    @Column(nullable = false)
    var status: String,
    @Column(name = "reviewed_by", nullable = true)
    var reviewedBy: String? = null,
    @Column(name = "resolved_at", nullable = true)
    var resolvedAt: Instant? = null,
    @Column(nullable = false, updatable = false, unique = true)
    @Convert(converter = KotlinUuidConverter::class)
    val iid: IdentityMatch.Id,
) : AbstractEntity<Long, IdentityMatchEntity.Key>() {

    data class Key(val userId: Long?, val accountId: Long?)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Long? = null

    override val uniqueKey: Key
        get() = Key(userId = developer.id, accountId = account.id)

    override fun equals(other: Any?): Boolean = super.equals(other)
    override fun hashCode(): Int = super.hashCode()
}
