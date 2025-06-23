package com.inso_world.binocular.cli.persistence.repository.sql

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RepositoryRepository : JpaRepository<com.inso_world.binocular.cli.entity.Repository, Long> {
    fun findByName(name: String): com.inso_world.binocular.cli.entity.Repository?
}
