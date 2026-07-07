package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.NoteInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.persistence.dao.NoteDao
import com.inso_world.binocular.infrastructure.sql.persistence.dao.NoteLinkDao
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.MergeRequest
import com.inso_world.binocular.model.Note
import jakarta.validation.Valid
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

@Service
@Profile("postgres")
@Validated
internal class NoteInfrastructurePortImpl(
    private val noteDao: NoteDao,
    private val linkDao: NoteLinkDao,
) : NoteInfrastructurePort {
    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    override fun findAccountsByNoteId(noteId: String): List<Account> =
        linkDao.findAccountIdsByNoteId(noteId).map {
            Account(gid = it, platform = com.inso_world.binocular.model.Platform.GitHub, login = "unknown")
        }

    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    override fun findIssuesByNoteId(noteId: String): List<Issue> =
        linkDao.findIssueIdsByNoteId(noteId).map {
            // TODO: this is just a workaround for now, should be a proper mapper when refactoring is done
            Issue(
                id = it,
                platformIid = it.toIntOrNull(),
                gid = it,
                project =
                    com.inso_world.binocular.model.Project
                        .Id(kotlin.uuid.Uuid.random())
            )
        }

    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    override fun findMergeRequestsByNoteId(noteId: String): List<MergeRequest> =
        linkDao.findMergeRequestIdsByNoteId(noteId).map {
            // TODO: this is just a workaround for now, should be a proper mapper when refactoring is done
            MergeRequest(
                project =
                    com.inso_world.binocular.model.Project
                        .Id(kotlin.uuid.Uuid.parse("00000000-0000-0000-0000-000000000000"))
            ).apply { id = it }
        }

    override fun findAll(pageable: Pageable): Page<Note> {
        val total = noteDao.count()
        if (total == 0L) return Page(emptyList(), 0, pageable)
        val content = noteDao.findAll(pageable)
        return Page(content, total, pageable)
    }

    override fun findById(id: String): Note? = noteDao.findById(id)

    override fun findByIid(iid: Note.Id): @Valid Note? {
        TODO("Not yet implemented")
    }

    override fun findAll(): Iterable<Note> = noteDao.findAll()

    override fun create(entity: Note): Note = noteDao.create(entity)

    override fun saveAll(entities: Collection<Note>): Iterable<Note> = entities.onEach { create(it) }

    override fun delete(entity: Note) {
        entity.id?.let { deleteById(it) }
    }

    override fun update(entity: Note): Note = noteDao.update(entity)

    override fun deleteById(id: String) {
        linkDao.deleteLinksByNoteId(id)
        noteDao.deleteById(id)
    }

    override fun deleteAll() {
        linkDao.deleteAllLinks()
        noteDao.deleteAll()
    }
}
