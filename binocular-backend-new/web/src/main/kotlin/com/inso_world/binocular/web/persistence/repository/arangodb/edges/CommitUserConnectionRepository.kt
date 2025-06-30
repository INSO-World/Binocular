package com.inso_world.binocular.web.persistence.repository.arangodb.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.persistence.entity.arangodb.CommitEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.CommitUserConnectionEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.UserEntity
import org.springframework.stereotype.Repository

@Repository
interface CommitUserConnectionRepository: ArangoRepository<CommitUserConnectionEntity, String> {

  @Query("""
    FOR c IN `commits-users`
        FILTER c._from == CONCAT('commits/', @commitId)
        FOR u IN users
            FILTER u._id == c._to
            RETURN u
""")
  fun findUsersByCommit(commitId: String): List<UserEntity>

  @Query("""
    FOR c IN `commits-users`
        FILTER c._to == CONCAT('users/', @userId)
        FOR cm IN commits
            FILTER cm._id == c._from
            RETURN cm
""")
  fun findCommitsByUser(userId: String): List<CommitEntity>
}
