package com.inso_world.binocular.cli.persistence.repository.sql

import com.inso_world.binocular.cli.entity.MergeRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MergeRequestRepository : JpaRepository<MergeRequest, Long>
