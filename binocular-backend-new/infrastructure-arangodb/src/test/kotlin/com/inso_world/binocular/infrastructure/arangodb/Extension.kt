package com.inso_world.binocular.infrastructure.arangodb

import com.inso_world.binocular.infrastructure.arangodb.service.AbstractInfrastructurePort
import com.inso_world.binocular.infrastructure.arangodb.service.AccountInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.arangodb.service.BranchInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.arangodb.service.BuildInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.arangodb.service.CommitInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.arangodb.service.FileInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.arangodb.service.IssueInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.arangodb.service.MergeRequestInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.arangodb.service.MilestoneInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.arangodb.service.ModuleInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.arangodb.service.NoteInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.arangodb.service.ProjectInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.arangodb.service.RepositoryInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.arangodb.service.UserInfrastructurePortImpl
import org.springframework.aop.framework.Advised
import org.springframework.transaction.annotation.Transactional
import java.io.Serializable

/**
 * Deletes all entities stored by this port.
 *
 * Unwraps a Spring AOP proxy (if the receiver is a proxy) before accessing
 * [AbstractInfrastructurePort.dao], because CGLIB proxies created by
 * `@MappingSession` skip constructor/init — the proxy's copy of the `lateinit`
 * field is never assigned on the proxy instance.
 */
@Suppress("UNCHECKED_CAST")
@Transactional
internal fun <T : Any, I : Serializable> AbstractInfrastructurePort<T, I>.deleteAllEntities() {
    val target = (this as? Advised)?.targetSource?.target as? AbstractInfrastructurePort<T, I>
        ?: this
    target.dao.deleteAll()
}

// Extension functions for specific implementation classes
// These are needed because the implementations don't extend AbstractInfrastructurePort
@Deprecated("should use AbstractInfrastructurePort<T, I>.deleteAllEntities")
@Transactional
internal fun AccountInfrastructurePortImpl.deleteAllEntities() {
    this.deleteAll()
}

@Deprecated("should use AbstractInfrastructurePort<T, I>.deleteAllEntities")
@Transactional
internal fun BranchInfrastructurePortImpl.deleteAllEntities() {
    this.deleteAll()
}

@Deprecated("should use AbstractInfrastructurePort<T, I>.deleteAllEntities")
@Transactional
internal fun IssueInfrastructurePortImpl.deleteAllEntities() {
    this.deleteAll()
}
