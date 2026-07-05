package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.service.IdentityResolutionPort
import com.inso_world.binocular.infrastructure.sql.persistence.entity.AccountEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.IdentityMatchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.repository.AccountRepository
import com.inso_world.binocular.infrastructure.sql.persistence.repository.DeveloperRepository
import com.inso_world.binocular.infrastructure.sql.persistence.repository.IdentityMatchRepository
import com.inso_world.binocular.model.IdentityMatch
import com.inso_world.binocular.model.MatchLayer
import com.inso_world.binocular.model.MatchStatus
import org.springframework.stereotype.Service
import java.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.springframework.transaction.annotation.Transactional

private const val HIGH_CONFIDENCE = 0.90
private val GITHUB_NOREPLY_REGEX =
    Regex("""^(\d+)\+(.+)@users\.noreply\.github\.com$""")

@OptIn(ExperimentalUuidApi::class)
@Service
internal class IdentityResolutionPortImpl(
    private val developerRepository: DeveloperRepository,
    private val accountRepository: AccountRepository,
    private val identityMatchRepository: IdentityMatchRepository,
) : IdentityResolutionPort {

    companion object {
        private val logger by logger()
    }
    @Transactional
    override fun resolveIdentities(repositoryId: Long) {
        logger.info("Starting identity resolution for repository $repositoryId")

        val accounts = accountRepository.findAll()
        if (accounts.isEmpty()) {
            logger.warn(
                "No accounts found — skipping identity resolution. " +
                        "Ensure GitHub accounts are populated first."
            )
            return
        }

        val developers = developerRepository.findAllByRepository_Id(repositoryId).toList()
        logger.info("Resolving ${developers.size} developers against ${accounts.size} accounts")

        var matched = 0
        var pending = 0
        var unresolved = 0

        for (developer in developers) {
            val existing = identityMatchRepository.findByDeveloper_Id(developer.id!!)
            if (existing != null) {
                logger.debug("Skipping ${developer.email} — already matched")
                continue
            }

            val result = runPipeline(developer.name, developer.email, accounts)

            if (result != null) {
                identityMatchRepository.save(
                    IdentityMatchEntity(
                        iid = IdentityMatch.Id(Uuid.random()),
                        developer = developer,
                        account = result.account,
                        matchLayer = result.layer.name,
                        confidence = result.confidence,
                        signals = result.signals.joinToString(","),
                        status = result.status.name,
                        reviewedBy = null,
                        resolvedAt = Instant.now(),
                    )
                )
                if (result.status == MatchStatus.AUTO_MATCHED) matched++ else pending++
                logger.debug(
                    "Matched '${developer.name} <${developer.email}>' → " +
                            "'${result.account.login}' via ${result.layer} " +
                            "(confidence: ${"%.2f".format(result.confidence)})"
                )
            } else {
                unresolved++
                logger.debug("No match for '${developer.name} <${developer.email}>'")
            }
        }

        logger.info(
            "Identity resolution complete: $matched auto-matched, " +
                    "$pending pending review, $unresolved unresolved"
        )
    }

    private data class MatchResult(
        val account: AccountEntity,
        val layer: MatchLayer,
        val confidence: Double,
        val signals: List<String>,
        val status: MatchStatus,
    )

    private fun runPipeline(
        name: String,
        email: String,
        accounts: List<AccountEntity>,
    ): MatchResult? {
        val emailLower = email.lowercase()
        return layer1ExactEmail(emailLower, accounts)
            ?: layer2NormalizedEmail(emailLower, accounts)
            ?: layer3UsernameInference(emailLower, accounts)
            ?: layer4NameSimilarity(name, accounts)
    }

    private fun layer1ExactEmail(
        email: String,
        accounts: List<AccountEntity>,
    ): MatchResult? {
        for (account in accounts) {
            if (account.email?.lowercase() == email) {
                return result(account, MatchLayer.EXACT_EMAIL, 1.0,
                    listOf("exact_email:$email"))
            }
            val noreply = GITHUB_NOREPLY_REGEX.find(email)
            if (noreply != null) {
                val login = noreply.groupValues[2]
                if (account.login.lowercase() == login.lowercase()) {
                    return result(account, MatchLayer.EXACT_EMAIL, 1.0,
                        listOf("noreply_login:$login"))
                }
            }
        }
        return null
    }

    private fun layer2NormalizedEmail(
        email: String,
        accounts: List<AccountEntity>,
    ): MatchResult? {
        val normalized = email.replace(Regex("""\+[^@]+"""), "")
        for (account in accounts) {
            val accountNormalized = account.email
                ?.lowercase()
                ?.replace(Regex("""\+[^@]+"""), "")
            if (accountNormalized == normalized) {
                return result(account, MatchLayer.NORMALIZED_EMAIL, 0.95,
                    listOf("normalized_email:$normalized"))
            }
        }
        return null
    }

    private fun layer3UsernameInference(
        email: String,
        accounts: List<AccountEntity>,
    ): MatchResult? {
        val localPart = email.substringBefore("@").lowercase()
        if (localPart.isEmpty()) return null

        var bestAccount: AccountEntity? = null
        var bestScore = 0.0

        for (account in accounts) {
            val score = jaroWinkler(localPart, account.login.lowercase())
            if (score > 0.92 && score > bestScore) {
                bestScore = score
                bestAccount = account
            }
        }

        return bestAccount?.let {
            val confidence = (0.70 + (bestScore - 0.92) * 4).coerceAtMost(0.89)
            result(it, MatchLayer.USERNAME_INFERENCE, confidence,
                listOf("jaro_winkler_login:${"%.3f".format(bestScore)}"))
        }
    }

    private fun layer4NameSimilarity(
        name: String,
        accounts: List<AccountEntity>,
    ): MatchResult? {
        val normalizedName = name.lowercase().trim()
        var bestAccount: AccountEntity? = null
        var bestScore = 0.0

        for (account in accounts) {
            val accountName = (account.name ?: account.login).lowercase().trim()
            val score = jaroWinkler(normalizedName, accountName)
            if (score > 0.88 && score > bestScore) {
                bestScore = score
                bestAccount = account
            }
        }

        return bestAccount?.let {
            val confidence = (0.60 + (bestScore - 0.88) * 2.5).coerceAtMost(0.89)
            result(it, MatchLayer.NAME_SIMILARITY, confidence,
                listOf("jaro_winkler_name:${"%.3f".format(bestScore)}"))
        }
    }

    private fun result(
        account: AccountEntity,
        layer: MatchLayer,
        confidence: Double,
        signals: List<String>,
    ) = MatchResult(
        account = account,
        layer = layer,
        confidence = confidence,
        signals = signals,
        status = if (confidence >= HIGH_CONFIDENCE) MatchStatus.AUTO_MATCHED
        else MatchStatus.PENDING_REVIEW,
    )

    private fun jaroWinkler(s1: String, s2: String): Double {
        if (s1 == s2) return 1.0
        if (s1.isEmpty() || s2.isEmpty()) return 0.0

        val matchDist = maxOf(s1.length, s2.length) / 2 - 1
        val s1Matches = BooleanArray(s1.length)
        val s2Matches = BooleanArray(s2.length)
        var matches = 0
        var transpositions = 0

        for (i in s1.indices) {
            val lo = maxOf(0, i - matchDist)
            val hi = minOf(i + matchDist + 1, s2.length)
            for (j in lo until hi) {
                if (s2Matches[j] || s1[i] != s2[j]) continue
                s1Matches[i] = true
                s2Matches[j] = true
                matches++
                break
            }
        }

        if (matches == 0) return 0.0

        var k = 0
        for (i in s1.indices) {
            if (!s1Matches[i]) continue
            while (!s2Matches[k]) k++
            if (s1[i] != s2[k]) transpositions++
            k++
        }

        val jaro = (matches.toDouble() / s1.length +
                matches.toDouble() / s2.length +
                (matches - transpositions / 2.0) / matches) / 3.0

        var prefix = 0
        for (i in 0 until minOf(4, s1.length, s2.length)) {
            if (s1[i] == s2[i]) prefix++ else break
        }

        return jaro + prefix * 0.1 * (1 - jaro)
    }
}
