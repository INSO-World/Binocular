package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.persistence.dao.interfaces.IFileDao
import com.inso_world.binocular.web.persistence.entity.arangodb.FileEntity
import com.inso_world.binocular.web.persistence.mapper.arangodb.FileMapper
import com.inso_world.binocular.web.persistence.repository.arangodb.FileRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of IFileDao using the MappedArangoDbDao approach.
 * 
 * This class extends MappedArangoDbDao to leverage the entity mapping pattern,
 * which provides a clean separation between domain models (File) and 
 * database-specific entities (FileEntity).
 */
@Repository
@Profile("nosql", "arangodb")
class FileDao(
  @Autowired fileRepository: FileRepository,
  @Autowired fileMapper: FileMapper
) : MappedArangoDbDao<File, FileEntity, String>(fileRepository, fileMapper), IFileDao {
}
