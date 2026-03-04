package com.inso_world.binocular.web.graphql.mapper.impl

import com.inso_world.binocular.model.File
import com.inso_world.binocular.web.graphql.model.FileDto
import org.springframework.stereotype.Component

@Component
class GraphQlFileMapper {
    fun toDto(file: File): FileDto {
        return FileDto(
            id = file.id,
            path = file.path,
            maxLength = file.maxLength.toLong(),
            webUrl = file.webUrl
        )
    }
}
