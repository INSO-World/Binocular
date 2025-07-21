package com.inso_world.binocular.cli.service

import com.inso_world.binocular.core.service.UserInfrastructurePort
import com.inso_world.binocular.model.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserService(
    @Autowired private val userPort: UserInfrastructurePort,
) {
    private val logger: Logger = LoggerFactory.getLogger(UserService::class.java)

//    fun saveAll(people: Iterable<VcsPerson>): Iterable<User> {
//        logger.trace("Saving all people...")
//
//        val users = people.map { it.toEntity() }.stream().collect(Collectors.toSet())
//
//        return userSerivce.saveAll(users)
//    }

    fun findAllUsersByEmails(emails: Collection<String>): Collection<User> {
        logger.trace("Finding users with emails {}", emails)

        return userPort.findAll().filter { emails.contains(it.email) }
    }
}
