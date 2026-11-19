package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.core.service.narrative.NarrativeService
import com.inso_world.binocular.model.narrative.NarrativeGranularity
import com.inso_world.binocular.web.exception.ValidationException
import com.inso_world.binocular.web.graphql.mapper.GraphQlMapper
import com.inso_world.binocular.web.graphql.model.NarrativeSegmentDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * GraphQL entry point of the software history narrative.
 *
 * ## Purpose
 * Serves the root query `narrative`, which returns the correlated VCS, ITS and CI events of a period
 * as chronologically ordered segments. The composition itself is done by [NarrativeService]; this class
 * only translates between the GraphQL transport and the service.
 *
 * ## Semantics
 * - The period is half-open, `[since, until)`. A missing bound leaves that side open.
 * - If both bounds are given, `since` must be before `until`. A reversed or empty period is rejected with
 *   a [ValidationException] instead of being answered with an empty list, so that a client notices
 *   swapped arguments.
 * - The result is not paginated. A narrative is a closed sequence of gap-free segments, and a page of it
 *   would cut correlations between events. Clients that want less data request fewer fields (the
 *   segments without `events`) or a shorter period.
 *
 * ## Usage
 * ```graphql
 * query {
 *   narrative(granularity: MONTH, since: "2025-01-01T00:00:00Z", until: "2025-07-01T00:00:00Z") {
 *     start
 *     end
 *     summary
 *     commitCount
 *     events { id type title actor relatedEntityKeys }
 *   }
 * }
 * ```
 */
@Controller
@SchemaMapping(typeName = "NarrativeSegment")
internal class NarrativeController(
    private val narrativeService: NarrativeService,
    private val mapper: GraphQlMapper,
) {
    private val logger: Logger = LoggerFactory.getLogger(NarrativeController::class.java)

    /**
     * Composes the narrative of a period.
     *
     * @param granularity The time unit the narrative is cut into.
     * @param since Start of the period in epoch milliseconds, inclusive; `null` for no lower bound.
     * @param until End of the period in epoch milliseconds, exclusive; `null` for no upper bound.
     * @return The segments in chronological order; empty if the period contains no events.
     * @throws ValidationException if both bounds are given and [since] is not before [until].
     */
    @QueryMapping(name = "narrative")
    fun compose(
        @Argument granularity: NarrativeGranularity,
        @Argument since: Long?,
        @Argument until: Long?,
    ): List<NarrativeSegmentDto> {
        logger.info("Composing narrative with granularity={}, since={}, until={}", granularity, since, until)
        if (since != null && until != null && since >= until) {
            throw ValidationException(
                "Since must be before until",
                mapOf("parameter" to "since", "providedValue" to since, "until" to until),
            )
        }
        return narrativeService
            .compose(granularity, since?.let(::toUtc), until?.let(::toUtc))
            .map { mapper.toDto(it) }
    }

    /**
     * Converts a moment given in epoch milliseconds to the UTC date-time the narrative model works with.
     *
     * @param epochMillis Milliseconds since 1970-01-01T00:00:00Z.
     * @return The same moment as a date-time in UTC.
     */
    private fun toUtc(epochMillis: Long): LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC)
}
