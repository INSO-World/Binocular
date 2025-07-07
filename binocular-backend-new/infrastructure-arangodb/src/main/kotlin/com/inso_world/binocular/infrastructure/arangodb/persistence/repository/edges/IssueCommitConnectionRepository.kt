package com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.IssueEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.IssueCommitConnectionEntity
import org.springframework.stereotype.Repository

@Repository
interface IssueCommitConnectionRepository : ArangoRepository<IssueCommitConnectionEntity, String> {
    @Query(
        """
    FOR c IN `issues-commits`
        FILTER c._from == CONCAT('issues/', @issueId)
        FOR cm IN commits
            FILTER cm._id == c._to
            RETURN cm
""",
    )
    fun findCommitsByIssue(issueId: String): List<CommitEntity>

    @Query(
        """
    FOR c IN `issues-commits`
        FILTER c._to == CONCAT('commits/', @commitId)
        FOR i IN issues
            FILTER i._id == c._from
            RETURN i
""",
    )
    fun findIssuesByCommit(commitId: String): List<IssueEntity>
}
