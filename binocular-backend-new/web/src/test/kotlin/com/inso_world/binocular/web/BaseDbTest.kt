package com.inso_world.binocular.web

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.Stats
import com.inso_world.binocular.web.persistence.repository.arangodb.CommitRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester
import org.springframework.graphql.test.tester.GraphQlTester
import java.util.Date

/**
 * Base test class that sets up test data in the database.
 * This class can be extended by test classes that need database test data.
 */
@SpringBootTest
@AutoConfigureGraphQlTester
abstract class BaseDbTest {

    @Autowired
    protected lateinit var graphQlTester: GraphQlTester

    @Autowired
    protected lateinit var commitRepository: CommitRepository

    /**
     * Test data that will be created in the database.
     */
    protected val testCommits = listOf(
        Commit(
            id = "1",
            sha = "abc123",
            date = Date(),
            message = "First commit",
            webUrl = "https://example.com/commit/abc123",
            branch = "main",
            stats = Stats(additions = 10, deletions = 5)
        ),
        Commit(
            id = "2",
            sha = "def456",
            date = Date(),
            message = "Second commit",
            webUrl = "https://example.com/commit/def456",
            branch = "main",
            stats = Stats(additions = 7, deletions = 3)
        )
    )

    /**
     * Set up test data in the database before each test.
     */
    @BeforeEach
    fun setupTestData() {
        // Clear existing data
        commitRepository.deleteAll()

        // Create test data
        testCommits.forEach { commit ->
            commitRepository.save(commit)
        }
    }
}
