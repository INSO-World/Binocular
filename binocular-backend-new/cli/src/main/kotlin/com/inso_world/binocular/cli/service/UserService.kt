package com.inso_world.binocular.cli.service

import com.inso_world.binocular.cli.entity.User
import com.inso_world.binocular.cli.index.vcs.VcsPerson
import com.inso_world.binocular.cli.persistence.dao.sql.interfaces.IUserDao
import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class UserService(
    @Autowired private val userDao: IUserDao,
) {
    private val logger: Logger = LoggerFactory.getLogger(CommitService::class.java)

    fun saveAll(people: Iterable<VcsPerson>): Iterable<User> {
        logger.trace("Saving all people...")

        val users = people.map { it.toEntity() }.stream().collect(Collectors.toSet())

        return userDao.saveAll(users)
    }

    @Transactional
    fun findAllUsersByEmails(emails: Collection<String>): Collection<User> {
        logger.trace("Finding users with emails {}", emails)

        return userDao.findAllByEmail(emails).collect(Collectors.toSet())
    }
}
