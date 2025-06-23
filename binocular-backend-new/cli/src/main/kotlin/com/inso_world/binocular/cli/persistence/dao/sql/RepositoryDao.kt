package com.inso_world.binocular.cli.persistence.dao.sql

import com.inso_world.binocular.cli.persistence.dao.sql.interfaces.IRepositoryDao
import com.inso_world.binocular.cli.persistence.repository.sql.RepositoryRepository
import com.inso_world.binocular.core.persistence.dao.sql.SqlDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class RepositoryDao(
    @Autowired private val repositoryRepository: RepositoryRepository,
) : SqlDao<com.inso_world.binocular.cli.entity.Repository, Long>(),
    IRepositoryDao {
    init {
        this.setClazz(com.inso_world.binocular.cli.entity.Repository::class.java)
        this.setRepository(repositoryRepository)
    }

    override fun findByName(gitDir: String): com.inso_world.binocular.cli.entity.Repository? = this.repositoryRepository.findByName(gitDir)

//  override fun findByNameWithRelations(name: String): com.inso_world.binocular.cli.entity.Repository? {
//    return this.repositoryRepository.findByNameWithRelations(name)
//  }
}
