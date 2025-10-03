package com.inso_world.binocular.infrastructure.sql.persistence.dao

import com.inso_world.binocular.core.persistence.exception.PersistenceException
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IUserDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity
import com.inso_world.binocular.infrastructure.sql.persistence.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Repository
import java.util.stream.Stream

@Repository
internal class UserDao(
    @Autowired
    private val repo: UserRepository,
) : SqlDao<UserEntity, Long>(),
    IUserDao {
    init {
        this.setClazz(UserEntity::class.java)
        this.setRepository(repo)
    }

    private object UserSpecification {
        fun hasRepository(repository: RepositoryEntity): Specification<UserEntity> =
            Specification { root, query, cb ->
                cb.equal(
                    root.get<RepositoryEntity>("repository").get<String>("local_path"),
                    repository.localPath,
                )
            }
    }

    override fun findAllByGitSignatureIn(emails: Collection<String>): Stream<UserEntity> = repo.findAllByEmailIn(emails)

    override fun findAll(repository: RepositoryEntity): Iterable<UserEntity> =
        this.repo.findAll(
            Specification.allOf(UserSpecification.hasRepository(repository)),
        )

    override fun findAllAsStream(repository: com.inso_world.binocular.model.Repository): Stream<UserEntity> {
        val rid = repository.id
        if (rid == null) throw PersistenceException("Cannot search for repo without valid ID")
        return this.repo.findAllByRepository_Id(rid.toLong())
    }
}
