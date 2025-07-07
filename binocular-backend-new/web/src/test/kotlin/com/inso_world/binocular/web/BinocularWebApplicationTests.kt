package com.inso_world.binocular.web

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    classes = [BinocularWebApplication::class],
)
@ActiveProfiles("nosql", "arangodb", "test")
internal class BinocularWebApplicationTests {
    @Test
    fun contextLoads() {
    }
}
