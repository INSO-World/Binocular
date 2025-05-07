package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.persistence.dao.interfaces.IFileDao
import com.inso_world.binocular.web.persistence.repository.arangodb.FileRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class FileDao(
  @Autowired private val fileRepository: FileRepository
) : ArangoDbDao<File, String>(), IFileDao {

  init {
    this.setClazz(File::class.java)
    this.setRepository(fileRepository)
  }
}
