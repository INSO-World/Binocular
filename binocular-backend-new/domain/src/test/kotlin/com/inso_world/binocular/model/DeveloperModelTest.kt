package com.inso_world.binocular.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.uuid.ExperimentalUuidApi

/**
 * BDD tests for Developer domain model.
 * Developer is now global (no repository scope).
 */
@OptIn(ExperimentalUuidApi::class)
class DeveloperModelTest {

    @Nested
    inner class Construction {

        @Test
        fun `given valid name and email, when creating developer, then it should be created with iid`() {
            val name = "John Doe"
            val email = "john@example.com"

            val developer = Developer(name = name, email = email)

            assertAll(
                { assertThat(developer.name).isEqualTo(name) },
                { assertThat(developer.email).isEqualTo(email) },
                { assertThat(developer.iid).isNotNull() }
            )
        }

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.domain.data.DummyTestData#provideBlankStrings")
        fun `given blank name, when creating developer, then it should throw IllegalArgumentException`(name: String) {
            org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
                Developer(name = name, email = "test@example.com")
            }
        }

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.domain.data.DummyTestData#provideBlankStrings")
        fun `given blank email, when creating developer, then it should throw IllegalArgumentException`(email: String) {
            org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
                Developer(name = "Test", email = email)
            }
        }
    }

    @Nested
    inner class UniqueKey {

        @Test
        fun `given developer, when accessing uniqueKey, then it should contain gitSignature`() {
            val developer = Developer(name = "Test User", email = "test@example.com")

            val key = developer.uniqueKey

            assertThat(key.gitSignature).isEqualTo("Test User ")
        }
    }

    @Nested
    inner class GitSignature {

        @Test
        fun `given developer with name and email, when getting gitSignature, then it should return formatted signature`() {
            val developer = Developer(name = "John Doe", email = "john@example.com")

            val signature = developer.gitSignature

            assertThat(signature).isEqualTo("John Doe ")
        }

        @Test
        fun `given developer with whitespace in name, when getting gitSignature, then it should trim the name`() {
            val developer = Developer(name = "  John Doe  ", email = "john@example.com")

            val signature = developer.gitSignature

            assertThat(signature).isEqualTo("John Doe ")
        }
    }

    @Nested
    inner class Equality {

        @Test
        fun `given same developer instance, when comparing with equals, then it should be equal`() {
            val developer = Developer(name = "Test", email = "test@example.com")

            assertThat(developer).isEqualTo(developer)
        }

        @Test
        fun `given two different developers, when comparing, then they should not be equal`() {
            val developer1 = Developer(name = "Test1", email = "test1@example.com")
            val developer2 = Developer(name = "Test2", email = "test2@example.com")

            assertThat(developer1).isNotEqualTo(developer2)
        }

        @Test
        fun `given developer, when getting hashCode, then it should be based on iid`() {
            val developer = Developer(name = "Test", email = "test@example.com")

            assertThat(developer.hashCode()).isEqualTo(developer.iid.hashCode())
        }
    }
}
