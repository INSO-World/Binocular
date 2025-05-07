package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.persistence.dao.interfaces.IUserDao
import com.inso_world.binocular.web.persistence.repository.arangodb.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class UserDao(
  @Autowired private val userRepository: UserRepository
) : ArangoDbDao<User, String>(), IUserDao {

  init {
    this.setClazz(User::class.java)
    this.setRepository(userRepository)
  }
}
