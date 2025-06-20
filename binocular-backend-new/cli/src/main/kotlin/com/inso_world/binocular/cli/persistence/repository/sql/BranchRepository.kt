package com.inso_world.binocular.cli.persistence.repository.sql

import com.inso_world.binocular.cli.entity.Branch
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface BranchRepository : JpaRepository<Branch, Long> {
    fun findByNameAndRepositoryId(name: String, repositoryId: Long): Optional<Branch>
} 