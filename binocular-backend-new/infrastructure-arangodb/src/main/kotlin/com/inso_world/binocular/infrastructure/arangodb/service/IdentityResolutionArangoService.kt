package com.inso_world.binocular.infrastructure.arangodb.service

import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.AccountEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.UserEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.AccountUserConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.AccountRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.UserRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges.AccountUserConnectionRepository
import com.inso_world.binocular.model.MatchLayer
import com.inso_world.binocular.model.MatchStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

private const val HIGH_CONFIDENCE = 0.90
private const val MEDIUM_CONFIDENCE = 0.60
private val GITHUB_NOREPLY_REGEX = Regex("""^(\d+)\+(.+)@users\.noreply\.github\.com$""")

// Strips a "+alias" local-part suffix, e.g. john+work@example.com -> john@example.com
private val PLUS_ALIAS_REGEX = Regex("""\+[^@]+""")

// Configurable table of legacy -> canonical domains used by Layer 2 normalisation.
// In a full deployment this would be externalized (e.g. application config) rather
// than hardcoded; kept as a simple map here to keep the matching logic identical
// in shape to the relational implementation.
private val DOMAIN_ALIASES: Map<String, String> = mapOf(
    // "oldcompany.example.com" to "newcompany.example.com",
)

@Service
class IdentityResolutionArangoService(
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
    private val connectionRepository: AccountUserConnectionRepository,
) {
    private val logger: Logger = LoggerFactory.getLogger(IdentityResolutionArangoService::class.java)


    fun resolveAll(clearExisting: Boolean = true): Map<String, Int> {
        val accounts = connectionRepository.findAllAccountsFlat()
        if (accounts.isEmpty()) {
            logger.warn("No accounts found - skipping identity resolution.")
            return mapOf("auto_matched" to 0, "pending_review" to 0, "low_confidence" to 0, "unresolved" to 0)
        }
        val users = connectionRepository.findAllUsersFlat()
        logger.info("Resolving ${users.size} users against ${accounts.size} accounts")

        if (clearExisting) {
            connectionRepository.clearAll()
            logger.info("Cleared existing account-user edges")
        }

        var matched = 0;
        var pending = 0;
        var lowConfidence = 0;
        var unresolved = 0

        for (user in users) {
            val gitSig = user["gitSignature"] as? String ?: continue
            val toId = user["id"] as? String ?: continue
            val name = Regex("""^(.+?)\s*<""").find(gitSig)?.groupValues?.get(1)?.trim()
            val email = Regex("""<([^>]+)>$""").find(gitSig)?.groupValues?.get(1)
            if (name == null || email == null) {
                unresolved++; continue
            }

            val result = runPipeline(name, email, accounts)
            if (result != null) {
                connectionRepository.insertMatch(
                    from = result.accountId,
                    to = toId,
                    layer = result.layer.name,
                    confidence = result.confidence,
                    status = result.status.name,
                    signals = result.signals.joinToString(","),
                )
                when (result.status) {
                    MatchStatus.AUTO_MATCHED -> matched++
                    MatchStatus.PENDING_REVIEW -> pending++
                    MatchStatus.LOW_CONFIDENCE -> lowConfidence++
                    else -> pending++
                }
            } else unresolved++
        }
        logger.info("Complete: $matched auto, $pending pending, $lowConfidence low-confidence, $unresolved unresolved")
        return mapOf(
            "auto_matched" to matched,
            "pending_review" to pending,
            "low_confidence" to lowConfidence,
            "unresolved" to unresolved,
        )
    }

    fun reviewMatch(matchKey: String, decision: MatchStatus, reviewedBy: String) {
        require(decision == MatchStatus.MANUALLY_CONFIRMED || decision == MatchStatus.MANUALLY_REJECTED) {
            "reviewMatch only accepts MANUALLY_CONFIRMED or MANUALLY_REJECTED, got $decision"
        }
        require(reviewedBy.isNotBlank()) { "reviewedBy must not be blank" }

        val current = connectionRepository.findMatchStatusAndConfidence(matchKey)
            ?: throw NoSuchElementException("No identity match found for id $matchKey")
        val currentStatus = current["status"] as? String

        check(currentStatus == MatchStatus.PENDING_REVIEW.name || currentStatus == MatchStatus.LOW_CONFIDENCE.name) {
            "Match $matchKey is not eligible for manual review (current status: $currentStatus). " +
                    "Only PENDING_REVIEW or LOW_CONFIDENCE matches can be manually confirmed or rejected."
        }

        connectionRepository.reviewMatch(
            matchKey = matchKey,
            status = decision.name,
            reviewedBy = reviewedBy,
            resolvedAt = Instant.now().toString(),
        )
    }

    fun undoReview(matchKey: String) {
        val current = connectionRepository.findMatchStatusAndConfidence(matchKey)
            ?: throw NoSuchElementException("No identity match found for id $matchKey")
        val currentStatus = current["status"] as? String

        check(currentStatus == MatchStatus.MANUALLY_CONFIRMED.name || currentStatus == MatchStatus.MANUALLY_REJECTED.name) {
            "Match $matchKey has not been manually reviewed (current status: $currentStatus); nothing to undo."
        }

        val confidence = (current["confidence"] as? Number)?.toDouble()
            ?: throw IllegalStateException("Match $matchKey has no confidence value")

        val restoredStatus = classify(confidence)
        connectionRepository.undoReview(matchKey, restoredStatus.name)
        logger.info("Undid review for match $matchKey, restored to $restoredStatus")
    }

    private data class MatchResult(
        val accountId: String,
        val layer: MatchLayer,
        val confidence: Double,
        val signals: List<String>,
        val status: MatchStatus,
    )

    private data class NormalizedEmail(
        val value: String,
        val aliasStripped: Boolean,
        val domainRemapped: Boolean,
    )

    private fun normalize(email: String): NormalizedEmail {
        val at = email.lastIndexOf('@')
        if (at < 0) return NormalizedEmail(email, aliasStripped = false, domainRemapped = false)
        val local = email.substring(0, at)
        val domain = email.substring(at + 1)
        val aliasStripped = PLUS_ALIAS_REGEX.containsMatchIn(local)
        val strippedLocal = local.replace(PLUS_ALIAS_REGEX, "")
        val canonicalDomain = DOMAIN_ALIASES[domain] ?: domain
        val domainRemapped = canonicalDomain != domain
        return NormalizedEmail("$strippedLocal@$canonicalDomain", aliasStripped, domainRemapped)
    }

    private fun normalizedConfidence(aliasStripped: Boolean, domainRemapped: Boolean): Double = when {
        domainRemapped && aliasStripped -> 0.90
        domainRemapped -> 0.93
        aliasStripped -> 0.99
        else -> 0.95
    }

    private fun runPipeline(name: String, email: String, accounts: List<Map<String, Any?>>): MatchResult? {
        val emailLower = email.lowercase()
        return layer1ExactEmail(emailLower, accounts)
            ?: layer2NormalizedEmail(emailLower, accounts)
            ?: layer3UsernameInference(emailLower, accounts)
            ?: layer4NameSimilarity(name, accounts)
    }

    private fun layer1ExactEmail(email: String, accounts: List<Map<String, Any?>>): MatchResult? {
        val noreply = GITHUB_NOREPLY_REGEX.find(email)
        for (a in accounts) {
            val accountEmail = (a["email"] as? String)?.lowercase()
            if (accountEmail != null && accountEmail == email) {
                return result(a["id"] as String, MatchLayer.EXACT_EMAIL, 1.0, listOf("exact_email:$email"))
            }
            if (noreply != null) {
                val login = noreply.groupValues[2]
                val aLogin = a["login"] as? String
                if (aLogin != null && aLogin.lowercase() == login.lowercase()) {
                    return result(a["id"] as String, MatchLayer.EXACT_EMAIL, 1.0, listOf("noreply_login:$login"))
                }
            }
        }
        return null
    }

    private fun layer2NormalizedEmail(email: String, accounts: List<Map<String, Any?>>): MatchResult? {
        val devNorm = normalize(email)
        for (a in accounts) {
            val accountEmailRaw = (a["email"] as? String)?.lowercase() ?: continue
            val accNorm = normalize(accountEmailRaw)
            if (accNorm.value == devNorm.value) {
                val aliasStripped = devNorm.aliasStripped || accNorm.aliasStripped
                val domainRemapped = devNorm.domainRemapped || accNorm.domainRemapped
                val confidence = normalizedConfidence(aliasStripped, domainRemapped)
                return result(
                    a["id"] as String,
                    MatchLayer.NORMALIZED_EMAIL,
                    confidence,
                    listOfNotNull(
                        "normalized_email:${devNorm.value}",
                        if (aliasStripped) "alias_stripped" else null,
                        if (domainRemapped) "domain_remapped" else null,
                    ),
                )
            }
        }
        return null
    }

    private fun layer3UsernameInference(email: String, accounts: List<Map<String, Any?>>): MatchResult? {
        val localPart = email.substringBefore("@").lowercase()
        if (localPart.isEmpty()) return null
        var best: Map<String, Any?>? = null;
        var bestScore = 0.0
        for (a in accounts) {
            val login = (a["login"] as? String)?.lowercase() ?: continue
            val score = jaroWinkler(localPart, login)
            if (score > 0.92 && score > bestScore) {
                bestScore = score; best = a
            }
        }
        return best?.let {
            val c = (0.70 + (bestScore - 0.92) * 4).coerceAtMost(0.89)
            result(
                it["id"] as String,
                MatchLayer.USERNAME_INFERENCE,
                c,
                listOf("jaro_winkler_login:${"%.3f".format(bestScore)}")
            )
        }
    }

    private fun layer4NameSimilarity(name: String, accounts: List<Map<String, Any?>>): MatchResult? {
        val nn = name.lowercase().trim()
        var best: Map<String, Any?>? = null;
        var bestScore = 0.0
        for (a in accounts) {
            val an = ((a["name"] as? String) ?: (a["login"] as? String) ?: continue).lowercase().trim()
            val score = jaroWinkler(nn, an)
            if (score > 0.88 && score > bestScore) {
                bestScore = score; best = a
            }
        }
        return best?.let {
            val c = (0.60 + (bestScore - 0.88) * 2.5).coerceAtMost(0.89)
            result(
                it["id"] as String,
                MatchLayer.NAME_SIMILARITY,
                c,
                listOf("jaro_winkler_name:${"%.3f".format(bestScore)}")
            )
        }
    }

    private fun classify(confidence: Double): MatchStatus = when {
        confidence >= HIGH_CONFIDENCE -> MatchStatus.AUTO_MATCHED
        confidence >= MEDIUM_CONFIDENCE -> MatchStatus.PENDING_REVIEW
        else -> MatchStatus.LOW_CONFIDENCE
    }

    private fun result(accountId: String, layer: MatchLayer, confidence: Double, signals: List<String>) =
        MatchResult(accountId, layer, confidence, signals, classify(confidence))

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

    fun allMatches(): List<Map<String, Any?>> = connectionRepository.findAllMatches()
    fun pendingMatches(): List<Map<String, Any?>> = connectionRepository.findPendingMatches()
}
