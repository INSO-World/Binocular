package com.inso_world.binocular.cli.persistence.repository.sql

import com.inso_world.binocular.cli.entity.Issue
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface IssueRepository : JpaRepository<Issue, Long>
