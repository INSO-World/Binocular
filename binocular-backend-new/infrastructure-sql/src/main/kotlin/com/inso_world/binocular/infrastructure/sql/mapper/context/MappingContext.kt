package com.inso_world.binocular.infrastructure.sql.mapper.context

import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
@Scope("mapping", proxyMode = ScopedProxyMode.TARGET_CLASS)
internal class MappingContext {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(MappingContext::class.java)
    }

    // entity → domain
    val domain = DomainMaps()

    // domain → entity
    val entity = EntityMaps()

    /** Clear all maps in this context */
    fun clear() {
        logger.debug("Clearing mapping context")
        domain.run {
            user.clear()
            commit.clear()
            branch.clear()
        }
        entity.run {
            user.clear()
            commit.clear()
            branch.clear()
        }
        logger.debug("Mapping context cleared")
    }

    class DomainMaps {
        val user = ConcurrentHashMap<String, User>()
        val commit = ConcurrentHashMap<String, Commit>()
        val branch = ConcurrentHashMap<String, Branch>()
    }

    class EntityMaps {
        val user = ConcurrentHashMap<String, UserEntity>()
        val commit = ConcurrentHashMap<String, CommitEntity>()
        val branch = ConcurrentHashMap<String, BranchEntity>()
    }
}
