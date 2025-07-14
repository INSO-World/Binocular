package com.inso_world.binocular.infrastructure.arangodb.service

import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.IBranchFileConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IBranchDao
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.File
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class BranchInfrastructurePortImpl : BranchInfrastructurePort {
    @Autowired private lateinit var branchDao: IBranchDao

    @Autowired private lateinit var branchFileConnectionRepository: IBranchFileConnectionDao
    var logger: Logger = LoggerFactory.getLogger(BranchInfrastructurePortImpl::class.java)

    override fun findAll(pageable: Pageable): Page<Branch> {
        logger.trace("Getting all branches with pageable: page=${pageable.pageNumber}, size=${pageable.pageSize}")
        return branchDao.findAll(pageable)
    }

    override fun findById(id: String): Branch? {
        logger.trace("Getting branch by id: $id")
        return branchDao.findById(id)
    }

    override fun findFilesByBranchId(branchId: String): List<File> {
        logger.trace("Getting files for branch: $branchId")
        return branchFileConnectionRepository.findFilesByBranch(branchId)
    }

    override fun findAll(): Iterable<Branch> = this.branchDao.findAll()

    override fun save(entity: Branch): Branch = this.branchDao.save(entity)

    override fun saveAll(entities: Collection<Branch>): Iterable<Branch> = this.branchDao.saveAll(entities)

    override fun delete(entity: Branch) = this.branchDao.delete(entity)

    override fun update(entity: Branch): Branch {
        TODO("Not yet implemented")
    }

    override fun updateAndFlush(entity: Branch): Branch {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: String) {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        this.branchDao.deleteAll()
    }
}
