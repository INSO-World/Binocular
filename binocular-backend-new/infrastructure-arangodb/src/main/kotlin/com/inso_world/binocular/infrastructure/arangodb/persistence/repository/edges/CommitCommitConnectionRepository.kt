package com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.CommitCommitConnectionEntity
import org.springframework.stereotype.Repository

@Repository
interface CommitCommitConnectionRepository : ArangoRepository<CommitCommitConnectionEntity, String> {
    @Query(
        """
    FOR c IN `commits-commits`
        FILTER c._from == CONCAT('commits/', @childCommitId)
        FOR cm IN commits
            FILTER cm._id == c._to
            RETURN cm
""",
    )
    fun findParentCommitsByChildCommit(childCommitId: String): List<CommitEntity>

    @Query(
        """
    FOR c IN `commits-commits`
        FILTER c._to == CONCAT('commits/', @parentCommitId)
        FOR cm IN commits
            FILTER cm._id == c._from
            RETURN cm
""",
    )
    fun findChildCommitsByParentCommit(parentCommitId: String): List<CommitEntity>
}
