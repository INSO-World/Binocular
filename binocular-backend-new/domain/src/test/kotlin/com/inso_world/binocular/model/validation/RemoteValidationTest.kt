package com.inso_world.binocular.model.validation

import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.utils.ReflectionUtils.Companion.setField
import com.inso_world.binocular.model.validation.base.ValidationTest
import com.inso_world.binocular.model.vcs.Remote
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

/**
 * Comprehensive validation tests for [Remote] domain model ensuring SEON compliance.
 *
 * # SEON Compliance Requirements
 * - **Entity Identity**: Remote must have stable technical identity (iid) and business key (repository.iid + name)
 * - **Domain Validation**: All properties must satisfy domain constraints (non-blank, format patterns)
 * - **Relationship Integrity**: Remote must maintain valid relationship with owning Repository
 * - **Immutability Constraints**: Core identifying properties must be immutable
 *
 * # Validation Coverage
 * - Property-level Jakarta validation annotations (@NotBlank, @Pattern, @GitUrl)
 * - Business rule validation (uniqueness within repository)
 * - Relationship consistency (repository reference integrity)
 * - Edge cases (whitespace, special characters, invalid formats)
 */
internal class RemoteValidationTest : ValidationTest() {
    private lateinit var repository: Repository

    @BeforeEach
    fun setup() {
        val project = Project(name = "test-project")
        repository = Repository(localPath = "/path/to/repo", project = project)
    }

    @Nested
    inner class NameValidation {
        /**
         * Tests validation of blank remote names.
         *
         * # SEON Requirement
         * - Remote name is a mandatory identifying property in Git domain model
         * - Must be non-blank per @NotBlank constraint
         *
         * # Validation
         * - Blank strings (empty, whitespace-only) must fail validation
         * - Violation must target the 'name' property
         */
        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.model.validation.ValidationTestData#provideBlankStrings")
        fun `should fail when name is blank`(name: String) {
            // Given
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )
            // Change field via reflection, otherwise constructor check fails
            setField(
                remote.javaClass.getDeclaredField("name").apply { isAccessible = true },
                remote,
                name
            )
            assertThat(remote.name).isEqualTo(name)

            // When
            val violations = validator.validate(remote)

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1)
            val nameViolations = violations.filter { it.propertyPath.toString() == "name" }
            assertThat(nameViolations).isNotEmpty
        }

        /**
         * Tests validation of invalid remote name patterns.
         *
         * # SEON Requirement
         * - Remote names must follow Git naming conventions
         * - Only alphanumeric, dots, underscores, slashes, hyphens allowed
         *
         * # Validation
         * - Names with invalid characters must fail @Pattern constraint
         */
        @ParameterizedTest
        @ValueSource(
            strings = [
                "origin@upstream",     // @ not allowed
                "remote name",         // space not allowed
                "remote:name",         // : not allowed
                "remote*name",         // * not allowed
                "remote&name",         // & not allowed
                "remote#name",         // # not allowed
                "remote\$name",        // $ not allowed
                "remote%name",         // % not allowed
                "remote^name",         // ^ not allowed
                "remote(name)",        // parentheses not allowed
                "remote[name]",        // brackets not allowed
                "remote{name}",        // braces not allowed
                "remote|name",         // pipe not allowed
                "remote\\name",        // backslash not allowed
                "remote;name",         // semicolon not allowed
                "remote'name",         // single quote not allowed
                "remote\"name",        // double quote not allowed
                "remote<name>",        // angle brackets not allowed
                "remote,name",         // comma not allowed
                "remote?name",         // question mark not allowed
                "remote!name",         // exclamation not allowed
                "remote~name",         // tilde not allowed
                "remote`name",         // backtick not allowed
            ]
        )
        fun `should fail when name contains invalid characters`(invalidName: String) {
            // Given
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )
            setField(
                remote.javaClass.getDeclaredField("name").apply { isAccessible = true },
                remote,
                invalidName
            )

            // When
            val violations = validator.validate(remote)

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1)
            val nameViolations = violations.filter {
                it.propertyPath.toString() == "name" &&
                        it.message.contains("alphanumeric")
            }
            assertThat(nameViolations).isNotEmpty
        }

        /**
         * Tests validation of valid remote name patterns.
         *
         * # SEON Requirement
         * - Common Git remote names must pass validation
         * - Supports standard naming conventions
         */
        @ParameterizedTest
        @ValueSource(
            strings = [
                "origin",              // Most common
                "upstream",            // Fork workflow
                "fork",                // Another fork name
                "production",          // Deployment remote
                "staging",             // Another deployment remote
                "backup",              // Backup remote
                "origin-https",        // With hyphen
                "origin_ssh",          // With underscore
                "remote.name",         // With dot
                "remote/name",         // With slash
                "remote-name_123",     // Mixed alphanumeric
                "Remote123",           // Mixed case
                "ORIGIN",              // Uppercase
                "123remote",           // Starting with number
                "r",                   // Single character
                "remote-name-with-many-hyphens", // Long with hyphens
                "remote_name_with_many_underscores", // Long with underscores
                "remote.name.with.dots", // With multiple dots
            ]
        )
        fun `should pass when name contains only valid characters`(validName: String) {
            // Given
            val remote = Remote(
                name = validName,
                url = "https://github.com/user/repo.git",
                repository = repository
            )

            // When
            val violations = validator.validate(remote)

            // Then
            val nameViolations = violations.filter { it.propertyPath.toString() == "name" }
            assertThat(nameViolations).isEmpty()
        }
    }

    @Nested
    inner class UrlValidation {
        /**
         * Tests validation of blank remote URLs.
         *
         * # SEON Requirement
         * - Remote URL is mandatory for identifying the external repository location
         * - Must be non-blank per @NotBlank constraint
         *
         * # Validation
         * - Blank URLs must fail validation
         */
        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.model.validation.ValidationTestData#provideBlankStrings")
        fun `should fail when url is blank`(url: String) {
            // Given
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )
            setField(
                remote.javaClass.getDeclaredField("url").apply { isAccessible = true },
                remote,
                url
            )
            assertThat(remote.url).isEqualTo(url)

            // When
            val violations = validator.validate(remote)

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1)
            val urlViolations = violations.filter { it.propertyPath.toString() == "url" }
            assertThat(urlViolations).isNotEmpty
        }

        /**
         * Tests validation of invalid URL formats.
         *
         * # SEON Requirement
         * - Remote URL must be a valid Git URL per @GitUrl constraint
         * - Supports HTTP(S), SSH, Git protocols, and local paths
         *
         * # Validation
         * - Invalid URL formats must fail validation
         */
        @ParameterizedTest
        @ValueSource(
            strings = [
                "not-a-url",           // Plain text
                "github.com/user/repo", // Missing protocol
                "htp://invalid.com",   // Invalid protocol
                "://no-protocol.com",  // Missing protocol name
                "http://",             // Incomplete URL
                "http:///path",        // Missing host
                "http:// space.com",   // Space in URL
            ]
        )
        fun `should fail when url format is invalid`(invalidUrl: String) {
            // Given
            val remote = Remote(
                name = "origin",
                url = "https://example.com/user/repo.git",
                repository = repository
            )
            // required, otherwise constructor will reject
            setField(
                remote.javaClass.getDeclaredField("url").apply { isAccessible = true },
                remote,
                invalidUrl
            )

            // When
            val violations = validator.validate(remote)

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1)
            val urlViolations = violations.filter { it.propertyPath.toString() == "url" }
            assertThat(urlViolations).isNotEmpty()
        }

        /**
         * Tests validation of valid URL formats for Git remotes.
         *
         * # SEON Requirement
         * - Remote URLs must support all standard Git protocols
         * - HTTPS, HTTP, Git, SSH, file protocols, and local paths are valid
         *
         * # Supported formats
         * - HTTP/HTTPS URLs (e.g., https://github.com/user/repo.git)
         * - SSH URLs (e.g., ssh://git@github.com/user/repo.git)
         * - Git protocol (e.g., git://github.com/user/repo.git)
         * - SCP-like SSH (e.g., git@github.com:user/repo.git)
         * - File URLs (e.g., file:///path/to/repo.git)
         * - Absolute paths (e.g., /path/to/repo)
         * - Relative paths (e.g., ../repo)
         */
        @ParameterizedTest
        @ValueSource(
            strings = [
                // HTTPS URLs
                "https://example.com/user/repo.git",
                "https://github.com/user/repo.git",
                "https://gitlab.com/group/project.git",
                "https://bitbucket.org/user/repo.git",
                "https://example.com:8080/repo.git",
                "https://user@example.com/repo.git",
                "https://user:token@example.com/repo.git",
                "https://example.com/user/repo-name.git",
                "https://example.com/user/repo_name.git",
                "https://git.example.com/user/repo.git",
                // HTTP URLs
                "http://example.com/user/repo.git",
                "http://github.com/user/repo.git",
                // Git protocol
                "git://example.com/user/repo.git",
                "git://github.com/user/repo.git",
                // SSH URLs
                "ssh://git@example.com/user/repo.git",
                "ssh://git@github.com/user/repo.git",
                "ssh://git@github.com:22/user/repo.git",
                // SCP-like SSH syntax
                "git@github.com:user/repo.git",
                "git@gitlab.com:group/project.git",
                "user@example.com:path/to/repo.git",
                // File URLs
                "file:///path/to/repo.git",
                "file:///absolute/path/repo",
                // Absolute paths
                "/path/to/repo",
                "/absolute/path/to/repository.git",
                // Relative paths
                "../relative/path/repo",
                "./current/dir/repo",
                "relative/repo",
            ]
        )
        fun `should pass when url format is valid`(validUrl: String) {
            // Given
            val remote = Remote(
                name = "origin",
                url = validUrl,
                repository = repository
            )

            // When
            val violations = validator.validate(remote)

            // Then
            val urlViolations = violations.filter { it.propertyPath.toString() == "url" }
            assertThat(urlViolations).isEmpty()
        }
    }

    @Nested
    inner class RepositoryRelationshipValidation {
        /**
         * Tests that remote properly maintains relationship with repository.
         *
         * # SEON Requirement
         * - Remote is a dependent entity requiring an owning Repository
         * - Relationship integrity is critical for domain consistency
         *
         * # Validation
         * - Repository reference must not be null
         * - Remote must be registered in repository's remotes collection
         */
        @Test
        fun `should maintain valid repository relationship`() {
            // Given
            val remote = Remote(
                name = "origin",
                url = "https://example.com/user/repo.git",
                repository = repository
            )

            // When
            val violations = validator.validate(remote)

            // Then
            assertThat(violations).isEmpty()
            assertThat(remote.repository).isNotNull
            assertThat(remote.repository).isSameAs(repository)
            assertThat(repository.remotes).contains(remote)
        }
    }

    @Nested
    inner class BusinessKeyValidation {
        /**
         * Tests uniqueness constraint within repository scope.
         *
         * # SEON Requirement
         * - Remote names must be unique within a single repository
         * - Business key is (repository.iid, name)
         * - Same name can exist across different repositories
         *
         * # Validation
         * - Duplicate names in same repository should result in same business key
         * - Different repositories can have same remote name
         */
        @Test
        fun `should generate unique business key per repository`() {
            // Given
            val remote1 = Remote(
                name = "origin",
                url = "https://example.com/user/repo1.git",
                repository = repository
            )

            val anotherProject = Project(name = "another-project")
            val anotherRepository = Repository(localPath = "/path/to/another", project = anotherProject)
            val remote2 = Remote(
                name = "origin",
                url = "https://example.com/user/repo2.git",
                repository = anotherRepository
            )

            // When
            val key1 = remote1.uniqueKey
            val key2 = remote2.uniqueKey

            // Then - Same name, different repositories → different keys
            assertThat(key1).isNotEqualTo(key2)
            assertThat(key1.repositoryId).isEqualTo(repository.iid)
            assertThat(key2.repositoryId).isEqualTo(anotherRepository.iid)
            assertThat(key1.name).isEqualTo("origin")
            assertThat(key2.name).isEqualTo("origin")
        }

        @Test
        fun `should generate same business key for same repository and name`() {
            // Given
            val remote1 = Remote(
                name = "origin",
                url = "https://example.com/user/repo1.git",
                repository = repository
            )

            val remote2 = Remote(
                name = "upstream",
                url = "https://example.com/user/repo2.git",
                repository = repository
            )
            // Change name to match remote1
            setField(
                remote2.javaClass.getDeclaredField("name").apply { isAccessible = true },
                remote2,
                "origin"
            )

            // When
            val key1 = remote1.uniqueKey
            val key2 = remote2.uniqueKey

            // Then - Same repository, same name → same business key
            assertThat(key1).isEqualTo(key2)
            assertThat(key1.repositoryId).isEqualTo(repository.iid)
            assertThat(key2.repositoryId).isEqualTo(repository.iid)
        }
    }

    @Nested
    inner class EdgeCases {
        /**
         * Tests handling of whitespace in name property.
         *
         * # SEON Requirement
         * - Business key uses trimmed name for consistency
         * - Validation should detect effectively blank names
         */
        @Test
        fun `should trim whitespace in business key name`() {
            // Given
            val remote = Remote(
                name = "origin",
                url = "https://example.com/user/repo.git",
                repository = repository
            )
            setField(
                remote.javaClass.getDeclaredField("name").apply { isAccessible = true },
                remote,
                "  origin  "
            )

            // When
            val key = remote.uniqueKey

            // Then - Business key should use trimmed name
            assertThat(key.name).isEqualTo("origin")
        }

        /**
         * Tests handling of very long remote names.
         *
         * # SEON Requirement
         * - Remote names should support reasonable lengths
         * - No artificial length restrictions beyond pattern validation
         */
        @Test
        fun `should handle very long remote names`() {
            // Given
            val longName = "remote-" + "name".repeat(50) // 255 characters
            val remote = Remote(
                name = longName,
                url = "https://example.com/user/repo.git",
                repository = repository
            )

            // When
            val violations = validator.validate(remote)

            // Then - Should pass pattern validation
            val nameViolations = violations.filter { it.propertyPath.toString() == "name" }
            assertThat(nameViolations).isEmpty()
        }

        /**
         * Tests handling of very long URLs.
         *
         * # SEON Requirement
         * - URLs can be arbitrarily long (deep paths, query params)
         * - Should support complex repository URLs
         */
        @Test
        fun `should handle very long urls`() {
            // Given
            val longPath = "path/".repeat(50)
            val longUrl = "https://example.com/user/$longPath/repo.git"
            val remote = Remote(
                name = "origin",
                url = longUrl,
                repository = repository
            )

            // When
            val violations = validator.validate(remote)

            // Then
            val urlViolations = violations.filter { it.propertyPath.toString() == "url" }
            assertThat(urlViolations).isEmpty()
        }
    }
}
