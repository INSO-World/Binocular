package com.inso_world.binocular.web.graphql.base

import com.inso_world.binocular.web.BaseDbTest
import com.inso_world.binocular.web.BinocularWebApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.graphql.test.tester.GraphQlTester

@SpringBootTest(
    classes = [BinocularWebApplication::class],
)
@AutoConfigureGraphQlTester
internal class GraphQlControllerTest : BaseDbTest() {
    @Autowired
    protected lateinit var graphQlTester: GraphQlTester
}
