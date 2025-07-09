package com.inso_world.binocular.cli.service.its

import com.inso_world.binocular.core.service.IssueInfrastructurePort
import com.inso_world.binocular.model.Issue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class IssueService(
    @Autowired private val issueDao: IssueInfrastructurePort,
) {
    fun getOrCreate(e: Issue): Issue =
        e.id?.let { id ->
            this.issueDao.findById(id)
        } ?: this.issueDao.save(e)
}
