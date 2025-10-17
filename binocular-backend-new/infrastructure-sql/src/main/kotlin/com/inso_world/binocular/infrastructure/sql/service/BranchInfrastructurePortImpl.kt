package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.mapper.BranchMapper
import com.inso_world.binocular.infrastructure.sql.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.sql.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.sql.mapper.context.MappingSession
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IBranchDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Repository
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

@Service
@Validated
internal class BranchInfrastructurePortImpl(
    @Autowired private val branchMapper: BranchMapper,
) : AbstractInfrastructurePort<Branch, BranchEntity, Long>(Long::class),
    BranchInfrastructurePort {
    @Autowired
    private lateinit var branchDao: IBranchDao

    @Autowired
    @Lazy
    private lateinit var repositoryMapper: RepositoryMapper

    @Autowired
    @Lazy
    private lateinit var projectMapper: ProjectMapper

    @PostConstruct
    fun init() {
        super.dao = branchDao
    }

    override fun findFilesByBranchId(branchId: String): List<File> {
        TODO("Not yet implemented")
    }

    override fun findById(id: String): Branch? {
        TODO("Not yet implemented")
    }

    override fun update(value: Branch): Branch {
        TODO("Not yet implemented")
    }

    override fun delete(value: Branch) {
        TODO("Not yet implemented")
    }

    override fun updateAndFlush(value: Branch): Branch {
        TODO("Not yet implemented")
    }

    override fun create(value: Branch): Branch {
        TODO("Not yet implemented")
    }

    override fun saveAll(values: Collection<Branch>): Iterable<Branch> {
        TODO("Not yet implemented")
    }

    @MappingSession
    @Transactional(readOnly = true)
    override fun findAll(): Iterable<Branch> {
        val context: MutableMap<Long, Repository> = mutableMapOf()

        return super<AbstractInfrastructurePort>.findAllEntities().map { b ->
            val repository =
                context.getOrPut(b.repository?.id!!) {
                    val repo = b.repository ?: throw IllegalStateException("Repository of a Branch cannot be null")
                    val project =
                        projectMapper.toDomain(
                            repo.project,
                        )

                    this.repositoryMapper.toDomain(repo, project)
                }
            branchMapper.toDomainFull(b, repository)
        }
    }

    @MappingSession
    @Transactional(readOnly = true)
    override fun findAll(repository: Repository): Iterable<Branch> =
        branchDao.findAll(repository).map { b ->
            branchMapper.toDomainFull(b, repository)
        }

    override fun findAll(pageable: Pageable): Page<Branch> {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: String) {
        TODO("Not yet implemented")
    }

    @Transactional
    override fun deleteAll() {
        super.deleteAllEntities()
    }
}
