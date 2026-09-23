package com.inso_world.binocular.web.graphql.mapper.impl

import com.inso_world.binocular.model.Revision
import com.inso_world.binocular.web.graphql.model.CommitDto
import com.inso_world.binocular.web.graphql.model.FileDto
import com.inso_world.binocular.web.graphql.model.RevisionDto
import org.springframework.stereotype.Component

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
@Component
class GraphQlRevisionMapper(
    private val commitMapper: GraphQlCommitMapper,
    @org.springframework.context.annotation.Lazy private val fileMapper: GraphQlFileMapper
) {
    fun toDto(revision: Revision): RevisionDto {
        return RevisionDto(
            id = revision.iid.toString(),
            content = revision.content,
            commit = CommitDto(sha = revision.commitSha),
            file = FileDto(path = revision.filePath)
        )
    }
}
