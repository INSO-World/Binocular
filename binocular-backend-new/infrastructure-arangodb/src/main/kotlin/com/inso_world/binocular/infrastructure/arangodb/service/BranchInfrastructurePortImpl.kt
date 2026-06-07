package com.inso_world.binocular.infrastructure.arangodb.service

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.context.MappingSession
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.infrastructure.arangodb.assembler.RepositoryAssembler
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.IBranchFileConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IBranchDao
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Reference
import com.inso_world.binocular.model.Repository
import jakarta.annotation.PostConstruct
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class BranchInfrastructurePortImpl(
    @Autowired private val branchDao: IBranchDao
) : AbstractInfrastructurePort<Branch, String>(),
    BranchInfrastructurePort {
    @PostConstruct
    fun init() {
        super.dao = branchDao
    }

    @Autowired private lateinit var branchFileConnectionRepository: IBranchFileConnectionDao

    @Autowired
    @Lazy
    private lateinit var repositoryAssembler: RepositoryAssembler

    @Autowired
    @Lazy
    private lateinit var self: BranchInfrastructurePortImpl

    companion object {
        private val logger by logger()
    }

    @MappingSession
    override fun findAll(pageable: Pageable): Page<Branch> {
        logger.trace("Getting all branches with pageable: page=${pageable.pageNumber}, size=${pageable.pageSize}")
        return branchDao.findAll(pageable)
    }

    @MappingSession
    override fun findById(id: String): Branch? {
        logger.trace("Getting branch by id: $id")
        return branchDao.findById(id)
    }

    override fun findByIid(iid: Reference.Id): @Valid Branch? = self.findByIidInternal(iid)

    @MappingSession
    @Transactional(readOnly = true)
    protected fun findByIidInternal(iid: Reference.Id): Branch? {
        logger.trace("Getting branch by iid: $iid")
        return this.branchDao.findByIid(iid)
    }

    @MappingSession
    override fun findFilesByBranchId(branchId: String): List<File> {
        logger.trace("Getting files for branch: $branchId")
        return branchFileConnectionRepository.findFilesByBranch(branchId)
    }

    @MappingSession
    override fun findFilesByBranchId(
        branchId: String,
        pageable: Pageable,
    ): Page<File> {
        logger.trace("Getting files for branch: $branchId with pageable: page=${pageable.pageNumber}, size=${pageable.pageSize}")
        return branchFileConnectionRepository.findFilesByBranch(branchId, pageable)
    }

    @MappingSession
    override fun findAll(): Iterable<Branch> = this.branchDao.findAll()

    override fun create(entity: Branch): Branch {
        repositoryAssembler.toEntity(entity.repository)
        return this.branchDao.save(entity)
    }

    @MappingSession
    override fun saveAll(entities: Collection<Branch>): Iterable<Branch> {
        entities.forEach { create(it) }
        return entities
    }

    override fun update(entity: Branch): Branch {
        TODO("Not yet implemented")
    }

    override fun findAll(repository: Repository): Iterable<Branch> {
        TODO("Not yet implemented")
    }

    @MappingSession
    override fun findByName(name: String): Branch? {
        logger.trace("Getting branch by name: $name")
        return branchDao.findByName(name)
    }

    override fun deleteAll() {
        this.branchDao.deleteAll()
    }
}
