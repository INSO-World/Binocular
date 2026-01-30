package com.inso_world.binocular.model.vcs

import com.inso_world.binocular.model.AbstractDomainObject
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.validation.GitUrl
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Remote — a named URL endpoint referencing an external Git repository location.
 *
 * In Git terminology, a **remote** is a bookmark for a URL that points to another copy
 * of the repository (typically on a server like GitHub, GitLab, or Bitbucket). The most
 * common remote name is `origin`, pointing to the repository from which the local copy
 * was cloned.
 *
 * ### SEON (Software Engineering ONtology) compliance
 * - **Remote** is a first-class entity in the Git domain model representing external repository endpoints.
 * - A remote has a **name** (e.g., "origin", "upstream") and a **URL** (fetch/push location).
 * - Remotes enable distributed version control by linking local repositories to external ones.
 *
 * ### Identity & equality
 * - Technical identity: immutable [iid] of type [Id] (generated at construction).
 * - Business key: [uniqueKey] == [Key]([repository].iid, [name]).
 * - Equality is identity-based (same [iid]); `hashCode()` derives from [iid].
 *
 * ### Construction & validation
 * - Requires a non-blank [name] (`@field:NotBlank` + runtime `require`).
 * - Requires a non-blank [url] (`@field:NotBlank` + `@field:GitUrl` + runtime `require`).
 * - The [url] must be a valid Git repository URL (supports HTTP/HTTPS, SSH, Git protocol, file paths, etc.).
 * - On construction, the remote **registers itself** to the owning [repository] via `repository.remotes.add(this)`.
 *
 * ### Relationships
 * - **Many-to-one with [Repository]:** Each remote belongs to exactly one repository.
 *   A repository may have multiple remotes (e.g., "origin", "upstream", "fork").
 * - **Naming convention:** The default remote created by `git clone` is named "origin".
 *
 * ### Git remote operations
 * - **fetch:** Downloads objects and refs from the remote URL.
 * - **push:** Uploads local refs and objects to the remote URL.
 * - **pull:** Combines fetch + merge from the remote.
 *
 * ### Thread-safety
 * - Instances are mutable (e.g., [url] can be updated) and not thread-safe.
 *   Coordinate externally if concurrent access is required.
 *
 * @property name The short, human-readable name for this remote (e.g., "origin", "upstream").
 *   Must be non-blank and unique within the [repository]. Participates in [uniqueKey].
 * @property url The complete URL to the remote repository (e.g., "https://github.com/user/repo.git").
 *   Must be non-blank. Can be HTTP(S), SSH, or Git protocol.
 * @property repository The local [Repository] that references this remote.
 *
 * ### Example
 * ```kotlin
 * val repo = Repository(localPath = "/path/to/repo", project = myProject)
 * val origin = Remote(
 *     name = "origin",
 *     url = "https://github.com/user/repo.git",
 *     repository = repo
 * )
 *
 * // The remote is automatically added to repo.remotes during construction
 * check(origin in repo.remotes)
 * ```
 *
 * ### Git documentation reference
 * - [git-remote](https://git-scm.com/docs/git-remote)
 * - [Working with Remotes](https://git-scm.com/book/en/v2/Git-Basics-Working-with-Remotes)
 */
@OptIn(ExperimentalUuidApi::class)
data class Remote(
    @field:NotBlank
    @field:Pattern(
        regexp = "^[a-zA-Z0-9._/-]+$",
        message = "Remote name must contain only alphanumeric characters, dots, underscores, slashes, or hyphens"
    )
    val name: String,

    @field:NotBlank
    @field:GitUrl
    var url: String,

    val repository: Repository,
) : AbstractDomainObject<Remote.Id, Remote.Key>(
    Id(Uuid.random())
) {
    /**
     * Type-safe wrapper for the technical identity of a [Remote].
     */
    @JvmInline
    value class Id(val value: Uuid)

    /**
     * Business key for a [Remote]: unique combination of repository ID and remote name.
     *
     * Within a single repository, remote names must be unique (Git enforces this).
     * Across repositories, the same name (e.g., "origin") can exist independently.
     */
    data class Key(val repositoryId: Repository.Id, val name: String)

    /**
     * Optional database-specific identifier.
     * @deprecated Prefer using [iid] for identity; this field is infrastructure-specific.
     */
    @Deprecated("Avoid using database specific id, use business key .iid", ReplaceWith("iid"))
    var id: String? = null

    init {
        require(name.trim().isNotBlank()) { "Remote name cannot be blank." }
        require(url.trim().isNotBlank()) { "Remote URL cannot be blank." }
        repository.remotes.add(this)
    }

    override val uniqueKey: Key
        get() = Key(repository.iid, name.trim())

    // Entities compare by immutable identity only
    override fun equals(other: Any?) = super.equals(other)
    override fun hashCode(): Int = super.hashCode()

    override fun toString(): String =
        "Remote(id=$id, iid=$iid, name='$name', url='$url', repository=${repository.uniqueKey})"
}
