package com.inso_world.binocular.cli.persistence.dao.sql

import com.inso_world.binocular.cli.entity.Branch
import com.inso_world.binocular.cli.persistence.dao.sql.interfaces.IBranchDao
import com.inso_world.binocular.cli.persistence.repository.sql.BranchRepository
import com.inso_world.binocular.core.persistence.dao.sql.SqlDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import kotlin.jvm.optionals.getOrNull

@Repository
class BranchDao(
    @Autowired private val branchRepository: BranchRepository
) : SqlDao<Branch, Long>(), IBranchDao {

    init {
        this.setClazz(Branch::class.java)
        this.setRepository(branchRepository)
    }

    override fun findByNameAndRepositoryId(name: String, repositoryId: Long): Branch? {
        return this.branchRepository.findByNameAndRepositoryId(name, repositoryId).getOrNull()
    }
} 