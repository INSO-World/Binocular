package com.inso_world.binocular.infrastructure.sql

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Profile
import org.testcontainers.containers.PostgreSQLContainer

@SpringBootApplication(
    scanBasePackages = ["com.inso_world.binocular.infrastructure.sql", "com.inso_world.binocular.core"],
)
internal class TestApplication {
    @Test
    fun contextLoads() {}

    @Test
    @Profile("postgres")
    fun testEmbeddedPg() {
        val pg = PostgreSQLContainer("postgres:17.5")
        pg.start()

        val rs = pg.createConnection("").createStatement().executeQuery(
            pg.testQueryString
        )
        assertTrue(rs.next())
        assertEquals(1, rs.getInt(1))
        assertFalse(rs.next())
        pg.stop()
    }
}
