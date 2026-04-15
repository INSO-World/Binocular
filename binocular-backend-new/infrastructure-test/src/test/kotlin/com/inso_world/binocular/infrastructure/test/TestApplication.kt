package com.inso_world.binocular.infrastructure.test

import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication(
    scanBasePackages = [
        "com.inso_world.binocular.infrastructure.test",
        "com.inso_world.binocular.core",
    ],
)
internal class TestApplication {
    @Test
    fun contextLoads() {
    }
}
