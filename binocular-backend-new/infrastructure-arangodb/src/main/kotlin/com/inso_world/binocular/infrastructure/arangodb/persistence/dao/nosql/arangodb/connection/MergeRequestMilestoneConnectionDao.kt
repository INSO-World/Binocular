package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.connection

import com.inso_world.binocular.infrastructure.arangodb.model.edge.MergeRequestMilestoneConnection
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.edge.IMergeRequestMilestoneConnectionDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.edges.MergeRequestMilestoneConnectionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.MergeRequestMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.MilestoneMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.MergeRequestRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.MilestoneRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.edges.MergeRequestMilestoneConnectionRepository
import com.inso_world.binocular.model.MergeRequest
import com.inso_world.binocular.model.Milestone
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of IMergeRequestMilestoneConnectionDao.
 *
 * This class adapts the existing MergeRequestMilestoneConnectionRepository to work with
 * the new domain model and entity structure.
 */

@Repository
internal class MergeRequestMilestoneConnectionDao
    @Autowired
    constructor(
        private val repository: MergeRequestMilestoneConnectionRepository,
        private val mergeRequestRepository: MergeRequestRepository,
        private val milestoneRepository: MilestoneRepository,
        private val mergeRequestMapper: MergeRequestMapper,
        private val milestoneMapper: MilestoneMapper,
    ) : IMergeRequestMilestoneConnectionDao {
        /**
         * Find all milestones connected to a merge request
         */
        override fun findMilestonesByMergeRequest(mergeRequestId: String): List<Milestone> {
            val milestoneEntities = repository.findMilestonesByMergeRequest(mergeRequestId)
            return milestoneEntities.map { milestoneMapper.toDomain(it) }
        }

        /**
         * Find all merge requests connected to a milestone
         */
        override fun findMergeRequestsByMilestone(milestoneId: String): List<MergeRequest> {
            val mergeRequestEntities = repository.findMergeRequestsByMilestone(milestoneId)
            return mergeRequestEntities.map { mergeRequestMapper.toDomain(it) }
        }

        /**
         * Save a merge request-milestone connection
         */
        override fun save(connection: MergeRequestMilestoneConnection): MergeRequestMilestoneConnection {
            // Get the merge request and milestone entities from their repositories
            val mergeRequestEntity =
                mergeRequestRepository.findById(connection.from.id!!).orElseThrow {
                    IllegalArgumentException("MergeRequest with ID ${connection.from.id} not found")
                }
            val milestoneEntity =
                milestoneRepository.findById(connection.to.id!!).orElseThrow {
                    IllegalArgumentException("Milestone with ID ${connection.to.id} not found")
                }

            // Convert domain model to the entity format
            val entity =
                MergeRequestMilestoneConnectionEntity(
                    id = connection.id,
                    from = mergeRequestEntity,
                    to = milestoneEntity,
                )

            // Save using the repository
            val savedEntity = repository.save(entity)

            // Convert back to domain model
            return MergeRequestMilestoneConnection(
                id = savedEntity.id,
                from = mergeRequestMapper.toDomain(savedEntity.from),
                to = milestoneMapper.toDomain(savedEntity.to),
            )
        }

        /**
         * Delete all merge request-milestone connections
         */
        override fun deleteAll() {
            repository.deleteAll()
        }
    }
