package com.inso_world.binocular.infrastructure.test

import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication(
    scanBasePackages = [
        "com.inso_world.binocular.infrastructure.arangodb",
        "com.inso_world.binocular.infrastructure.sql",
        "com.inso_world.binocular.core"
    ]
)
class TestApplication {

    @Test
    fun contextLoads() {
        // maybe remove this idk
    }

}
