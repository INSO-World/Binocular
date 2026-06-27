package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IMergeRequestDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.MergeRequestEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.MergeRequestMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.MergeRequestRepository
import com.inso_world.binocular.model.MergeRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

@Repository
internal class MergeRequestDao(
    @Autowired private val mergeRequestRepository: MergeRequestRepository,
    @Autowired private val mergeRequestMapper: MergeRequestMapper,
) : MappedArangoDbDao<MergeRequest, MergeRequestEntity, String>(mergeRequestRepository, mergeRequestMapper),
    IMergeRequestDao {
    @Autowired
    @Lazy
    private lateinit var projectDao: ProjectDao

    override fun findAll(
        pageable: Pageable,
        since: Long?,
        until: Long?
    ): Page<MergeRequest> {
        if (since == null && until == null) {
            return super.findAll(pageable)
        }
        val offset = pageable.offset.toInt()
        val size = pageable.pageSize
        val firstOrder = pageable.sort.firstOrNull()
        val asc = firstOrder?.direction == Sort.Direction.ASC

        val entities =
            when {
                since != null && until != null -> {
                    if (asc) {
                        mergeRequestRepository.findAllBetweenAsc(offset, size, since, until)
                    } else {
                        mergeRequestRepository.findAllBetweenDesc(offset, size, since, until)
                    }
                }

                since != null -> {
                    if (asc) {
                        mergeRequestRepository.findAllSinceAsc(offset, size, since)
                    } else {
                        mergeRequestRepository.findAllSinceDesc(offset, size, since)
                    }
                }

                else -> {
                    if (asc) {
                        mergeRequestRepository.findAllUntilAsc(offset, size, until!!)
                    } else {
                        mergeRequestRepository.findAllUntilDesc(offset, size, until!!)
                    }
                }
            }

        val content = entities.map { mergeRequestMapper.toDomain(it) }
        val total =
            when {
                since != null && until != null -> mergeRequestRepository.countAllBetween(since, until)
                since != null -> mergeRequestRepository.countAllSince(since)
                else -> mergeRequestRepository.countAllUntil(until!!)
            }
        return Page(content, total, pageable)
    }

    override fun create(entity: MergeRequest): MergeRequest {
        val mappedEntity =
            mapper.toEntity(entity).apply {
                project =
                    requireNotNull(projectDao.findEntityByIid(entity.project)) {
                        "Project with ID ${entity.project} not found for issue ${entity.iid}"
                    }
            }

        val savedEntity = this.create(mappedEntity)

        return mapper.toDomain(savedEntity)
    }

    override fun create(mergeRequest: MergeRequestEntity): MergeRequestEntity = this.mergeRequestRepository.save(mergeRequest)
}
