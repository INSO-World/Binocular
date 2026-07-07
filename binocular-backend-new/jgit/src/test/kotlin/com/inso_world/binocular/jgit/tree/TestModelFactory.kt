package com.inso_world.binocular.jgit.tree

import com.inso_world.binocular.model.*
import com.inso_world.binocular.model.vcs.ReferenceCategory
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
object TestModelFactory {
    @JvmStatic
    fun createProject(name: String, iid: Project.Id = Project.Id(kotlin.uuid.Uuid.random())): Project = Project(name, iid = iid)

    @JvmStatic
    fun createProjectWithRepo(name: String, iid: Project.Id = Project.Id(kotlin.uuid.Uuid.random())): Project {
        val p = Project(name, iid = iid)
        val r = Repository("test-path", projectId = p.iid)
        r.project = p
        return p
    }

    @JvmStatic
    fun createRepository(localPath: String, projectId: Project.Id, iid: Repository.Id = Repository.Id(kotlin.uuid.Uuid.random())): Repository = 
        Repository(localPath, projectId = projectId, iid = iid)

    @JvmStatic
    fun createRepository(localPath: String, project: Project, iid: Repository.Id = Repository.Id(kotlin.uuid.Uuid.random())): Repository =
        Repository(localPath, projectId = project.iid, iid = iid).apply { this.project = project }

    @JvmStatic
    fun createDeveloper(name: String, email: String, repositoryId: Repository.Id, iid: Developer.Id = Developer.Id(kotlin.uuid.Uuid.random())): Developer = 
        Developer(name = name, email = email, repositoryId = repositoryId, iid = iid)

    @JvmStatic
    fun createDeveloper(name: String, email: String, repository: Repository, iid: Developer.Id = Developer.Id(kotlin.uuid.Uuid.random())): Developer =
        Developer(name = name, email = email, repositoryId = repository.iid, iid = iid).apply { this.repository = repository }

    @JvmStatic
    fun createSignature(developerId: Developer.Id, timestamp: LocalDateTime): Signature = 
        Signature(developerId = developerId, timestamp = timestamp)

    @JvmStatic
    fun createCommit(sha: String, authorSignature: Signature, committerSignature: Signature, message: String?, repositoryId: Repository.Id, parentShas: Set<String> = emptySet(), iid: Commit.Id = Commit.Id(kotlin.uuid.Uuid.random())): Commit {
        val c = Commit(sha = sha, authorSignature = authorSignature, committerSignature = committerSignature, message = message, repositoryId = repositoryId, iid = iid)
        c.parentShas.addAll(parentShas)
        return c
    }

    @JvmStatic
    fun createCommit(sha: String, authorSignature: Signature, committerSignature: Signature, message: String?, repository: Repository, parentShas: Set<String> = emptySet(), iid: Commit.Id = Commit.Id(kotlin.uuid.Uuid.random())): Commit {
        val c = Commit(sha = sha, authorSignature = authorSignature, committerSignature = committerSignature, message = message, repositoryId = repository.iid, iid = iid).apply { this.repository = repository }
        c.parentShas.addAll(parentShas)
        return c
    }

    @JvmStatic
    fun createBranch(name: String, fullName: String, repositoryId: Repository.Id, headSha: String, iid: Reference.Id = Reference.Id(kotlin.uuid.Uuid.random())): Branch =
        Branch(name = name, fullName = fullName, category = ReferenceCategory.LOCAL_BRANCH, repositoryId = repositoryId, headSha = headSha, iid = iid)

    @JvmStatic
    fun createBranch(name: String, fullName: String, repository: Repository, headSha: String, iid: Reference.Id = Reference.Id(kotlin.uuid.Uuid.random())): Branch =
        Branch(name = name, fullName = fullName, category = ReferenceCategory.LOCAL_BRANCH, repositoryId = repository.iid, headSha = headSha, iid = iid).apply { this.repository = repository }
}
