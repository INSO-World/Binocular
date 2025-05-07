package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.inso_world.binocular.web.entity.Milestone
import com.inso_world.binocular.web.persistence.dao.interfaces.IMilestoneDao
import com.inso_world.binocular.web.persistence.repository.arangodb.MilestoneRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class MilestoneDao(
  @Autowired private val milestoneRepository: MilestoneRepository
) : ArangoDbDao<Milestone, String>(), IMilestoneDao {

  init {
    this.setClazz(Milestone::class.java)
    this.setRepository(milestoneRepository)
  }
}
