package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Relations
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.MergeRequestAccountConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.MergeRequestMilestoneConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.MergeRequestNoteConnectionEntity
import com.inso_world.binocular.model.Mention
import org.springframework.data.annotation.Id

/**
 * ArangoDB-specific MergeRequest entity.
 */
@Document("mergeRequests")
data class MergeRequestEntity(
    @Id
    var id: String? = null,
    var iid: Int? = null,
    var title: String? = null,
    var description: String? = null,
    var createdAt: String? = null,
    var closedAt: String? = null,
    var updatedAt: String? = null,
    var labels: List<String> = emptyList(),
    var state: String? = null,
    var webUrl: String? = null,
    var mentions: List<Mention> = emptyList(),
    @Relations(
        edges = [MergeRequestAccountConnectionEntity::class],
        lazy = true,
        maxDepth = 1,
        direction = Relations.Direction.OUTBOUND,
    )
    var accounts: List<AccountEntity> = emptyList(),
    @Relations(
        edges = [MergeRequestMilestoneConnectionEntity::class],
        lazy = true,
        maxDepth = 1,
        direction = Relations.Direction.OUTBOUND,
    )
    var milestones: List<MilestoneEntity> = emptyList(),
    @Relations(
        edges = [MergeRequestNoteConnectionEntity::class],
        lazy = true,
        maxDepth = 1,
        direction = Relations.Direction.OUTBOUND,
    )
    var notes: List<NoteEntity> = emptyList(),
)
