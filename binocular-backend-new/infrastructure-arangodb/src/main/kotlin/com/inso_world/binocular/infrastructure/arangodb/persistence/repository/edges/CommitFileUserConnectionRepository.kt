package com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.FileEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.UserEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitFileUserConnectionEntity
import org.springframework.stereotype.Repository

@Repository
interface CommitFileUserConnectionRepository : ArangoRepository<CommitFileUserConnectionEntity, String> {
    @Query(
        """
    FOR c IN `commit-files-users`
        FILTER c._from == CONCAT('files/', @commitFileId)
        FOR u IN users
            FILTER u._id == c._to
            RETURN u
""",
    )
    fun findUsersByCommitFile(commitFileId: String): List<UserEntity>

    @Query(
        """
    FOR c IN `commit-files-users`
        FILTER c._to == CONCAT('users/', @userId)
        FOR f IN files
            FILTER f._id == c._from
            RETURN f
""",
    )
    fun findCommitFilesByUser(userId: String): List<FileEntity>
}
