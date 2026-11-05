package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.infrastructure.arangodb.service.IdentityResolutionArangoService
import com.inso_world.binocular.model.MatchStatus
import com.inso_world.binocular.web.graphql.model.MatchDto
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
internal class MatchController(
    private val resolutionService: IdentityResolutionArangoService,
) {
    @QueryMapping(name = "matches")
    fun matches(): List<MatchDto> = resolutionService.allMatches().map { it.toMatchDto() }

    @QueryMapping(name = "pendingMatches")
    fun pendingMatches(): List<MatchDto> = resolutionService.pendingMatches().map { it.toMatchDto() }

    @MutationMapping(name = "reviewMatch")
    fun reviewMatch(
        @Argument matchId: String,
        @Argument status: String,
        @Argument reviewedBy: String,
    ): MatchDto {
        val decision = MatchStatus.valueOf(status) // throws IllegalArgumentException on invalid input
        resolutionService.reviewMatch(matchId, decision, reviewedBy)
        return resolutionService.allMatches()
            .first { it["matchId"] == matchId }
            .toMatchDto()
    }

    @MutationMapping(name = "undoReview")
    fun undoReview(@Argument matchId: String): MatchDto {
        resolutionService.undoReview(matchId)
        return resolutionService.allMatches()
            .first { it["matchId"] == matchId }
            .toMatchDto()
    }

    private fun Map<String, Any?>.toMatchDto() = MatchDto(
        matchId = this["matchId"] as? String,
        accountLogin = this["accountLogin"] as? String,
        accountName = this["accountName"] as? String,
        userGitSignature = this["userGitSignature"] as? String,
        matchLayer = this["matchLayer"] as? String,
        confidence = (this["confidence"] as? Number)?.toDouble(),
        status = this["status"] as? String,
        signals = this["signals"] as? String,
        reviewedBy = this["reviewedBy"] as? String,
        resolvedAt = this["resolvedAt"] as? String,
    )
}
