package com.inso_world.binocular.cli.persistence.dao.sql

import com.inso_world.binocular.cli.entity.User
import com.inso_world.binocular.cli.persistence.dao.sql.interfaces.IUserDao
import com.inso_world.binocular.cli.persistence.repository.sql.UserRepository
import com.inso_world.binocular.core.persistence.dao.sql.SqlDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.util.stream.Stream

@Repository
class UserDao(
    @Autowired private val userRepository: UserRepository,
) : SqlDao<User, String>(),
    IUserDao {
    init {
        this.setClazz(User::class.java)
        this.setRepository(userRepository)
    }

    override fun findAllByEmail(emails: Collection<String>): Stream<User> = this.userRepository.findAllByEmailIn(emails)
}
