package com.inso_world.binocular.infrastructure.arangodb.persistence.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import java.time.LocalDateTime
import java.time.OffsetDateTime

/**
 * Converter to write Kotlin [LocalDateTime] to String for ArangoDB storage.
 */
@WritingConverter
internal class LocalDateTimeToStringConverter : Converter<LocalDateTime, String> {
    override fun convert(source: LocalDateTime): String = source.toString()
}

/**
 * Converter to read String from ArangoDB and convert to Kotlin [LocalDateTime].
 *
 * Handles ISO-8601 timestamps with timezone information by parsing as [OffsetDateTime]
 * and extracting the local date-time. Supports:
 * - UTC indicator: `2016-11-16T13:22:07.000Z`
 * - Offset format: `2016-11-16T13:22:07.000+02:00`
 * - No timezone: `2016-11-16T13:22:07.000` (parsed directly)
 */
@ReadingConverter
internal class StringToLocalDateTimeConverter : Converter<String, LocalDateTime> {
    override fun convert(source: String): LocalDateTime =
        if (hasTimezoneInfo(source)) {
            OffsetDateTime.parse(source).toLocalDateTime()
        } else {
            LocalDateTime.parse(source)
        }

    private fun hasTimezoneInfo(source: String): Boolean =
        source.endsWith("Z") || source.lastIndexOf('+') > 10 || source.lastIndexOf('-') > 10
}
