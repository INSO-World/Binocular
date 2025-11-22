package com.inso_world.binocular.ffi.unit.extensions

import com.inso_world.binocular.core.unit.base.BaseUnitTest
import com.inso_world.binocular.ffi.extensions.toDomain
import com.inso_world.binocular.ffi.internal.GixSignature
import com.inso_world.binocular.ffi.internal.GixTime
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

/**
 * Unit tests for [toDomain] extension function.
 *
 * Provides comprehensive C4 coverage testing:
 * - Identity preservation (finding existing users by business key)
 * - Email enrichment logic (idempotent, non-overwriting)
 * - Name normalization (trim behavior)
 * - User registration in repository
 * - All decision paths and edge cases
 */
class GixSignatureTest : BaseUnitTest() {

    private lateinit var project: Project
    private lateinit var repository: Repository

    @BeforeEach
    fun setUp() {
        project = Project(name = "test-project")
        repository = Repository(
            localPath = "/path/to/repo",
            project = project
        )
    }

    // ========== Creating New Users ==========

    @Test
    fun `toDomain creates new user when no existing user matches`() {
        val sig = GixSignature(
            name = "John Doe",
            email = "john@example.com",
            time = GixTime(seconds = 0L, offset = 0)
        )

        val result = sig.toDomain(repository)

        assertThat(result).isNotNull
        assertThat(result.name).isEqualTo("John Doe")
        assertThat(result.email).isEqualTo("john@example.com")
        assertThat(result.repository).isSameAs(repository)
    }

    @Test
    fun `toDomain registers new user in repository user collection`() {
        val sig = GixSignature(
            name = "Jane Smith",
            email = "jane@example.com",
            time = GixTime(seconds = 0L, offset = 0)
        )

        val result = sig.toDomain(repository)

        assertThat(repository.user).contains(result)
        assertThat(repository.user).hasSize(1)
    }

    @Test
    fun `toDomain creates user with trimmed name`() {
        val sig = GixSignature(
            name = "  Alice Wonderland  ",
            email = "alice@example.com",
            time = GixTime(seconds = 0L, offset = 0)
        )

        val result = sig.toDomain(repository)

        assertThat(result.name).isEqualTo("Alice Wonderland")
    }

    // ========== Identity Preservation (Returning Existing Users) ==========

    @Test
    fun `toDomain returns existing user when name matches exactly`() {
        val existingUser = User(name = "Bob Builder", repository = repository)

        val sig = GixSignature(
            name = "Bob Builder",
            email = "bob@example.com",
            time = GixTime(seconds = 0L, offset = 0)
        )

        val result = sig.toDomain(repository)

        assertThat(result).isSameAs(existingUser)
    }

    @Test
    fun `toDomain returns existing user when name matches after trim`() {
        val existingUser = User(name = "Charlie Brown", repository = repository)

        val sig = GixSignature(
            name = "  Charlie Brown  ",
            email = "charlie@example.com",
            time = GixTime(seconds = 0L, offset = 0)
        )

        val result = sig.toDomain(repository)

        assertThat(result).isSameAs(existingUser)
    }

    @Test
    fun `toDomain does not create duplicate users for same trimmed name`() {
        val sig1 = GixSignature(
            name = "Dave Grohl",
            email = "dave1@example.com",
            time = GixTime(seconds = 0L, offset = 0)
        )
        val sig2 = GixSignature(
            name = "  Dave Grohl  ",
            email = "dave2@example.com",
            time = GixTime(seconds = 0L, offset = 0)
        )

        val result1 = sig1.toDomain(repository)
        val result2 = sig2.toDomain(repository)

        assertThat(result1).isSameAs(result2)
        assertThat(repository.user).hasSize(1)
    }

    // ========== Email Enrichment Logic ==========

    @Nested
    inner class EmailEnrichment {

        @Test
        fun `toDomain sets email on existing user when user has no email`() {
            val existingUser = User(name = "Emma Watson", repository = repository)
            assertThat(existingUser.email).isNull()

            val sig = GixSignature(
                name = "Emma Watson",
                email = "emma@example.com",
                time = GixTime(seconds = 0L, offset = 0)
            )

            val result = sig.toDomain(repository)

            assertThat(result).isSameAs(existingUser)
            assertThat(result.email).isEqualTo("emma@example.com")
        }

        @Test
        fun `toDomain does not overwrite existing email`() {
            val existingUser = User(name = "Frank Ocean", repository = repository)
            existingUser.email = "frank.original@example.com"

            val sig = GixSignature(
                name = "Frank Ocean",
                email = "frank.new@example.com",
                time = GixTime(seconds = 0L, offset = 0)
            )

            val result = sig.toDomain(repository)

            assertThat(result).isSameAs(existingUser)
            assertThat(result.email).isEqualTo("frank.original@example.com")
        }

        @Test
        fun `toDomain enrichment is idempotent - calling multiple times preserves first email`() {
            val existingUser = User(name = "Grace Hopper", repository = repository)

            val sig1 = GixSignature(
                name = "Grace Hopper",
                email = "grace1@example.com",
                time = GixTime(seconds = 0L, offset = 0)
            )
            val sig2 = GixSignature(
                name = "Grace Hopper",
                email = "grace2@example.com",
                time = GixTime(seconds = 0L, offset = 0)
            )

            val result1 = sig1.toDomain(repository)
            assertThat(result1.email).isEqualTo("grace1@example.com")

            val result2 = sig2.toDomain(repository)
            assertThat(result2.email).isEqualTo("grace1@example.com") // Still first email
        }

        @Test
        fun `toDomain sets email with trimmed value from FFI`() {
            val sig = GixSignature(
                name = "Henry Ford",
                email = "  henry@example.com  ",
                time = GixTime(seconds = 0L, offset = 0)
            )

            val result = sig.toDomain(repository)

            assertThat(result.email).isEqualTo("henry@example.com")
        }

        @Test
        fun `toDomain handles blank email on new user - sets null because blank is rejected`() {
            val sig = GixSignature(
                name = "Iris Apfel",
                email = "   ",
                time = GixTime(seconds = 0L, offset = 0)
            )

            val result = sig.toDomain(repository)

            // Email setter rejects blank, so it should remain null
            assertThat(result.email).isNull()
        }

        @Test
        fun `toDomain handles empty email on new user`() {
            val sig = GixSignature(
                name = "Jack Dorsey",
                email = "",
                time = GixTime(seconds = 0L, offset = 0)
            )

            val result = sig.toDomain(repository)

            // Email setter rejects empty, so it should remain null
            assertThat(result.email).isNull()
        }

        @Test
        fun `toDomain does not set blank email on existing user without email`() {
            val existingUser = User(name = "Kate Middleton", repository = repository)
            assertThat(existingUser.email).isNull()

            val sig = GixSignature(
                name = "Kate Middleton",
                email = "   ",
                time = GixTime(seconds = 0L, offset = 0)
            )

            val result = sig.toDomain(repository)

            assertThat(result).isSameAs(existingUser)
            assertThat(result.email).isNull() // Should remain null, not set to blank
        }
    }

    // ========== Name Normalization & Business Key Matching ==========

    @Nested
    inner class NameNormalization {

        @ParameterizedTest
        @ValueSource(strings = [
            "  Leading Space",
            "Trailing Space  ",
            "  Both Sides  ",
            "\tTab Character\t",
            "\nNewline\n",
            "  Multiple   Spaces  "
        ])
        fun `toDomain trims various whitespace from name`(nameWithWhitespace: String) {
            val trimmedName = nameWithWhitespace.trim()
            val sig = GixSignature(
                name = nameWithWhitespace,
                email = "test@example.com",
                time = GixTime(seconds = 0L, offset = 0)
            )

            val result = sig.toDomain(repository)

            assertThat(result.name).isEqualTo(trimmedName)
        }

        @Test
        fun `toDomain finds existing user by trimmed name even with different whitespace`() {
            val existingUser = User(name = "Linus Torvalds", repository = repository)

            val sig = GixSignature(
                name = "\t  Linus Torvalds  \n",
                email = "linus@example.com",
                time = GixTime(seconds = 0L, offset = 0)
            )

            val result = sig.toDomain(repository)

            assertThat(result).isSameAs(existingUser)
        }

        @Test
        fun `toDomain business key matching is case-sensitive`() {
            val existingUser = User(name = "Mary Smith", repository = repository)

            val sig = GixSignature(
                name = "mary smith", // Different case
                email = "mary@example.com",
                time = GixTime(seconds = 0L, offset = 0)
            )

            val result = sig.toDomain(repository)

            // Should create new user because name is case-sensitive
            assertThat(result).isNotSameAs(existingUser)
            assertThat(result.name).isEqualTo("mary smith")
            assertThat(repository.user).hasSize(2)
        }
    }

    // ========== Multiple Users in Repository ==========

    @Test
    fun `toDomain correctly identifies user among multiple users`() {
        val user1 = User(name = "User One", repository = repository)
        val user2 = User(name = "User Two", repository = repository)
        val user3 = User(name = "User Three", repository = repository)

        val sig = GixSignature(
            name = "User Two",
            email = "two@example.com",
            time = GixTime(seconds = 0L, offset = 0)
        )

        val result = sig.toDomain(repository)

        assertThat(result).isSameAs(user2)
        assertThat(repository.user).hasSize(3) // No new user added
    }

    @Test
    fun `toDomain creates fourth user when no match among existing three`() {
        User(name = "User One", repository = repository)
        User(name = "User Two", repository = repository)
        User(name = "User Three", repository = repository)

        val sig = GixSignature(
            name = "User Four",
            email = "four@example.com",
            time = GixTime(seconds = 0L, offset = 0)
        )

        val result = sig.toDomain(repository)

        assertThat(result.name).isEqualTo("User Four")
        assertThat(repository.user).hasSize(4)
    }

    // ========== Edge Cases ==========

    @Test
    fun `toDomain with minimal valid name`() {
        val sig = GixSignature(
            name = "A",
            email = "a@example.com",
            time = GixTime(seconds = 0L, offset = 0)
        )

        val result = sig.toDomain(repository)

        assertThat(result.name).isEqualTo("A")
    }

    @Test
    fun `toDomain with long name`() {
        val longName = "A" + "Very ".repeat(100) + "Long Name"
        val sig = GixSignature(
            name = longName,
            email = "long@example.com",
            time = GixTime(seconds = 0L, offset = 0)
        )

        val result = sig.toDomain(repository)

        assertThat(result.name).isEqualTo(longName)
    }

    @Test
    fun `toDomain with special characters in name`() {
        val sig = GixSignature(
            name = "Björk Guðmundsdóttir",
            email = "bjork@example.com",
            time = GixTime(seconds = 0L, offset = 0)
        )

        val result = sig.toDomain(repository)

        assertThat(result.name).isEqualTo("Björk Guðmundsdóttir")
    }

    @Test
    fun `toDomain with special characters in email`() {
        val sig = GixSignature(
            name = "Test User",
            email = "test+alias@sub.example.com",
            time = GixTime(seconds = 0L, offset = 0)
        )

        val result = sig.toDomain(repository)

        assertThat(result.email).isEqualTo("test+alias@sub.example.com")
    }

    // ========== Repository Scoping ==========

    @Test
    fun `toDomain users are scoped to specific repository`() {
        val project2 = Project(name = "another-project")
        val repository2 = Repository(localPath = "/path/to/repo2", project = project2)

        val sig = GixSignature(
            name = "Shared Name",
            email = "shared@example.com",
            time = GixTime(seconds = 0L, offset = 0)
        )

        val userInRepo1 = sig.toDomain(repository)
        val userInRepo2 = sig.toDomain(repository2)

        // Different repository instances, so different users
        assertThat(userInRepo1).isNotSameAs(userInRepo2)
        assertThat(userInRepo1.name).isEqualTo("Shared Name")
        assertThat(userInRepo2.name).isEqualTo("Shared Name")
        assertThat(repository.user).containsOnly(userInRepo1)
        assertThat(repository2.user).containsOnly(userInRepo2)
    }

    // ========== Combination & Decision Coverage ==========

    @ParameterizedTest
    @CsvSource(
        // name, email, expectedName, shouldHaveEmail
        "'John Doe', 'john@example.com', 'John Doe', true",
        "'  Jane  ', 'jane@example.com', 'Jane', true",
        "'Bob', '', 'Bob', false",
        "'Alice', '   ', 'Alice', false",
        "'  Charlie  ', '  charlie@test.com  ', 'Charlie', true"
    )
    fun `toDomain handles various name and email combinations`(
        name: String,
        email: String,
        expectedName: String,
        shouldHaveEmail: Boolean
    ) {
        val sig = GixSignature(
            name = name,
            email = email,
            time = GixTime(seconds = 0L, offset = 0)
        )

        val result = sig.toDomain(repository)

        assertThat(result.name).isEqualTo(expectedName)
        if (shouldHaveEmail) {
            assertThat(result.email).isNotNull().isNotBlank()
        } else {
            assertThat(result.email).isNull()
        }
    }

    @Test
    fun `toDomain decision path - new user with valid email`() {
        val sig = GixSignature(
            name = "New User",
            email = "new@example.com",
            time = GixTime(seconds = 0L, offset = 0)
        )

        val result = sig.toDomain(repository)

        // Path: no existing user → create new → set email
        assertThat(repository.user).hasSize(1)
        assertThat(result.name).isEqualTo("New User")
        assertThat(result.email).isEqualTo("new@example.com")
    }

    @Test
    fun `toDomain decision path - existing user with email, enrich not triggered`() {
        val existingUser = User(name = "Existing", repository = repository)
        existingUser.email = "existing@example.com"

        val sig = GixSignature(
            name = "Existing",
            email = "different@example.com",
            time = GixTime(seconds = 0L, offset = 0)
        )

        val result = sig.toDomain(repository)

        // Path: existing user found → has email → no enrichment
        assertThat(result).isSameAs(existingUser)
        assertThat(result.email).isEqualTo("existing@example.com")
        assertThat(repository.user).hasSize(1)
    }

    @Test
    fun `toDomain decision path - existing user without email, enrich triggered`() {
        val existingUser = User(name = "Existing No Email", repository = repository)
        assertThat(existingUser.email).isNull()

        val sig = GixSignature(
            name = "Existing No Email",
            email = "enrich@example.com",
            time = GixTime(seconds = 0L, offset = 0)
        )

        val result = sig.toDomain(repository)

        // Path: existing user found → no email → enrich with FFI email
        assertThat(result).isSameAs(existingUser)
        assertThat(result.email).isEqualTo("enrich@example.com")
        assertThat(repository.user).hasSize(1)
    }

    @Test
    fun `toDomain decision path - new user with blank email`() {
        val sig = GixSignature(
            name = "New Blank Email",
            email = "   ",
            time = GixTime(seconds = 0L, offset = 0)
        )

        val result = sig.toDomain(repository)

        // Path: no existing user → create new → blank email rejected → email stays null
        assertThat(repository.user).hasSize(1)
        assertThat(result.name).isEqualTo("New Blank Email")
        assertThat(result.email).isNull()
    }
}
