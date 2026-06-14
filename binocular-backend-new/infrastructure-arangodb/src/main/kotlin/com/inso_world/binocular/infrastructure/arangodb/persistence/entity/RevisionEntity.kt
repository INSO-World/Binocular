package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.From
import com.arangodb.springframework.annotation.To
import org.springframework.data.annotation.Id

/**
 * ArangoDB-specific Revision entity.
 *
 * Links a [FileEntity] and a [CommitEntity] with additional content information.
 * In ArangoDB, we model this as an edge between File and Commit.
 */
@Document("revisions")
data class RevisionEntity(
    @Id var id: String? = null,
    @From var file: FileEntity,
    @To var commit: CommitEntity,
    var content: String? = null,
    var lines: Int? = null,
)
