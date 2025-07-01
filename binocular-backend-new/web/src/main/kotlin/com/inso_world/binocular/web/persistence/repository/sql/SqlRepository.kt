package com.inso_world.binocular.web.persistence.repository.sql

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean

/**
 * Base repository interface for SQL entities.
 * This interface extends JpaRepository to provide standard CRUD operations.
 * It's marked with @NoRepositoryBean to indicate that it's not meant to be instantiated directly.
 *
 * @param T the entity type
 * @param ID the type of the entity's ID
 */
@NoRepositoryBean
interface SqlRepository<T, ID> : JpaRepository<T, ID>
