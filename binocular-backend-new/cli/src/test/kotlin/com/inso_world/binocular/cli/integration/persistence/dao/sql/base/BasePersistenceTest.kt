package com.inso_world.binocular.cli.integration.persistence.dao.sql.base

import com.inso_world.binocular.cli.BinocularCommandLineApplication
import com.inso_world.binocular.cli.entity.Repository
import com.inso_world.binocular.cli.index.vcs.toVcsRepository
import com.inso_world.binocular.cli.integration.utils.generateCommits
import com.inso_world.binocular.cli.integration.utils.setupRepoConfig
import com.inso_world.binocular.cli.persistence.repository.sql.CommitRepository
import com.inso_world.binocular.cli.persistence.repository.sql.RepositoryRepository
import com.inso_world.binocular.cli.persistence.repository.sql.UserRepository
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@DataJpaTest
@ContextConfiguration(classes = [BinocularCommandLineApplication::class])
@Import(BinocularCommandLineApplication::class)
@ComponentScan(basePackages = ["com.inso_world.binocular.cli.persistence.dao.sql"])
internal class BasePersistenceTest : BaseFixturesIntegrationTest() {
  @Autowired
  lateinit var commitRepository: CommitRepository

  @Autowired
  lateinit var userRepository: UserRepository

  @Autowired
  internal lateinit var repositoryRepository: RepositoryRepository

  protected lateinit var simpleRepo: Repository
  protected lateinit var octoRepo: Repository

  @BeforeEach
  fun setupBase() {
    val simpleRepoConfig =
      setupRepoConfig(
        "${FIXTURES_PATH}/${SIMPLE_REPO}",
        "HEAD",
      )
    this.simpleRepo = simpleRepoConfig.repo.toVcsRepository().toEntity()
    generateCommits(simpleRepoConfig, simpleRepo)
    this.simpleRepo = this.repositoryRepository.save(this.simpleRepo)

    val octoRepoConfig =
      setupRepoConfig(
        "${FIXTURES_PATH}/${OCTO_REPO}",
        "HEAD",
      )
    this.octoRepo = octoRepoConfig.repo.toVcsRepository().toEntity()
    generateCommits(octoRepoConfig, octoRepo)
    this.octoRepo = this.repositoryRepository.save(this.octoRepo)
  }

  @AfterEach
  fun cleanup() {
    repositoryRepository.deleteAll()
    commitRepository.deleteAll()
    userRepository.deleteAll()
  }
}
