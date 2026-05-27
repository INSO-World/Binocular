package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
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
    private lateinit var ctx: MappingContext

    @Lazy
    @Autowired
    private lateinit var fileMapper: FileMapper

    @Lazy
    @Autowired
    private lateinit var commitMapper: CommitMapper

    override fun toEntity(domain: Revision): RevisionEntity {
        ctx.findEntity<String, Revision, RevisionEntity>(domain)?.let { return it }

        val entity = RevisionEntity(
            id = domain.id,
            content = domain.content,
            file = fileMapper.toEntity(domain.file),
            commit = commitMapper.toEntity(domain.commit)
        )

        ctx.remember(domain, entity)
        return entity
    }

    override fun toDomain(entity: RevisionEntity): Revision {
        ctx.findDomain<Revision, RevisionEntity>(entity)?.let { return it }

        val domain = Revision(
            content = entity.content ?: "",
            file = fileMapper.toDomain(entity.file),
            commit = commitMapper.toDomain(entity.commit)
        ).apply {
            id = entity.id
        }

        ctx.remember(domain, entity)
        return domain
    }

    override fun toDomainList(entities: Iterable<RevisionEntity>): List<Revision> = entities.map { toDomain(it) }
}
