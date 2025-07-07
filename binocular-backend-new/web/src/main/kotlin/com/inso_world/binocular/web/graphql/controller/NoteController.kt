package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.core.service.NoteInfrastructurePort
import com.inso_world.binocular.model.Note
import com.inso_world.binocular.web.graphql.error.GraphQLValidationUtils
import com.inso_world.binocular.web.graphql.model.PageDto
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
     * This method returns a Page object that includes:
     * - count: total number of items
     * - page: current page number (1-based)
     * - perPage: number of items per page
     * - data: list of notes for the current page
     *
     * @param page The page number (1-based). If null, defaults to 1.
     * @param perPage The number of items per page. If null, defaults to 20.
     * @return A Page object containing the notes and pagination metadata.
     */
    @QueryMapping(name = "notes")
    fun findAll(
        @Argument page: Int?,
        @Argument perPage: Int?,
    ): PageDto<Note> {
        logger.info("Getting all notes...")

        val pageable = PaginationUtils.createPageableWithValidation(page, perPage)

        val notesPage = noteService.findAll(pageable)

        return PageDto(notesPage)
    }

    @QueryMapping(name = "note")
    fun findById(
        @Argument id: String,
    ): Note {
        logger.info("Getting note by id: $id")
        return GraphQLValidationUtils.requireEntityExists(noteService.findById(id), "Note", id)
    }
}
