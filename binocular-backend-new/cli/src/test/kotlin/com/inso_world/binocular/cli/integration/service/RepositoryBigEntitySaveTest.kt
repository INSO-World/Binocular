package com.inso_world.binocular.cli.integration.service

import com.inso_world.binocular.cli.service.ProjectService
import com.inso_world.binocular.cli.service.RepositoryService
import com.inso_world.binocular.infrastructure.arangodb.ArangodbTestConfig
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Revision
import com.inso_world.binocular.model.Signature
import com.inso_world.binocular.model.Stats
import com.inso_world.binocular.model.vcs.ReferenceCategory
import com.inso_world.binocular.model.vcs.Remote
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import java.time.LocalDateTime

@ActiveProfiles("arangodb", "test")
@SpringBootTest(
    classes = [ArangodbTestConfig::class, ProjectService::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ContextConfiguration(initializers = [ArangodbTestConfig.Initializer::class])
internal class RepositoryBigEntitySaveTest @Autowired constructor(
    private val repositoryService: RepositoryService,
    private val projectService: ProjectService
) {

    @Test
    fun `save repository with full graph of commits and developers`() {
        // 1. Setup: Ensure a Project exists
        val project = projectService.save(Project(name = "Big Repo Save Test Project"))

        // 2. Setup: Build the "Big Entity" in memory
        val repo = Repository(localPath = "/test/path/big-repo", project = project)
        project.repo = repo
        
        val dev = Developer(name = "Test Author", email = "author@test.com", repository = repo)
        val signature = Signature(developer = dev, timestamp = LocalDateTime.now().minusMinutes(5))
        
        val commit1 = Commit(
            sha = "0000000000000000000000000000000000000001", 
            message = "Root commit", 
            authorSignature = signature, 
            repository = repo
        ).apply {
            stats = Stats(additions = 10, deletions = 2, kind = Stats.StatsKind.ADDITION)
        }
        val commit2 = Commit(
            sha = "0000000000000000000000000000000000000002", 
            message = "Child commit", 
            authorSignature = signature, 
            repository = repo
        ).apply {
            stats = Stats(additions = 5, deletions = 5, kind = Stats.StatsKind.MODIFICATION)
        }
        
        // Add File and Revision
        val file1 = File(path = "src/Main.kt")
        val revision1 = Revision(content = "println(\"Hello\")", commit = commit1, file = file1)
        val revision2 = Revision(content = "println(\"Hello World\")", commit = commit2, file = file1)
        file1.revisions.addAll(listOf(revision1, revision2))

        // Add Remote
        val remote1 = Remote(
            name = "origin",
            url = "https://github.com/inso-world/binocular.git",
            repository = repo
        )
        
        // Establish a domain-level relationship
        commit2.parents.add(commit1)
        
        val branch = Branch(
            name = "main", 
            fullName = "refs/heads/main",
            category = ReferenceCategory.LOCAL_BRANCH,
            repository = repo,
            head = commit2
        )

        // The domain models auto-register in their init blocks, but for clarity in the test
        // TODO ???? idk
        // repo.add(commit1) 
        // repo.add(commit2)
        // repo.add(dev)
        // repo.add(branch)

        // 3. Execution: Call the single save entry point
        val savedRepo = repositoryService.save(repo)

        // 4. Verification
        assertAll(
            { assertThat(savedRepo.id).isNotNull() },
            { assertThat(savedRepo.commits).hasSize(2) },
            { assertThat(savedRepo.developers).hasSize(1) },
            { assertThat(savedRepo.branches).hasSize(1) },
            { assertThat(savedRepo.remotes).hasSize(1) },
            { 
                val dbCommit2 = savedRepo.commits.find { it.sha == "0000000000000000000000000000000000000002" }
                assertThat(dbCommit2).isNotNull
                assertThat(dbCommit2?.parents?.map { it.sha }).contains("0000000000000000000000000000000000000001") 
                assertThat(dbCommit2?.stats?.additions).isEqualTo(5)
            }
        )
    }
}
