package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.MergeRequestInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.persistence.entity.MergeRequestEntity
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.MergeRequest
import com.inso_world.binocular.model.Milestone
import com.inso_world.binocular.model.Note
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

@Service
@Validated
internal class MergeRequestInfrastructurePortImpl :
    AbstractInfrastructurePort<MergeRequest, MergeRequestEntity, Long>(Long::class),
    MergeRequestInfrastructurePort {
    override fun update(value: MergeRequest): MergeRequest {
        TODO("Not yet implemented")
    }

    override fun delete(value: MergeRequest) {
        TODO("Not yet implemented")
    }

    override fun updateAndFlush(value: MergeRequest): MergeRequest {
        TODO("Not yet implemented")
    }

    override fun create(value: MergeRequest): MergeRequest {
        TODO("Not yet implemented")
    }

    override fun saveAll(values: Collection<MergeRequest>): Iterable<MergeRequest> {
        TODO("Not yet implemented")
    }

    override fun findAccountsByMergeRequestId(mergeRequestId: String): List<Account> {
        TODO("Not yet implemented")
    }

    override fun findMilestonesByMergeRequestId(mergeRequestId: String): List<Milestone> {
        TODO("Not yet implemented")
    }

    override fun findNotesByMergeRequestId(mergeRequestId: String): List<Note> {
        TODO("Not yet implemented")
    }

    override fun findById(id: String): MergeRequest? {
        TODO("Not yet implemented")
    }

    override fun findAll(): Iterable<MergeRequest> {
        TODO("Not yet implemented")
    }

    override fun findAll(pageable: Pageable): Page<MergeRequest> {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: String) {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        TODO("Not yet implemented")
    }
}
