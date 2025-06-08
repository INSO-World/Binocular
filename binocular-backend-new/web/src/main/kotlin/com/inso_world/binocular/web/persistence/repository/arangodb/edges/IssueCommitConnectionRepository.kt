package com.inso_world.binocular.web.persistence.repository.arangodb.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.edge.IssueCommitConnection
import org.springframework.stereotype.Repository

@Repository
interface IssueCommitConnectionRepository: ArangoRepository<IssueCommitConnection, String> {

  @Query("""
    FOR c IN `issues-commits`
        FILTER c._from == CONCAT('issues/', @issueId)
        FOR cm IN commits
            FILTER cm._id == c._to
            RETURN cm
""")
  fun findCommitsByIssue(issueId: String): List<Commit>

  @Query("""
    FOR c IN `issues-commits`
        FILTER c._to == CONCAT('commits/', @commitId)
        FOR i IN issues
            FILTER i._id == c._from
            RETURN i
""")
  fun findIssuesByCommit(commitId: String): List<Issue>
}
