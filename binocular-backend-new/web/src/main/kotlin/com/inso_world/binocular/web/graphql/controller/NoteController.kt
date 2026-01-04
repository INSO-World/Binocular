package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.core.service.NoteInfrastructurePort
import com.inso_world.binocular.model.Note
import com.inso_world.binocular.web.graphql.error.GraphQLValidationUtils
import com.inso_world.binocular.web.graphql.model.PageDto
import com.inso_world.binocular.web.graphql.model.Sort
import com.inso_world.binocular.web.util.PaginationUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
@SchemaMapping(typeName = "Note")
class NoteController(
    @Autowired private val noteService: NoteInfrastructurePort,
) {
    private var logger: Logger = LoggerFactory.getLogger(NoteController::class.java)

    /**
     * Find all notes with pagination.
     *
     * @param page The page number (1-based). If null, defaults to 1.
     * @param perPage The number of items per page. If null, defaults to 20.
     * @param sort Optional sort direction (ASC|DESC). Defaults to DESC when not provided.
     * @return A Page object containing the notes and pagination metadata.
     */
    @QueryMapping(name = "notes")
    fun findAll(
        @Argument page: Int?,
        @Argument perPage: Int?,
        @Argument sort: Sort?,
    ): PageDto<Note> {
        logger.info("Getting all notes... sort={}", sort)

        val pageable = PaginationUtils.createPageableWithValidation(page, perPage)

        val all = noteService.findAll().toList()
        val comparatorAsc = compareBy<Note>({ it.createdAt }, { it.updatedAt }, { it.id ?: "" })
        val effectiveSort = sort ?: Sort.DESC
        val sorted = when (effectiveSort) {
            Sort.ASC -> all.sortedWith(comparatorAsc)
            Sort.DESC -> all.sortedWith(comparatorAsc.reversed())
        }
        val from = (pageable.pageNumber * pageable.pageSize).coerceAtMost(sorted.size)
        val to = (from + pageable.pageSize).coerceAtMost(sorted.size)
        val slice = if (from < to) sorted.subList(from, to) else emptyList()
        return PageDto(
            count = sorted.size,
            page = pageable.pageNumber + 1,
            perPage = pageable.pageSize,
            data = slice,
        )
    }

    /**
     * Find a note by its ID.
     *
     * Retrieves a single note using its unique identifier. If the note cannot be found,
     * a GraphQLException is thrown to signal a client error.
     *
     * @param id the unique identifier of the note
     * @return the matching Note
     * @throws graphql.GraphQLException if no note exists with the given ID
     */
    @QueryMapping(name = "note")
    fun findById(
        @Argument id: String,
    ): Note {
        logger.info("Getting note by id: $id")
        return GraphQLValidationUtils.requireEntityExists(noteService.findById(id), "Note", id)
    }

}
