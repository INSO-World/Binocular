package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.MergeRequestInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.persistence.dao.MergeRequestDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.MergeRequestLinkDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.NoteDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.MergeRequestEntity
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.MergeRequest
import com.inso_world.binocular.model.Milestone
import com.inso_world.binocular.model.Note
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

@Service
@Profile("postgres")
@Validated
internal class MergeRequestInfrastructurePortImpl(
    private val mrDao: MergeRequestDao,
    private val linkDao: MergeRequestLinkDao,
    private val noteDao: NoteDao,
) :
    AbstractInfrastructurePort<MergeRequest, MergeRequestEntity, Long>(Long::class),
    MergeRequestInfrastructurePort {

    override fun findAccountsByMergeRequestId(mergeRequestId: String): List<Account> =
        linkDao.findAccountIdsByMergeRequestId(mergeRequestId).map { Account(id = it) }

    override fun findMilestonesByMergeRequestId(mergeRequestId: String): List<Milestone> =
        linkDao.findMilestoneIdsByMergeRequestId(mergeRequestId).map { Milestone(id = it) }

    override fun findNotesByMergeRequestId(mergeRequestId: String): List<Note> =
        linkDao.findNoteIdsByMergeRequestId(mergeRequestId)
            .mapNotNull { nid -> noteDao.findById(nid) }

    override fun findById(id: String): MergeRequest? = mrDao.findById(id)

    override fun create(value: MergeRequest): MergeRequest = mrDao.create(value)

    override fun saveAll(values: Collection<MergeRequest>): Iterable<MergeRequest> = values.onEach { create(it) }

    override fun findAll(): Iterable<MergeRequest> = mrDao.findAll()

    override fun findAll(pageable: Pageable): Page<MergeRequest> {
        val total = mrDao.count()
        if (total == 0L) return Page(emptyList(), 0, pageable)
        val content = mrDao.findAll(pageable)
        return Page(content, total, pageable)
    }

    override fun findAll(pageable: Pageable, since: Long?, until: Long?): Page<MergeRequest> {
        TODO("Not yet implemented")
    }

    override fun update(value: MergeRequest): MergeRequest = mrDao.update(value)

    override fun updateAndFlush(value: MergeRequest): MergeRequest = update(value)

    override fun delete(value: MergeRequest) {
        value.id?.let { deleteById(it) }
    }

    override fun deleteById(id: String) {
        linkDao.deleteLinksByMergeRequestId(id)
        mrDao.deleteById(id)
    }

    override fun deleteAll() {
        linkDao.deleteAllLinks()
        mrDao.deleteAll()
    }
}
