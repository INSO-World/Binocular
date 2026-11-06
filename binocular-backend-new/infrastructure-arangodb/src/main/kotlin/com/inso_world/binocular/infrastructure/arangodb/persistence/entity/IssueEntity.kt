package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.annotation.Document
import com.arangodb.springframework.annotation.Field
import com.arangodb.springframework.annotation.Relations
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.IssueAccountConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.IssueCommitConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.IssueMilestoneConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.IssueNoteConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.IssueUserConnectionEntity
import org.springframework.data.annotation.Id
import java.util.*

/**
 * ArangoDB-specific Issue entity.
 */
@Document("issues")
data class IssueEntity(
    @Id
    var id: String? = null,
    @Field("iid")
    var iid: Int? = null,
    var title: String? = null,
    var description: String? = null,
    var createdAt: Date? = null,
    var closedAt: Date? = null,
    var updatedAt: Date? = null,
    var labels: List<String> = emptyList(),
    var state: String? = null,
    var webUrl: String? = null,
    var mentions: List<MentionEntity> = emptyList(),
    @Relations(
        edges = [IssueAccountConnectionEntity::class],
        lazy = true,
        maxDepth = 1,
        direction = Relations.Direction.OUTBOUND,
    )
    var accounts: List<AccountEntity> = emptyList(),
    @Relations(
        edges = [IssueCommitConnectionEntity::class],
        lazy = true,
        maxDepth = 1,
        direction = Relations.Direction.OUTBOUND,
    )
    var commits: List<CommitEntity> = emptyList(),
    @Relations(
        edges = [IssueMilestoneConnectionEntity::class],
        lazy = true,
        maxDepth = 1,
        direction = Relations.Direction.OUTBOUND,
    )
    var milestones: List<MilestoneEntity> = emptyList(),
    @Relations(
        edges = [IssueNoteConnectionEntity::class],
        lazy = true,
        maxDepth = 1,
        direction = Relations.Direction.OUTBOUND,
    )
    var notes: List<NoteEntity> = emptyList(),
    @Relations(
        edges = [IssueUserConnectionEntity::class],
        lazy = true,
        maxDepth = 1,
        direction = Relations.Direction.OUTBOUND,
    )
    var users: List<UserEntity> = emptyList(),
)
