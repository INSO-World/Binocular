package com.inso_world.binocular.web.graphql.config

import graphql.language.IntValue
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import graphql.schema.GraphQLScalarType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.execution.RuntimeWiringConfigurer

/**
 * Configuration for the Timestamp scalar type in GraphQL.
 * This scalar represents a timestamp as milliseconds since epoch.
 */
@Configuration
class TimestampScalar {
    @Bean
    fun timestampScalarConfigurer(): RuntimeWiringConfigurer {
        val timestampType =
            GraphQLScalarType
                .newScalar()
                .name("Timestamp")
                .description("Timestamp scalar type representing milliseconds since epoch")
                .coercing(
                    object : Coercing<Long, Long> {
                        override fun serialize(dataFetcherResult: Any): Long =
                            when (dataFetcherResult) {
                                is Long -> dataFetcherResult
                                is Int -> dataFetcherResult.toLong()
                                is String -> parseStringToMillis(dataFetcherResult)
                                else -> throw CoercingSerializeException("Expected a Long/Int/String (ISO-8601 or epoch millis)")
                            }

                        override fun parseValue(input: Any): Long =
                            when (input) {
                                is Long -> input
                                is Int -> input.toLong()
                                is String -> parseStringToMillis(input)
                                else -> throw CoercingParseValueException("Expected a Long/Int/String (ISO-8601 or epoch millis) but was $input")
                            }

                        override fun parseLiteral(input: Any): Long {
                            return when (input) {
                                is IntValue -> input.value.toLong()
                                is graphql.language.StringValue -> parseStringToMillis(input.value)
                                else -> throw CoercingParseLiteralException("Expected a Long/Int/String literal but was $input")
                            }
                        }

                        private fun parseStringToMillis(value: String): Long =
                            value.trim().let { v ->
                                // If numeric, parse directly
                                v.toLongOrNull()?.let { return it }
                                // Try ISO-8601 with timezone (e.g., 2025-12-17T21:50:38.008Z)
                                kotlin.runCatching { java.time.Instant.parse(v).toEpochMilli() }.getOrNull()
                                    ?: kotlin.runCatching {
                                        // Try ISO date-time without zone, assume UTC
                                        java.time.LocalDateTime.parse(v, java.time.format.DateTimeFormatter.ISO_DATE_TIME)
                                            .toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
                                    }.getOrNull()
                                    ?: throw CoercingParseValueException("Expected epoch millis or ISO-8601 timestamp but was '$value'")
                            }
                    },
                ).build()

        return RuntimeWiringConfigurer { builder ->
            builder.scalar(timestampType)
        }
    }
}
