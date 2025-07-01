package com.inso_world.binocular.web.persistence.repository.arangodb.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.persistence.entity.arangodb.edges.CommitFileUserConnectionEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.FileEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.UserEntity
import org.springframework.stereotype.Repository

@Repository
interface CommitFileUserConnectionRepository: ArangoRepository<CommitFileUserConnectionEntity, String> {

  @Query("""
    FOR c IN `commit-files-users`
        FILTER c._from == CONCAT('files/', @commitFileId)
        FOR u IN users
            FILTER u._id == c._to
            RETURN u
""")
  fun findUsersByCommitFile(commitFileId: String): List<UserEntity>

  @Query("""
    FOR c IN `commit-files-users`
        FILTER c._to == CONCAT('users/', @userId)
        FOR f IN files
            FILTER f._id == c._from
            RETURN f
""")
  fun findCommitFilesByUser(userId: String): List<FileEntity>
}
