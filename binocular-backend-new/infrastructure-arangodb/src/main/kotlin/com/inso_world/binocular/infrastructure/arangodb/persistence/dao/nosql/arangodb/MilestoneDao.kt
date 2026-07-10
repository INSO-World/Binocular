package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IMilestoneDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.MilestoneEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.MilestoneMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.MilestoneRepository
import com.inso_world.binocular.model.Milestone
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of [IMilestoneDao].
 *
 * Overrides [create] to fetch and wire the parent project directly from the database
 * before saving. [MilestoneMapper.toEntity] resolves the project via MappingContext,
 * which returns null in standalone save sessions (each [saveAll] call starts a fresh
 * session with no projects seeded). Fetching it explicitly here ensures the saved
 * document carries the correct project reference and that [MilestoneMapper.toDomain]
 * can reconstruct the domain object from the returned entity.
 */
@Repository
internal class MilestoneDao
    @Autowired
    constructor(
        milestoneRepository: MilestoneRepository,
        milestoneMapper: MilestoneMapper,
    ) : MappedArangoDbDao<Milestone, MilestoneEntity, String>(milestoneRepository, milestoneMapper),
        IMilestoneDao {
        @Autowired
        @Lazy
        private lateinit var projectDao: ProjectDao

        /**
         * Creates a milestone, resolving the parent project from the database before saving.
         *
         * The project is fetched via [ProjectDao.findEntityByIid] and set on the entity so
         * the ArangoDB document stores the `@Ref` and [MilestoneMapper.toDomain] can read it
         * back from the returned saved entity.
         *
         * @param entity the milestone domain object to persist
         * @return the persisted milestone domain object
         * @throws IllegalArgumentException if no project matching [Milestone.project] exists
         */
        override fun create(entity: Milestone): Milestone {
            val mappedEntity =
                mapper.toEntity(entity).apply {
                    project =
                        requireNotNull(projectDao.findEntityByIid(entity.project)) {
                            "Project with ID ${entity.project} not found for milestone ${entity.platformIid}"
                        }
                }
            val savedEntity = repository.save(mappedEntity)
            return mapper.toDomain(savedEntity)
        }
    }
