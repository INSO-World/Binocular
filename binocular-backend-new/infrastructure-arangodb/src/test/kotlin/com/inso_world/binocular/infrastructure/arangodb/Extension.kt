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
import org.springframework.transaction.annotation.Transactional
import java.io.Serializable

@Transactional
internal fun <T : Any, I : Serializable> AbstractInfrastructurePort<T, I>.deleteAllEntities() {
    this.dao.deleteAll()
}

// Extension functions for specific implementation classes
// These are needed because the implementations don't extend AbstractInfrastructurePort

@Transactional
internal fun CommitInfrastructurePortImpl.deleteAllEntities() {
    this.deleteAll()
}

@Transactional
internal fun AccountInfrastructurePortImpl.deleteAllEntities() {
    this.deleteAll()
}

@Transactional
internal fun BranchInfrastructurePortImpl.deleteAllEntities() {
    this.deleteAll()
}

@Transactional
internal fun BuildInfrastructurePortImpl.deleteAllEntities() {
    this.deleteAll()
}

@Transactional
internal fun FileInfrastructurePortImpl.deleteAllEntities() {
    this.deleteAll()
}

@Transactional
internal fun IssueInfrastructurePortImpl.deleteAllEntities() {
    this.deleteAll()
}

@Transactional
internal fun MergeRequestInfrastructurePortImpl.deleteAllEntities() {
    this.deleteAll()
}

@Transactional
internal fun MilestoneInfrastructurePortImpl.deleteAllEntities() {
    this.deleteAll()
}

@Transactional
internal fun ModuleInfrastructurePortImpl.deleteAllEntities() {
    this.deleteAll()
}

@Transactional
internal fun NoteInfrastructurePortImpl.deleteAllEntities() {
    this.deleteAll()
}

@Transactional
internal fun UserInfrastructurePortImpl.deleteAllEntities() {
    this.deleteAll()
}

@Transactional
internal fun ProjectInfrastructurePortImpl.deleteAllEntities() {
    this.deleteAll()
}

@Transactional
internal fun RepositoryInfrastructurePortImpl.deleteAllEntities() {
    this.deleteAll()
}
