package com.inso_world.binocular.cli.persistence.dao.sql

import com.inso_world.binocular.cli.entity.Issue
import com.inso_world.binocular.cli.persistence.dao.sql.interfaces.IIssueDao
import com.inso_world.binocular.cli.persistence.repository.sql.IssueRepository
import com.inso_world.binocular.infrastructure.sql.persistence.dao.SqlDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class IssueDao(
    @Autowired private val repo: IssueRepository,
) : SqlDao<Issue, Long>(),
    IIssueDao {
    init {
        this.setClazz(Issue::class.java)
        this.setRepository(repo)
    }
}
