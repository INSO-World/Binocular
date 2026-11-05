package com.inso_world.binocular.cli.service.its

import com.inso_world.binocular.core.service.MergeRequestInfrastructurePort
import com.inso_world.binocular.model.MergeRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

// TODO can be uncommented when port is implemented in sql infra
//@Service
//class MergeRequestService(
//    @Autowired private val dao: MergeRequestInfrastructurePort,
//) {
//    fun getOrCreate(e: MergeRequest): MergeRequest = e.id?.let { this.dao.findById(it) } ?: this.dao.create(e)
//}
