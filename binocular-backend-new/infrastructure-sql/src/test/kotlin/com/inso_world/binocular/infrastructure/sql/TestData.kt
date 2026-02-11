package com.inso_world.binocular.infrastructure.sql

import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.DeveloperEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RemoteEntity
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Reference
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import com.inso_world.binocular.model.User
import com.inso_world.binocular.model.vcs.Remote
import com.inso_world.binocular.model.vcs.ReferenceCategory
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal object TestData {
    object Entity {
        /**
         * Creates a fresh ProjectEntity with standard test data.
         *
         * @param name Entity name (default: "test project")
         * @param id Database-specific id (default: 1L)
         * @param description Entity description (default: "this is a description")
         * @param iid Immutable identity (default: random UUID)
         * @return A new ProjectEntity instance
         */
        fun testProjectEntity(
            name: String = "test project",
            id: Long? = 1L,
            description: String? = "this is a description",
            iid: Project.Id = Project.Id(Uuid.random())
        ): ProjectEntity = ProjectEntity(
            name = name,
            iid = iid
        ).apply {
            this.description = description
            this.id = id
        }

        /**
         * Creates a test CommitEntity persistence entity with customizable parameters.
         *
         * @param sha The SHA hash of the commit. Must be 40 characters long.
         * @param authorDateTime The timestamp when the commit was authored.
         * @param commitDateTime The timestamp when the commit was committed.
         * @param message The commit message.
         * @param repository The RepositoryEntity this commit belongs to.
         * @param iid The internal immutable identifier.
         * @param id The Long database identifier, or null.
         * @return A CommitEntity configured with the specified values.
         */
        fun testCommitEntity(
            sha: String,
            authorDateTime: LocalDateTime,
            commitDateTime: LocalDateTime = authorDateTime,
            message: String?,
            repository: RepositoryEntity,
            author: DeveloperEntity = testDeveloperEntity(
                name = "Author-${sha.take(6)}",
                email = "author-${sha.take(6)}@example.com",
                repository = repository,
            ),
            committer: DeveloperEntity? = testDeveloperEntity(
                name = "Committer-${sha.take(6)}",
                email = "${sha.take(6)}@example.com",
                repository = repository,
            ),
            iid: Commit.Id = Commit.Id(Uuid.random()),
            id: Long? = null,
        ): CommitEntity = CommitEntity(
            sha = sha,
            authorDateTime = authorDateTime,
            commitDateTime = commitDateTime,
            message = message,
            repository = repository,
            iid = iid,
            author = author,
            committer = committer ?: author
        ).apply {
            this.id = id
        }

        /**
         * Creates a test DeveloperEntity persistence entity with customizable parameters.
         *
         * @param name The name of the developer.
         * @param email The email address of the developer.
         * @param repository The RepositoryEntity this developer belongs to.
         * @param iid The internal immutable identifier.
         * @param id The Long database identifier, or null.
         * @return A DeveloperEntity configured with the specified values.
         */
        fun testDeveloperEntity(
            name: String,
            email: String,
            repository: RepositoryEntity,
            iid: Developer.Id = Developer.Id(Uuid.random()),
            id: Long? = null
        ): DeveloperEntity = DeveloperEntity(
            name = name,
            email = email,
            repository = repository,
            iid = iid
        ).apply {
            this.id = id
        }

        @Deprecated("Use testDeveloperEntity", ReplaceWith("testDeveloperEntity(name,email,repository,iid,id)"))
        fun testUserEntity(
            name: String,
            email: String,
            repository: RepositoryEntity,
            iid: Developer.Id = Developer.Id(Uuid.random()),
            id: Long? = null
        ): DeveloperEntity = testDeveloperEntity(name, email, repository, iid, id)

        /**
         * Creates a test RepositoryEntity persistence entity with default or customizable parameters.
         *
         * This factory method provides a convenient way to create RepositoryEntity instances for testing,
         * with sensible defaults that can be overridden for specific test cases.
         *
         * @param localPath The local file system path to the repository. Defaults to "TestRepository".
         * @param id The Long database identifier, or null. Defaults to 1L.
         * @param iid The internal immutable identifier. Defaults to a new random Repository.Id.
         * @param project The ProjectEntity that owns this repository. Defaults to a minimal test project entity.
         * @return A RepositoryEntity configured with the specified or default values.
         *
         */
        fun testRepositoryEntity(
            localPath: String = "TestRepository",
            id: Long? = 1L,
            iid: Repository.Id = Repository.Id(Uuid.random()),
            project: ProjectEntity = testProjectEntity(
                name = "TestProject",
                id = 1L,
                description = "A test project"
            )
        ): RepositoryEntity = RepositoryEntity(
            iid = iid,
            localPath = localPath,
            project = project
        ).apply {
            this.id = id
        }

        /**
         * Creates a test BranchEntity persistence entity with customizable parameters.
         *
         * @param name The name of the branch.
         * @param repository The RepositoryEntity this branch belongs to.
         * @param head The CommitEntity that is the head of this branch.
         * @param iid The internal immutable identifier.
         * @param id The Long database identifier, or null.
         * @return A BranchEntity configured with the specified values.
         */
        fun testBranchEntity(
            name: String,
            repository: RepositoryEntity,
            head: CommitEntity,
            fullName: String = name,
            category: ReferenceCategory = ReferenceCategory.LOCAL_BRANCH,
            iid: Reference.Id = Reference.Id(Uuid.random()),
            id: Long? = null
        ): BranchEntity = BranchEntity(
            name = name,
            fullName = fullName,
            category = category,
            repository = repository,
            head = head,
            iid = iid
        ).apply {
            this.id = id
        }

        fun testRemoteEntity(
            name: String,
            url: String,
            repository: RepositoryEntity,
            iid: Remote.Id = Remote.Id(Uuid.random()),
            id: Long? = null
        ): RemoteEntity = RemoteEntity(
            name = name,
            url = url,
            repository = repository,
            iid = iid
        ).apply {
            this.id = id
        }
    }

    object Domain {
        /**
         * Creates a fresh Project domain object with standard test data.
         *
         * @param name Project name (default: "test project")
         * @param id Database-specific id (default: null)
         * @param description Project description (default: "this is a description")
         * @return A new Project instance
         */
        fun testProject(
            name: String = "test project",
            id: String? = null,
            description: String? = "this is a description"
        ): Project = Project(name = name).apply {
            this.id = id
            this.description = description
        }

        /**
         * Creates a test Commit domain object with customizable parameters.
         *
         * @param sha The SHA hash of the commit. Must be 40 characters long.
         * @param authorDateTime The timestamp when the commit was authored.
         * @param commitDateTime The timestamp when the commit was committed.
         * @param message The commit message.
         * @param repository The Repository this commit belongs to.
         * @param committer The User who committed this change.
         * @param id The string identifier for the commit, or null.
         * @return A Commit domain object configured with the specified values.
         */
        fun testCommit(
            sha: String,
            authorDateTime: LocalDateTime,
            commitDateTime: LocalDateTime?,
            message: String?,
            repository: Repository,
            author: Developer = testDeveloper(
                name = "Author-${sha.take(6)}",
                email = "author-${sha.take(6)}@example.com",
                repository = repository
            ),
            committer: Developer = author,
            id: String? = null,
        ): Commit {
            val authorSignature = Signature(developer = author, timestamp = authorDateTime)
            val committerSignature = commitDateTime?.let { Signature(developer = committer, timestamp = it) } ?: authorSignature

            return Commit(
                sha = sha,
                authorSignature = authorSignature,
                committerSignature = committerSignature,
                message = message,
                repository = repository,
            ).apply {
                this.id = id
            }
        }

        /**
         * Creates a test Repository domain object with default or customizable parameters.
         *
         * This factory method provides a convenient way to create Repository instances for testing,
         * with sensible defaults that can be overridden for specific test cases.
         *
         * @param localPath The local file system path to the repository. Defaults to "TestRepo".
         * @param id The string identifier for the repository, or null. Defaults to "10".
         * @param project The Project that owns this repository. Defaults to a minimal test project.
         * @return A Repository domain object configured with the specified or default values.
         *
         */
        fun testRepository(
            localPath: String = "TestRepo",
            id: String? = "10",
            project: Project = testProject(
                name = "TestProject",
                id = "1",
                description = "A test project"
            )
        ): Repository = Repository(
            localPath = localPath,
            project = project
        ).apply {
            this.id = id
        }

        /**
         * Creates a test Developer domain object with customizable parameters.
         *
         * @param name The name of the developer.
         * @param email The email address of the developer.
         * @param repository The Repository this developer belongs to.
         * @param id The string identifier for the developer, or null.
         * @return A Developer domain object configured with the specified values.
         */
        fun testDeveloper(
            name: String,
            email: String,
            repository: Repository,
            id: String? = null
        ): Developer =
            Developer(
                name = name,
                email = email,
                repository = repository
            ).apply {
                this.id = id
            }

        @Deprecated("Use testDeveloper", ReplaceWith("testDeveloper(name,email,repository,id)"))
        fun testUser(
            name: String,
            email: String,
            repository: Repository,
            id: String? = null
        ): Developer = testDeveloper(name, email, repository, id)

        /**
         * Creates a test Branch domain object with customizable parameters.
         *
         * @param name The name of the branch.
         * @param repository The Repository this branch belongs to.
         * @param head The Commit that is the head of this branch.
         * @param id The string identifier for the branch, or null.
         * @return A Branch domain object configured with the specified values.
         */
        fun testBranch(
            name: String,
            repository: Repository,
            head: Commit,
            fullName: String = name,
            category: ReferenceCategory = ReferenceCategory.LOCAL_BRANCH,
            id: String? = null
        ): Branch = Branch(
            name = name,
            fullName = fullName,
            category = category,
            repository = repository,
            head = head
        ).apply {
            this.id = id
        }

        fun testRemote(
            name: String = "origin",
            url: String = "https://example.com/repo.git",
            repository: Repository,
            id: String? = null
        ): Remote = Remote(
            name = name,
            url = url,
            repository = repository
        ).apply {
            this.id = id
        }
    }
}
