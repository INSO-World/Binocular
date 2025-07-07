package com.inso_world.binocular.cli.service

import com.inso_world.binocular.cli.entity.User
import com.inso_world.binocular.core.service.UserInfrastructurePort
import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserService(
    @Autowired private val userPort: UserInfrastructurePort,
) {
    private val logger: Logger = LoggerFactory.getLogger(CommitService::class.java)

//    fun saveAll(people: Iterable<VcsPerson>): Iterable<User> {
//        logger.trace("Saving all people...")
//
//        val users = people.map { it.toEntity() }.stream().collect(Collectors.toSet())
//
//        return userSerivce.saveAll(users)
//    }

    @Transactional
    fun findAllUsersByEmails(emails: Collection<String>): Collection<User> {
        logger.trace("Finding users with emails {}", emails)

        return userPort.findAll().filter { emails.contains(it.gitSignature) }.map {
            User()
        }

//        return userDao.findAllByEmail(emails).collect(Collectors.toSet())
//        return userSerivce.findAll().filter { it.gitSignature.in }
    }
}
