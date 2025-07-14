package com.inso_world.binocular.infrastructure.sql.persistence.dao

import com.inso_world.binocular.core.persistence.exception.PersistenceException
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.repository.CommitRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.validation.annotation.Validated

/**
 * SQL implementation of ICommitDao.
 */
@Repository
@Validated
internal class CommitDao(
    @Autowired
    private val repo: CommitRepository,
) : SqlDao<CommitEntity, Long>(),
    ICommitDao {
    init {
        this.setClazz(CommitEntity::class.java)
        this.setRepository(repo)
    }

    override fun create(entity: CommitEntity): CommitEntity {
        val toSave =
            getManagedEntity(entity) ?: entity

        val managedRepo =
            toSave.repository?.let {
                entityManager.find(
                    it.javaClass,
                    it.id,
                )
            } ?: throw IllegalArgumentException("RepositoryEntity not found")
        toSave.repository = managedRepo
        if (!entityManager.contains(toSave)) {
            managedRepo.commits.add(toSave)
        }
        val managedParents =
            toSave.parents
                .map { parentEntity ->
                    getManagedEntity(parentEntity) ?: parentEntity
                }.map { parentEntity ->
                    parentEntity.repository = managedRepo
                    managedRepo.commits.add(parentEntity)
                    parentEntity
                }
        toSave.parents = managedParents

        val managedProject =
            entityManager.find(
                managedRepo.project.javaClass,
                managedRepo.project.id,
            )
        managedRepo.project = managedProject

        return super.create(toSave)
    }

//  TODO proper managed entity, missing parents here
    fun getManagedEntity(entity: CommitEntity): CommitEntity? {
        val managed =
            entity.id?.let {
                entityManager.find(CommitEntity::class.java, it)
            } ?: entity.repository?.let { repo ->
                findBySha(repo, entity.sha)
            }

//        man

        return managed ?: entity
    }

    override fun findExistingSha(
        repo: RepositoryEntity,
        shas: List<String>,
    ): Set<CommitEntity> {
        if (repo.id == null) throw PersistenceException("Cannot search for repo without valid ID")
        return this.repo.findAllByRepository_IdAndShaIn(repo.id!!, shas)
    }

    override fun findAllByRepo(
        repo: RepositoryEntity,
        pageable: Pageable,
    ): Iterable<CommitEntity> {
        if (repo.id == null) throw PersistenceException("Cannot search for repo without valid ID")
        return this.repo.findAllByRepository_Id(repo.id)
    }

    override fun findHeadForBranch(
        repo: RepositoryEntity,
        branch: String,
    ): CommitEntity? {
        if (repo.id == null) throw PersistenceException("Cannot search for repo without valid ID")
        return this.repo.findLeafCommitsByRepository(repo.id, branch)
    }

    override fun findAllLeafCommits(repo: RepositoryEntity): Iterable<CommitEntity> {
        if (repo.id == null) throw PersistenceException("Cannot search for repo without valid ID")
        return this.repo.findAllLeafCommits(repo.id)
    }

    override fun findBySha(
        repo: RepositoryEntity,
        sha: String,
    ): CommitEntity? {
        if (repo.id == null) throw PersistenceException("Cannot search for repo without valid ID")
        return this.repo.findByRepository_IdAndSha(repo.id, sha)
    }
}
