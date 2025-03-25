package com.inso_world.binocular.web.rest

import com.inso_world.binocular.web.dao.CommitRepository
import com.inso_world.binocular.web.dao.CommitsCommitsRepository
import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.CommitParent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/commit"])
class CommitEndpoint(
  @Autowired private val commitRepository: CommitRepository,
  @Autowired private val edgeRepo: CommitsCommitsRepository
) {
  @GetMapping
  fun findAll(): MutableIterable<Commit> {
    return commitRepository.findAll()
  }

  @GetMapping(value = ["/p"])
  fun findAllParents(): MutableIterable<CommitParent> {
    return edgeRepo.findAll()
  }
}
