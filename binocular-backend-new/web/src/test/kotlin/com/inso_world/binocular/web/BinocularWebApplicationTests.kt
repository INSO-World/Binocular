package com.inso_world.binocular.web

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Profile

@SpringBootTest(
    classes = [BinocularWebApplication::class],
)
@Profile("test")
internal class BinocularWebApplicationTests {
    @Test
    fun contextLoads() {
    }
}
