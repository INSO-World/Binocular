package com.inso_world.binocular.web.graphql.integration.realdata.base

import com.inso_world.binocular.web.BinocularWebApplication
import com.inso_world.binocular.web.graphql.integration.realdata.base.legacy.LegacyHttpGraphQlClient
import com.inso_world.binocular.web.graphql.integration.realdata.base.spring.SpringTesterGraphQlClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.graphql.test.tester.GraphQlTester

@AutoConfigureGraphQlTester
@AutoConfigureMockMvc
@SpringBootTest(
  classes = [BinocularWebApplication::class],
  webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
abstract class BaseGraphQlCompatibilityIT {

  companion object {
    private const val DEFAULT_TARGET = "spring"
    private const val TARGET_LEGACY = "legacy"
    private const val TARGET_SPRING = "spring"
    private const val DEFAULT_LEGACY_URL = "http://[::1]:8080/graphQl"
  }

  @Autowired
  protected lateinit var tester: GraphQlTester

  protected val client: CompatibleGraphQlClient by lazy {
    val target = graphqlTarget()
    log("Running GraphQL tests in mode: $target")

    when (target) {
      TARGET_LEGACY -> createLegacyClient()
      TARGET_SPRING -> SpringTesterGraphQlClient(tester)
      else -> error("Unknown graphql.target: $target (use '$TARGET_LEGACY' or '$TARGET_SPRING')")
    }
  }

  private fun createLegacyClient(): CompatibleGraphQlClient {
    val url = legacyUrl()
    log("Legacy endpoint: $url (override with -Dgraphql.legacy.url)")
    return LegacyHttpGraphQlClient(url)
  }

  private fun graphqlTarget(): String =
    System.getProperty("graphql.target", DEFAULT_TARGET).lowercase()

  private fun legacyUrl(): String =
    System.getProperty("graphql.legacy.url", DEFAULT_LEGACY_URL)

  private fun log(message: String) =
    println("[GRAPHQL-IT] $message")

}
