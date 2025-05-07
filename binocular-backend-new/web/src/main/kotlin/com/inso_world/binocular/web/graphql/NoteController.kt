package com.inso_world.binocular.web.graphql

import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.graphql.error.GraphQLValidationUtils
import com.inso_world.binocular.web.service.NoteService
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
  @Autowired private val noteService: NoteService,
) {
  private var logger: Logger = LoggerFactory.getLogger(NoteController::class.java)

  @QueryMapping(name = "notes")
  fun findAll(@Argument page: Int?, @Argument perPage: Int?): Iterable<Note> {
    logger.trace("Getting all notes...")

    GraphQLValidationUtils.validatePagination(page, perPage)

    return noteService.findAll(page, perPage)
  }

  @QueryMapping(name = "note")
  fun findById(@Argument id: String): Note {
    logger.trace("Getting note by id: $id")
    return GraphQLValidationUtils.requireEntityExists(noteService.findById(id), "Note", id)
  }
}
