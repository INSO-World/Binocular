package com.inso_world.binocular.infrastructure.sql.persistence.dao

import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IUserDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity
import com.inso_world.binocular.infrastructure.sql.persistence.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
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

    override fun findAllByGitSignatureIn(emails: Collection<String>): Stream<UserEntity> = repo.findAllByEmailIn(emails)
}
