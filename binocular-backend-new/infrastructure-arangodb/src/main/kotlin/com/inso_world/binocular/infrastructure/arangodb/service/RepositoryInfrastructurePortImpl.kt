package com.inso_world.binocular.infrastructure.arangodb.service

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.CommitDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.CommitDiffDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.RepositoryDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.CommitDiffMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.RepositoryMapper
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.CommitDiff
import com.inso_world.binocular.model.Repository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
internal class RepositoryInfrastructurePortImpl : RepositoryInfrastructurePort {
    companion object {
        val logger by logger()
    }

    @Autowired
    private lateinit var commitDiffMapper: CommitDiffMapper

    @Autowired
    private lateinit var commitDiffDao: CommitDiffDao

    @Autowired
    private lateinit var commitDao: CommitDao

    @Autowired
    private lateinit var repositoryDao: RepositoryDao

    @Autowired
    private lateinit var repositoryMapper: RepositoryMapper

    override fun findByIid(iid: Repository.Id): Repository? {
        TODO("Not yet implemented")
    }

    override fun findAll(): Iterable<Repository> {
        return this.repositoryDao.findAll()
    }

    override fun findAll(pageable: Pageable): Page<Repository> {
        return this.repositoryDao.findAll(pageable)
    }

    override fun findById(id: String): Repository? {
        return this.repositoryDao.findById(id)
    }

    override fun create(value: Repository): Repository {
        val mappedEntity = repositoryMapper.toEntity(value)
        val savedEntity = this.repositoryDao.create(mappedEntity)
        return repositoryMapper.toDomain(savedEntity)
    }

    override fun saveAll(values: Collection<Repository>): Iterable<Repository> {
        return this.repositoryDao.saveAll(values)
    }

    override fun update(value: Repository): Repository {
        TODO("Not yet implemented")
    }

    override fun findByName(name: String): Repository? {
        return this.repositoryDao.findByName(name)?.let { this.repositoryMapper.toDomain(it) }
    }

    override fun findExistingCommits(repo: Repository, shas: Set<String>): Sequence<Commit> {
        TODO("Not yet implemented")
    }

    override fun saveCommitDiffs(
        repository: Repository,
        diffs: Set<CommitDiff>,
    ): Set<CommitDiff> {
//        val adbDiffs =
//            diffs.map {
//                val source = commitDao.findById(it.source.id!!) ?: throw IllegalStateException("Must find diff source")
//                val target = it.target?.id?.let { i -> commitDao.findById(i) }
//
//                CommitDiff(
//                    source = source,
//                    target = target,
//                    files = emptySet(),
//                    repository = null,
//                )
//            }

//        val mappedDiffs =
//            diffs
//                .map {
//                    val entity = commitDiffMapper.toEntity(it)
// //                    val fileDiffs =
// //                        it.files.map { fd -> fileDiffMapper.toEntity(fd) }.map { fd ->
// //                            fd.diffEntry = entity
// //                            fd
// //                        }
// //                    entity.files = fileDiffs.toSet()
//                    return@map entity
//                }.map {
// //                    it.repository = project.repo
//                    it
//                }
        return commitDiffDao
            .saveAll(diffs)
            .toSet()
    }

    override fun findAllDiffs(repository: Repository): Set<CommitDiff> = commitDiffDao.findAll().toSet()
}
