package com.inso_world.binocular.infrastructure.sql.persistence.repository

import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
internal interface RepositoryRepository : JpaRepository<RepositoryEntity, Long>, JpaSpecificationExecutor<RepositoryEntity> {
    fun findByName(name: String): RepositoryEntity?

//    @EntityGraph(
//        attributePaths = [
////            "commits",
////            "commits.parents",
////            "commits.children",
//            "branches",
//            "user"
//        ]
//    )
    @Query("select r from RepositoryEntity r where r.id = :id")
    fun findByIdWithAllRelations(@Param("id") id: Long): RepositoryEntity?
}
