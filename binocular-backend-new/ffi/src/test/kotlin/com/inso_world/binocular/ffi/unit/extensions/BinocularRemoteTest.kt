package com.inso_world.binocular.ffi.unit.extensions

import com.inso_world.binocular.core.unit.base.BaseUnitTest
import com.inso_world.binocular.ffi.extensions.toModel
import com.inso_world.binocular.ffi.internal.GixRemote
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class BinocularRemoteTest : BaseUnitTest() {
    private lateinit var project: Project
    private lateinit var repository: Repository

    @BeforeEach
    fun setUp() {
        project = Project(name = "test-project")
        repository =
            Repository(
                localPath = "/path/to/repo",
                projectId = project.iid,
            ).apply { this.project = project }
    }

    @Nested
    inner class RemoteCreation {
        @Test
        fun `toModel creates new remote when no existing remote matches`() {
            // Verifies that a new remote is created with all properties set correctly
            val ffiRemote =
                GixRemote(
                    name = "origin",
                    url = "https://github.com/user/repo.git",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertAll(
                { assertThat(result).isNotNull },
                { assertThat(result.name).isEqualTo("origin") },
                { assertThat(result.url).isEqualTo("https://github.com/user/repo.git") },
            )
        }

        @Test
        fun `toModel creates remote with specified name and url`() {
            // Validates that FFI remote name and URL are correctly transferred to domain model
            val ffiRemote =
                GixRemote(
                    name = "fork",
                    url = "https://github.com/fork/repo.git",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertAll(
                { assertThat(result.name).isEqualTo("fork") },
                { assertThat(result.url).isEqualTo("https://github.com/fork/repo.git") },
            )
        }
    }

    // ========== Standard Git Remote Names ==========

    @Nested
    inner class StandardRemoteNames {
        @ParameterizedTest
        @ValueSource(
            strings = [
                "origin",
                "upstream",
                "fork",
                "backup",
                "mirror",
                "production",
                "staging",
                "development",
            ],
        )
        fun `toModel handles common Git remote names`(remoteName: String) {
            val ffiRemote =
                GixRemote(
                    name = remoteName,
                    url = "https://github.com/user/repo.git",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.name).isEqualTo(remoteName)
        }

        @Test
        fun `toModel handles remote name with hyphen`() {
            val ffiRemote =
                GixRemote(
                    name = "origin-https",
                    url = "https://github.com/user/repo.git",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.name).isEqualTo("origin-https")
        }

        @Test
        fun `toModel handles remote name with underscore`() {
            val ffiRemote =
                GixRemote(
                    name = "origin_ssh",
                    url = "ssh://git@github.com/user/repo.git",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.name).isEqualTo("origin_ssh")
        }

        @Test
        fun `toModel handles remote name with dot`() {
            val ffiRemote =
                GixRemote(
                    name = "origin.backup",
                    url = "https://github.com/user/repo.git",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.name).isEqualTo("origin.backup")
        }

        @Test
        fun `toModel handles remote name with slash`() {
            val ffiRemote =
                GixRemote(
                    name = "team/fork",
                    url = "https://github.com/team/repo.git",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.name).isEqualTo("team/fork")
        }
    }

    // ========== URL Protocol Support ==========

    @Nested
    inner class UrlProtocols {
        @ParameterizedTest
        @CsvSource(
            "'origin', 'https://github.com/user/repo.git'",
            "'upstream', 'http://github.com/user/repo.git'",
            "'ssh-remote', 'ssh://git@github.com/user/repo.git'",
            "'git-remote', 'git://github.com/user/repo.git'",
            "'file-remote', 'file:///path/to/repo.git'",
            "'scp-remote', 'git@github.com:user/repo.git'",
            "'local-abs', '/path/to/local/repo'",
            "'local-rel', '../relative/repo'",
            "'local-cur', './current/repo'",
        )
        fun `toModel supports various Git URL protocols`(
            name: String,
            url: String,
        ) {
            val ffiRemote = GixRemote(name = name, url = url)

            val result = ffiRemote.toModel(repository.iid)

            assertAll(
                { assertThat(result.name).isEqualTo(name) },
                { assertThat(result.url).isEqualTo(url) },
            )
        }

        @Test
        fun `toModel handles GitHub HTTPS URL`() {
            val ffiRemote =
                GixRemote(
                    name = "origin",
                    url = "https://github.com/user/repository.git",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.url).isEqualTo("https://github.com/user/repository.git")
        }

        @Test
        fun `toModel handles GitLab HTTPS URL`() {
            val ffiRemote =
                GixRemote(
                    name = "origin",
                    url = "https://gitlab.com/group/project.git",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.url).isEqualTo("https://gitlab.com/group/project.git")
        }

        @Test
        fun `toModel handles Bitbucket HTTPS URL`() {
            val ffiRemote =
                GixRemote(
                    name = "origin",
                    url = "https://bitbucket.org/team/repository.git",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.url).isEqualTo("https://bitbucket.org/team/repository.git")
        }

        @Test
        fun `toModel handles SSH URL with git user`() {
            val ffiRemote =
                GixRemote(
                    name = "origin",
                    url = "ssh://git@github.com:22/user/repo.git",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.url).isEqualTo("ssh://git@github.com:22/user/repo.git")
        }

        @Test
        fun `toModel handles URL with port number`() {
            val ffiRemote =
                GixRemote(
                    name = "origin",
                    url = "https://example.com:8080/user/repo.git",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.url).isEqualTo("https://example.com:8080/user/repo.git")
        }

        @Test
        fun `toModel handles URL with authentication credentials`() {
            val ffiRemote =
                GixRemote(
                    name = "origin",
                    url = "https://user:token@github.com/user/repo.git",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.url).isEqualTo("https://user:token@github.com/user/repo.git")
        }

        @Test
        fun `toModel handles URL with query parameters`() {
            val ffiRemote =
                GixRemote(
                    name = "origin",
                    url = "https://github.com/user/repo.git?param=value",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.url).isEqualTo("https://github.com/user/repo.git?param=value")
        }

        @Test
        fun `toModel handles URL with fragment`() {
            val ffiRemote =
                GixRemote(
                    name = "origin",
                    url = "https://github.com/user/repo.git#fragment",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.url).isEqualTo("https://github.com/user/repo.git#fragment")
        }

        @Test
        fun `toModel handles SCP-like SSH syntax for GitHub`() {
            // Tests the git@host:path format commonly used for SSH
            val ffiRemote =
                GixRemote(
                    name = "origin",
                    url = "git@github.com:user/repo.git",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.url).isEqualTo("git@github.com:user/repo.git")
        }

        @Test
        fun `toModel handles SCP-like SSH syntax for GitLab`() {
            val ffiRemote =
                GixRemote(
                    name = "origin",
                    url = "git@gitlab.com:group/project.git",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.url).isEqualTo("git@gitlab.com:group/project.git")
        }

        @Test
        fun `toModel handles SCP-like SSH syntax with custom user`() {
            val ffiRemote =
                GixRemote(
                    name = "origin",
                    url = "deploy@server.example.com:repos/app.git",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.url).isEqualTo("deploy@server.example.com:repos/app.git")
        }

        @Test
        fun `toModel handles absolute local path`() {
            // Tests absolute filesystem paths
            val ffiRemote =
                GixRemote(
                    name = "local",
                    url = "/path/to/local/repository",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.url).isEqualTo("/path/to/local/repository")
        }

        @Test
        fun `toModel handles relative path with parent directory`() {
            // Tests ../ relative paths
            val ffiRemote =
                GixRemote(
                    name = "sibling",
                    url = "../sibling-repo",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.url).isEqualTo("../sibling-repo")
        }

        @Test
        fun `toModel handles relative path with current directory`() {
            // Tests ./ relative paths
            val ffiRemote =
                GixRemote(
                    name = "local",
                    url = "./local/repo",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.url).isEqualTo("./local/repo")
        }

        @Test
        fun `toModel handles simple relative path`() {
            // Tests simple relative paths without ./ prefix
            val ffiRemote =
                GixRemote(
                    name = "local",
                    url = "relative/path/to/repo",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.url).isEqualTo("relative/path/to/repo")
        }
    }

    // ========== Edge Cases ==========

    @Nested
    inner class EdgeCases {
        @Test
        fun `toModel handles minimal valid remote name`() {
            val ffiRemote =
                GixRemote(
                    name = "x",
                    url = "https://github.com/user/repo.git",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.name).isEqualTo("x")
        }

        @Test
        fun `toModel handles very long remote name`() {
            val longName = "remote-" + "name-".repeat(50)
            val ffiRemote =
                GixRemote(
                    name = longName,
                    url = "https://github.com/user/repo.git",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.name).isEqualTo(longName)
        }

        @Test
        fun `toModel handles very long url`() {
            val longPath = "path/".repeat(100)
            val longUrl = "https://github.com/user/$longPath/repo.git"
            val ffiRemote =
                GixRemote(
                    name = "origin",
                    url = longUrl,
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.url).isEqualTo(longUrl)
        }

        @Test
        fun `toModel handles url with special characters`() {
            val ffiRemote =
                GixRemote(
                    name = "origin",
                    url = "https://github.com/user/repo-name_123.git",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.url).isEqualTo("https://github.com/user/repo-name_123.git")
        }

        @Test
        fun `toModel handles url with subdomain`() {
            val ffiRemote =
                GixRemote(
                    name = "origin",
                    url = "https://git.example.com/user/repo.git",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.url).isEqualTo("https://git.example.com/user/repo.git")
        }

        @Test
        fun `toModel handles url with deep path`() {
            val ffiRemote =
                GixRemote(
                    name = "origin",
                    url = "https://github.com/org/team/project/repo.git",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.url).isEqualTo("https://github.com/org/team/project/repo.git")
        }

        @Test
        fun `toModel handles remote name with mixed alphanumeric`() {
            val ffiRemote =
                GixRemote(
                    name = "origin123",
                    url = "https://github.com/user/repo.git",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.name).isEqualTo("origin123")
        }

        @Test
        fun `toModel handles remote name starting with number`() {
            val ffiRemote =
                GixRemote(
                    name = "123remote",
                    url = "https://github.com/user/repo.git",
                )

            val result = ffiRemote.toModel(repository.iid)

            assertThat(result.name).isEqualTo("123remote")
        }
    }

    @Test
    fun `toModel always returns non-null Remote`() {
        // Explicit null check to ensure contract is satisfied
        val ffiRemote =
            GixRemote(
                name = "origin",
                url = "https://github.com/user/repo.git",
            )

        val result = ffiRemote.toModel(repository.iid)

        assertThat(result).isNotNull
    }
}
