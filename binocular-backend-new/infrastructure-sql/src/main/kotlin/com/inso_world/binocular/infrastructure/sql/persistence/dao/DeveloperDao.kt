package com.inso_world.binocular.infrastructure.sql.persistence.dao

import com.inso_world.binocular.core.persistence.exception.PersistenceException
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IDeveloperDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.DeveloperEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.repository.DeveloperRepository
import com.inso_world.binocular.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Repository
import java.util.stream.Stream
import kotlin.uuid.ExperimentalUuidApi
import com.inso_world.binocular.model.Repository as DomainRepository

@Repository
internal class DeveloperDao(
    @Autowired
    private val repo: DeveloperRepository,
) : SqlDao<DeveloperEntity, Long>(),
    IDeveloperDao {
    init {
        this.setClazz(DeveloperEntity::class.java)
        this.setRepository(repo)
    }

    private object DeveloperSpecification {
        fun hasRepository(repository: RepositoryEntity): Specification<DeveloperEntity> =
            Specification { root, _, cb ->
                cb.equal(
                    root.get<RepositoryEntity>("repository").get<String>("local_path"),
                    repository.localPath,
                )
            }
    }

    override fun findAllByGitSignatureIn(emails: Collection<String>): Stream<DeveloperEntity> = repo.findAllByEmailIn(emails)

    override fun findAll(repository: RepositoryEntity): Iterable<DeveloperEntity> =
        this.repo.findAll(
            Specification.allOf(DeveloperSpecification.hasRepository(repository)),
        )

    override fun findAllAsStream(repository: DomainRepository): Stream<DeveloperEntity> {
        val rid = repository.id
        if (rid == null) throw PersistenceException("Cannot search for repo without valid ID")
        return this.repo.findAllByRepository_Id(rid.toLong())
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun findByIid(iid: User.Id): DeveloperEntity? = repo.findByIid(iid.value)
}
