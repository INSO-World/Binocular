package com.inso_world.binocular.cli.persistence.dao.sql.interfaces

import com.inso_world.binocular.cli.entity.User
import com.inso_world.binocular.core.persistence.dao.interfaces.IDao
import java.util.stream.Stream

interface IUserDao : IDao<User, String> {
  fun findAllByEmail(emails: Collection<String>): Stream<User>
}
