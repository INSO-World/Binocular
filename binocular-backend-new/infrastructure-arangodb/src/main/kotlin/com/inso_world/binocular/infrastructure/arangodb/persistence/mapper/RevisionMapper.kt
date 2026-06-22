package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RevisionEntity
import com.inso_world.binocular.model.Revision
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

/**
 * Mapper for Revision domain objects.
 */
@Component
internal class RevisionMapper : EntityMapper<Revision, RevisionEntity> {

    @Autowired

    @Lazy
    @Autowired
    private lateinit var fileMapper: FileMapper

    @Lazy
    @Autowired
    private lateinit var commitMapper: CommitMapper

    override fun toEntity(domain: Revision): RevisionEntity {

        val entity = RevisionEntity(
            id = domain.id,
            content = domain.content,
            file = fileMapper.toEntity(domain.file),
            commit = commitMapper.toEntity(domain.commit)
        )

        return entity
    }

    override fun toDomain(entity: RevisionEntity): Revision {

        val domain = Revision(
            content = entity.content ?: "",
            file = fileMapper.toDomain(entity.file),
            commit = commitMapper.toDomain(entity.commit)
        ).apply {
            id = entity.id
        }

        return domain
    }

    override fun toDomainList(entities: Iterable<RevisionEntity>): List<Revision> = entities.map { toDomain(it) }
}
