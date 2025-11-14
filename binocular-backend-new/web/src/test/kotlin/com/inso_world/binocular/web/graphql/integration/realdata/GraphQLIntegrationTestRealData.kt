package com.inso_world.binocular.web.graphql.integration.realdata

import com.fasterxml.jackson.databind.JsonNode
import com.inso_world.binocular.web.BinocularWebApplication
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.graphql.test.tester.GraphQlTester
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

/**
 * GraphQL test to test on uses a large ArangoDB dataset based on a dump.
 *
 * 1) Start an ArangoDB container and restore backup
 *
 *    # port, e.g. 8529
 *    docker run -d --rm --name bino-arango -p 8529:8529 -e ARANGO_ROOT_PASSWORD=test arangodb:3.12.4
 *    docker cp /Users/markus/inso/binocular/binocular-backend-new/arangodb-dump/binocular-repo bino-arango:/tmp/arangodb-dump/
 *    docker exec bino-arango arangorestore \
 *      --server.endpoint tcp://127.0.0.1:8529 \
 *      --server.username root --server.password test \
 *      --create-database true \
 *      --input-directory /tmp/arangodb-dump
 *
 * 2) Run this test file here:
 *
 *    mvn -f binocular-backend-new/web/pom.xml -Dspring.profiles.active=test \
 *      -Dtest.external.hosts=localhost:8529 \
 *      -Dtest.big.dbName=binocular \
 *      -Dtest.external.username=root \
 *      -Dtest.external.password=test \
 *      test
 *
 * backup can be created like this
 *    # 1) Ensure a target folder exists IN the container, then dump
 *    docker exec 5fde36c12fb sh -c '
 *      mkdir -p /tmp/arangodb-dump/'binocular-repo' && \
 *      arangodump \
 *        --server.endpoint tcp://127.0.0.1:8529 \
 *        --server.authentication false \
 *        --server.database 'binocular-repo' \
 *        --output-directory /tmp/arangodb-dump/'binocular-repo' \
 *        --overwrite true
 *    '
 *
 *    # 2) Copy the dump folder
 *    mkdir -p /Users/markus/inso/binocular/binocular-backend-new/arangodb-dump
 *    docker cp 5fde36c12fb:/tmp/arangodb-dump/binocular-repo /Users/markus/inso/binocular/binocular-backend-new/arangodb-dump/
 *
 */
@AutoConfigureGraphQlTester
@SpringBootTest(classes = [BinocularWebApplication::class])
internal class GraphQLIntegrationTestRealData {

    @Autowired
    lateinit var graphQlTester: GraphQlTester

    // TODO: verify this works correctly
    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun registerArangoProperties(registry: DynamicPropertyRegistry) {
            val hosts = System.getProperty("test.external.hosts", "localhost:8529")
            val username = System.getProperty("test.external.username", "root")
            val password = System.getProperty("test.external.password", "test")
            val dbName = System.getProperty("test.big.dbName", "binocular-repo")
            registry.add("spring.data.arangodb.hosts") { hosts }
            registry.add("spring.data.arangodb.username") { username }
            registry.add("spring.data.arangodb.password") { password }
            registry.add("spring.data.arangodb.database") { dbName }
        }
    }

    @Test
    fun `should execute a simple branches query`() {
        val result: JsonNode = graphQlTester
            .document(
                """
                query {
                    branches(page: 1, perPage: 1) {
                        count
                        page
                        perPage
                        data { id }
                    }
                }
                """
            )
            .execute()
            .path("branches")
            .entity(JsonNode::class.java)
            .get()

        assertTrue(result.has("count"), "branches.count should be present")
        assertTrue(result.get("count").asInt() >= 0, "branches.count should be >= 0")
    }
}
