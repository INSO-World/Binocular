package com.inso_world.binocular.infrastructure.arangodb.persistence.repository

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.FileEntity
import org.springframework.stereotype.Repository

@Repository
interface FileRepository : ArangoRepository<FileEntity, String> {

    @Query(
        """
        FOR f IN files
          FILTER f.path == @path
          LIMIT 1
          RETURN f
        """
    )
    fun findByPath(path: String): FileEntity?

}
