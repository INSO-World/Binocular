package com.inso_world.binocular.web.controller

import com.inso_world.binocular.web.service.DbExportService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api") //TODO: check if mapping is correct
class DbExportController (private val exportService: DbExportService) {

  // TODO: add openAPI
  @GetMapping("/db-export")
  fun exportDb(): Map<String, Any> {

    return exportService.exportDb()
  }

}
