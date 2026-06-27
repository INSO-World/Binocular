package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.persistence.mapper.context.MappingSession
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.assembler.RepositoryAssembler
import com.inso_world.binocular.infrastructure.sql.mapper.BranchMapper
import com.inso_world.binocular.infrastructure.sql.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.sql.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IBranchDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Reference
import com.inso_world.binocular.model.Repository
import jakarta.annotation.PostConstruct
import jakarta.validation.Valid
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
    private lateinit var repositoryAssembler: RepositoryAssembler

    @Autowired
    private lateinit var branchDao: IBranchDao

    @Autowired
    @Lazy
    private lateinit var repositoryMapper: RepositoryMapper

    @Autowired
    @Lazy
    private lateinit var projectMapper: ProjectMapper

    @Autowired
    @Lazy
    private lateinit var self: BranchInfrastructurePortImpl

    @PostConstruct
    fun init() {
        super.dao = branchDao
    }

    override fun findFilesByBranchId(branchId: String): List<File> {
        TODO("Not yet implemented")
    }

    override fun findFilesByBranchId(
        branchId: String,
        pageable: Pageable,
    ): Page<File> {
        TODO("Not yet implemented")
    }

    /**
     * Finds a branch by its JPA-assigned surrogate [id].
     *
     * Assembles the owning repository aggregate so the returned [Branch] is fully wired (head commit,
     * repository, active/tracksFileRenames flags). Returns null when no entity with [id] exists.
     *
     * @throws IllegalArgumentException if [id] cannot be parsed as a Long
     */
    @MappingSession
    @Transactional(readOnly = true)
    override fun findById(id: String): Branch? {
        val idL = id.trim().toLongOrNull() ?: throw IllegalArgumentException("id must be convertable to Long")
        val branchEntity = this.branchDao.findById(idL) ?: return null

        return repositoryAssembler
            .toDomain(branchEntity.repository)
            .branches
            .find { it.id == id }
    }

    override fun findByIid(iid: Reference.Id): @Valid Branch? = self.findByIidInternal(iid)

    @MappingSession
    @Transactional(readOnly = true)
    protected fun findByIidInternal(iid: Reference.Id): Branch? {
        val branch = this.branchDao.findByIid(iid)

        requireNotNull(branch?.repository)
        return repositoryAssembler
            .toDomain(branch.repository)
            .branches
            .find { it.iid == iid }
            ?.let { return it }
    }

    override fun update(value: Branch): Branch {
        TODO("Not yet implemented")
    }

    override fun delete(value: Branch) {
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
        val branches = super<AbstractInfrastructurePort>.findAllEntities()

        // Group branches by repository to process related branches together
        return branches
            .groupBy { it.repository }
            .flatMap { (repoEntity, _) ->
                repositoryAssembler.toDomain(repoEntity).branches
            }
    }

    @MappingSession
    @Transactional(readOnly = true)
    override fun findAll(repository: Repository): Iterable<Branch> =
        branchDao.findAll(repository).map { b ->
            branchMapper.toDomain(b)
        }

    override fun findAll(pageable: Pageable): Page<Branch> {
        TODO("Not yet implemented")
    }

    override fun findByName(name: String): Branch? {
        TODO("Not yet implemented")
    }
}
