package com.inso_world.binocular.infrastructure.sql.integration.persistence.mapper.base

import com.inso_world.binocular.core.integration.base.BaseIntegrationTest
import com.inso_world.binocular.infrastructure.sql.mapper.context.MappingScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest

@SpringBootApplication(scanBasePackages = ["com.inso_world.binocular.infrastructure.sql.mapper"])
private class MapperTestApplication

@SpringBootTest(classes = [MapperTestApplication::class])
internal class BaseMapperTest : BaseIntegrationTest() {
    @Autowired
    private lateinit var mappingScope: MappingScope

    @BeforeEach
    fun openSession() {
        mappingScope.startSession()
    }

    @AfterEach
    fun closeSession() {
        mappingScope.endSession()
    }
}

//internal object MapperTestData {
//    val projectEntity =
//        ProjectEntity(
//            id = 1L,
//            name = "TestProject",
//            description = "A test project",
//        )
//
//    val repositoryEntity =
//        RepositoryEntity(
//            id = 1L,
//            name = "TestRepository",
//            project = projectEntity,
//        )
//
//    val commitEntityA =
//        CommitEntity(
//            id = 1,
//            sha = "A".repeat(40),
//            authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
//            commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
//            message = "Valid commit 1",
//            repository = this.repositoryEntity,
//            branches = mutableSetOf(),
//        )
//
//    val commitEntityB =
//        CommitEntity(
//            id = 2,
//            sha = "B".repeat(40),
//            authorDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
//            commitDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
//            message = "Valid commit 2",
//            repository = this.repositoryEntity,
//            branches = mutableSetOf(),
//        )
//
//    val branchEntity =
//        BranchEntity(
//            name = "testBranch",
//            repository = this.repositoryEntity,
//            commits =
//                mutableSetOf(
////                    commitEntityA,
////                    commitEntityB,
//                ),
//        )
//
//    /*
//     * Domain objects
//     */
//
//    fun repositoryModel() =
//        Repository(
//            id = this.repositoryEntity.id.toString(),
//            name = this.repositoryEntity.name,
//            project = Project(
//                id = projectEntity.id?.toString(),
//                name = projectEntity.name,
//                description = projectEntity.description,
//            )
//        )
//
//    val branchDomain =
//        Branch(
//            id = branchEntity.id?.toString(),
//            name = branchEntity.name,
//            repository = repositoryModel(),
//        )
//
//    val commitDomainA =
//        Commit(
//            id = commitEntityA.id?.toString(),
//            sha = commitEntityA.sha,
//            authorDateTime = commitEntityA.authorDateTime,
//            commitDateTime = commitEntityA.commitDateTime,
//            message = commitEntityA.message,
//            repositoryId = commitEntityA.repository?.id.toString(),
//        )
//    val commitDomainB =
//        Commit(
//            id = commitEntityB.id?.toString(),
//            sha = commitEntityB.sha,
//            authorDateTime = commitEntityB.authorDateTime,
//            commitDateTime = commitEntityB.commitDateTime,
//            message = commitEntityB.message,
//            repositoryId = commitEntityB.repository?.id.toString(),
//        )
//
//    val commits =
//        listOf(
//            commitDomainA,
//            commitDomainB,
//        )
//}
