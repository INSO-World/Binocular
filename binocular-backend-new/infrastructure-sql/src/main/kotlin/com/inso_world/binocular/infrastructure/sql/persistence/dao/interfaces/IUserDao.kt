package com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces

import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity
import com.inso_world.binocular.model.Repository
import java.util.stream.Stream

internal interface IUserDao : IDao<UserEntity, Long> {
    fun findAllByGitSignatureIn(emails: Collection<String>): Stream<UserEntity>

    fun findAll(repository: RepositoryEntity): Iterable<UserEntity>

    fun findAllAsStream(repository: Repository): Stream<UserEntity>
}
