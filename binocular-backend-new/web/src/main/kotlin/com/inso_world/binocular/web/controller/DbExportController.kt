package com.inso_world.binocular.web.controller

import com.inso_world.binocular.web.service.DbExportService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api")
class DbExportController (private val exportService: DbExportService) {

  var logger: Logger = LoggerFactory.getLogger(DbExportController::class.java)

  // TODO: add openAPI
  @GetMapping("/db-export")
  fun exportDb(): ResponseEntity<Map<String, Any?>> {
    logger.info("Received request to export the database")

    return try {
      val result = exportService.exportDb()
      ResponseEntity.ok(result)
    } catch (ex: RuntimeException) {
      ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(mapOf("error" to "Failed to export database", "details" to ex.message))
    }
  }

}
