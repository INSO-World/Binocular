package com.inso_world.binocular.web.persistence.entity.arangodb.edges

import com.arangodb.springframework.annotation.Edge
import com.arangodb.springframework.annotation.From
import com.arangodb.springframework.annotation.To
import com.inso_world.binocular.web.persistence.entity.arangodb.CommitEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.FileEntity
import org.springframework.data.annotation.Id

/**
 * ArangoDB-specific entity for a connection between a Commit and a File.
 */
@Edge(value = "commits-files")
data class CommitFileConnectionEntity(
    @Id var id: String? = null,
    @From var from: CommitEntity,
    @To var to: FileEntity,
    var lineCount: Int? = null
)
