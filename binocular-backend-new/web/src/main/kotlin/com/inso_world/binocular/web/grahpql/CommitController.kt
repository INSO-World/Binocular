package com.inso_world.binocular.web.grahpql

import com.inso_world.binocular.web.dao.CommitRepository
import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.service.CommitService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller


@Controller
@SchemaMapping(typeName = "Commit")
class CommitController(
  @Autowired private val commitService: CommitService,
) {

  @QueryMapping(name = "commits")
  fun findAll(@Argument page: Int?, @Argument perPage: Int?): List<Commit> {
    return commitService.findAll(
      page, perPage
    )
  }

}
