package com.inso_world.binocular.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

enum class MatchLayer {
    EXACT_EMAIL,
    NORMALIZED_EMAIL,
    USERNAME_INFERENCE,
    NAME_SIMILARITY,
    MANUAL
}

enum class MatchStatus {
    AUTO_MATCHED,
    PENDING_REVIEW,
    LOW_CONFIDENCE,
    MANUALLY_CONFIRMED,
    MANUALLY_REJECTED
}

@OptIn(ExperimentalUuidApi::class)
data class IdentityMatch(
    val developer: Developer,
    val account: Account,
    val matchLayer: MatchLayer,
    val confidence: Double,
    val signals: List<String>,
    val status: MatchStatus,
    val reviewedBy: String? = null,
    val resolvedAt: java.time.Instant? = null,
) : AbstractDomainObject<IdentityMatch.Id, IdentityMatch.Key>(
    Id(Uuid.random())
) {
    @JvmInline
    value class Id(val value: Uuid)

    data class Key(val developerIid: Developer.Id, val accountIid: Account.Id)

    override val uniqueKey: Key
        get() = Key(developer.iid, account.iid)
}
