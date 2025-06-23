package com.inso_world.binocular.cli.service

import com.inso_world.binocular.cli.entity.Branch
import com.inso_world.binocular.cli.entity.Repository
import com.inso_world.binocular.cli.persistence.dao.sql.interfaces.IBranchDao
import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class BranchService(
    @Autowired private val branchDao: IBranchDao,
) {
    private val logger: Logger = LoggerFactory.getLogger(BranchService::class.java)

    fun findBranch(
        repository: Repository,
        branchName: String,
    ): Branch? {
        val repoId = repository.id ?: throw IllegalStateException("Repository must have an ID to find a branch.")
        return branchDao.findByNameAndRepositoryId(branchName, repoId)
    }

    @Transactional
    fun getOrCreate(
        repository: Repository,
        branchName: String,
    ): Branch {
        val existingBranch = this.findBranch(repository, branchName)
        if (existingBranch != null) {
            logger.debug("Branch '$branchName' already exists for repository ${repository.name}. Returning existing branch.")
            return existingBranch
        }

        logger.debug("Branch '$branchName' does not exist for repository ${repository.name}. Creating new branch.")
        val newBranch = Branch(name = branchName, repository = repository)
        return branchDao.create(newBranch)
    }
}
