package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.service.MergeRequestInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.persistence.entity.MergeRequestEntity
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.MergeRequest
import com.inso_world.binocular.model.Milestone
import com.inso_world.binocular.model.Note
import org.springframework.stereotype.Service

@Service
class MergeRequestInfrastructurePortImpl :
    AbstractInfrastructurePort<MergeRequest, MergeRequestEntity, Long>(Long::class),
    MergeRequestInfrastructurePort {
    override fun findAccountsByMergeRequestId(mergeRequestId: String): List<Account> {
        TODO("Not yet implemented")
    }

    override fun findMilestonesByMergeRequestId(mergeRequestId: String): List<Milestone> {
        TODO("Not yet implemented")
    }

    override fun findNotesByMergeRequestId(mergeRequestId: String): List<Note> {
        TODO("Not yet implemented")
    }
}
