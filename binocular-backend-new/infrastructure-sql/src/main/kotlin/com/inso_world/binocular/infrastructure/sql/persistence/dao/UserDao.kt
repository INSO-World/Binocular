package com.inso_world.binocular.infrastructure.sql.persistence.dao

import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IUserDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity
import org.springframework.stereotype.Repository

@Repository
internal class UserDao :
    SqlDao<UserEntity, Long>(),
    IUserDao {
    init {
        this.setClazz(UserEntity::class.java)
    }
}
