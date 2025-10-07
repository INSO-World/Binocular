package com.inso_world.binocular.cli

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Profile
import org.testcontainers.containers.PostgreSQLContainer

// @ActiveProfiles("test")
@SpringBootTest(
    classes = [BinocularCommandLineApplication::class],
)
internal class BinocularCommandLineApplicationTest {
    @Test
    fun contextLoads() {
    }

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
