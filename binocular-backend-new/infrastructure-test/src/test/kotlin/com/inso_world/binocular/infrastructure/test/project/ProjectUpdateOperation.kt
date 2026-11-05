//package com.inso_world.binocular.infrastructure.test.project
//
//import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
//import com.inso_world.binocular.model.Project
//import org.junit.jupiter.api.Assertions.assertDoesNotThrow
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//
//internal class ProjectUpdateOperation : BaseInfrastructureSpringTest() {
//    private lateinit var existingProject: Project
//
//    @BeforeEach
//    fun setup() {
//        val p = Project(
//            name = "test project",
//            repo = repository
//        )
//        existingProject = projectPort.create(p)
//    }
//
//    @Test
//    fun `update project, unchanged, should not fail`() {
//        assertDoesNotThrow {
//            projectPort.update(existingProject)
//        }
//
//        assertAll(
//            "check database numbers",
//            { assertThat(projectPort.findAll()).hasSize(1) },
//            { assertThat(repositoryPort.findAll()).hasSize(1) },
//            { assertThat(commitPort.findAll()).hasSize(0) },
//            { assertThat(branchPort.findAll()).hasSize(0) },
//            { assertThat(userPort.findAll()).hasSize(0) },
//        )
//    }
//
//    @Test
//    fun `update project, add empty repository`() {
//        super.tearDown()
//        assertThat(projectPort.findAll()).hasSize(0)
//        assertThat(repositoryPort.findAll()).hasSize(0)
//
//        existingProject = run {
//            val p = Project(
//                name = "test project",
//                repo = null
//            )
//            val saved = projectPort.create(p)
//            assertThat(saved.repo).isNull()
//            assertAll(
//                "check database numbers",
//                { assertThat(projectPort.findAll()).hasSize(1) },
//                { assertThat(repositoryPort.findAll()).hasSize(0) },
//                { assertThat(commitPort.findAll()).hasSize(0) },
//                { assertThat(branchPort.findAll()).hasSize(0) },
//                { assertThat(userPort.findAll()).hasSize(0) },
//            )
//
//            return@run saved
//        }
//
//        existingProject.repo = repository
//        repository.project = existingProject
//
//        val saved = projectPort.update(existingProject)
//        assertThat(saved.repo).isNotNull()
//        assertAll(
//            "check database numbers",
//            { assertThat(projectPort.findAll()).hasSize(1) },
//            { assertThat(repositoryPort.findAll()).hasSize(1) },
//            { assertThat(commitPort.findAll()).hasSize(0) },
//            { assertThat(branchPort.findAll()).hasSize(0) },
//            { assertThat(userPort.findAll()).hasSize(0) },
//        )
//    }
//
//    @Test
//    fun `update project, remove repository, should not update anything`() {
//        existingProject.repo = null
//        assertDoesNotThrow {
//            projectPort.update(existingProject)
//        }
//        assertAll(
//            "check database numbers",
//            { assertThat(projectPort.findAll()).hasSize(1) },
//            { assertThat(repositoryPort.findAll()).hasSize(1) },
//            { assertThat(commitPort.findAll()).hasSize(0) },
//            { assertThat(branchPort.findAll()).hasSize(0) },
//            { assertThat(userPort.findAll()).hasSize(0) },
//        )
//    }
//
//    @Test
//    fun `update project, change repository, should fail`() {
//        existingProject.repo = Repository(
//            localPath = "new repository",
//            project = existingProject
//        )
//        assertThrows<IllegalArgumentException> {
//            projectPort.update(existingProject)
//        }
//        assertAll(
//            "check database numbers",
//            { assertThat(projectPort.findAll()).hasSize(1) },
//            { assertThat(repositoryPort.findAll()).hasSize(1) },
//            { assertThat(commitPort.findAll()).hasSize(0) },
//            { assertThat(branchPort.findAll()).hasSize(0) },
//            { assertThat(userPort.findAll()).hasSize(0) },
//        )
//    }
//
//    @Test
//    fun `update project, add commits to repository, expect in database`() {
//        run {
//            val user =
//                User(
//                    name = "test",
//                    email = "test@example.com",
//                    repository = existingProject.repo,
//                )
//            val branch =
//                Branch(
//                    name = "test branch",
//                    repository = existingProject.repo,
//                )
//            val cmt =
//                Commit(
//                    sha = "1234567890123456789012345678901234567890",
//                    message = "test commit",
//                    commitDateTime = LocalDateTime.of(2025, 7, 13, 1, 1),
//                )
//            branch.commits.add(cmt)
//            user.committedCommits.add(cmt)
//            existingProject.repo?.user?.add(user)
//            branch.commits.add(cmt)
//            existingProject.repo?.commits?.add(cmt)
//            existingProject.repo?.branches?.add(branch)
//            assertAll(
//                "check model",
//                { assertThat(existingProject.repo).isNotNull() },
//                { assertThat(existingProject.repo?.commits).hasSize(1) },
//                { assertThat(existingProject.repo?.user).hasSize(1) },
//                { assertThat(existingProject.repo?.branches).hasSize(1) },
//            )
//        }
//
//        assertDoesNotThrow {
//            projectPort.update(existingProject)
//        }
//        assertAll(
//            "check database numbers",
//            { assertThat(projectPort.findAll()).hasSize(1) },
//            { assertThat(repositoryPort.findAll()).hasSize(1) },
//            { assertThat(commitPort.findAll()).hasSize(1) },
//            { assertThat(branchPort.findAll()).hasSize(1) },
//            { assertThat(userPort.findAll()).hasSize(1) },
//        )
//    }
//}
