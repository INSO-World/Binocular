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
                                else -> throw CoercingSerializeException("Expected a Long or Int")
                            }

                        override fun parseValue(input: Any): Long =
                            when (input) {
                                is Long -> input
                                is Int -> input.toLong()
                                is String ->
                                    try {
                                        input.toLong()
                                    } catch (e: NumberFormatException) {
                                        throw CoercingParseValueException("Expected a Long value but was $input")
                                    }
                                else -> throw CoercingParseValueException("Expected a Long value but was $input")
                            }

                        override fun parseLiteral(input: Any): Long {
                            if (input is IntValue) {
                                return input.value.toLong()
                            }
                            throw CoercingParseLiteralException("Expected a Long value but was $input")
                        }
                    },
                ).build()

        return RuntimeWiringConfigurer { builder ->
            builder.scalar(timestampType)
        }
    }
}
