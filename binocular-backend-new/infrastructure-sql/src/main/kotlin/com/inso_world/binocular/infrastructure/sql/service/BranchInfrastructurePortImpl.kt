package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IBranchDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.mapper.BranchMapper
import com.inso_world.binocular.infrastructure.sql.persistence.mapper.RepositoryMapper
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
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

    override fun findAll(): Iterable<Branch> {
        val context: MutableMap<Long, Repository> = mutableMapOf()
        val commitContext = mutableMapOf<String, Commit>()
        val branchContext = mutableMapOf<String, Branch>()
        val userContext = mutableMapOf<String, User>()

        return super<AbstractInfrastructurePort>.findAllEntities().map { b ->
            val repo =
                context.getOrPut(b.repository?.id!!) {
                    this.repositoryMapper.toDomain(b.repository!!, commitContext, branchContext, userContext)
                }
            branchMapper.toDomain(b, repo, commitContext, branchContext)
        }
    }

    override fun findAll(pageable: Pageable): Page<Branch> {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: String) {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        super.deleteAllEntities()
    }
}
