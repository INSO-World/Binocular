package com.inso_world.binocular.model

import com.inso_world.binocular.model.validation.CommitValidation
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@CommitValidation
data class Repository(
    val id: String? = null,
    @field:NotBlank
    val name: String,
    @field:NotNull // TODO conditional validation, only when coming out of infra
    var project: Project? = null,
) {
    private val logger: Logger = LoggerFactory.getLogger(Repository::class.java)
    private val _commits: MutableSet<Commit> = mutableSetOf()
    private val _branches: MutableSet<Branch> = mutableSetOf()
    private val _user: MutableSet<User> = mutableSetOf()

    @get:Valid
    val commits: MutableSet<Commit> =
        object : MutableSet<Commit> by _commits {
            override fun add(element: Commit): Boolean {
                val added = _commits.add(element)
                if (added) {
                    element.repository = this@Repository
                }
                return added
            }

            override fun addAll(elements: Collection<Commit>): Boolean {
                // for bulk‐adds make sure each one gets the same treatment
                var anyAdded = false
                for (e in elements) {
                    if (add(e)) anyAdded = true
                }
                return anyAdded
            }
        }

    @get:Valid
    val branches: MutableSet<Branch> =
        object : MutableSet<Branch> by _branches {
            override fun add(element: Branch): Boolean {
                val added = _branches.add(element)
                if (added) {
                    element.repository = this@Repository
                }
                return added
            }

            override fun addAll(elements: Collection<Branch>): Boolean {
                // for bulk‐adds make sure each one gets the same treatment
                var anyAdded = false
                for (e in elements) {
                    if (add(e)) anyAdded = true
                }
                return anyAdded
            }
        }

    @field:Valid
    val user: MutableSet<User> =
        object : MutableSet<User> by _user {
            override fun add(element: User): Boolean {
                val added = _user.add(element)
                if (added) {
                    element.repository = this@Repository
                }
                return added
            }

            override fun addAll(elements: Collection<User>): Boolean {
                // for bulk‐adds make sure each one gets the same treatment
                var anyAdded = false
                for (e in elements) {
                    if (add(e)) anyAdded = true
                }
                return anyAdded
            }
        }

    override fun toString(): String = "Repository(id=$id, name='$name')"

    fun removeCommitBySha(
        @Size(min = 40, max = 40)
        sha: String,
    ) {
        // Remove the commit from the repository's commits set
        require(commits.removeIf { it.sha == sha })
        // Remove the commit sha from all branches' commitShas sets
        val affectedBranchNames = mutableListOf<String>()
        branches.forEach { branch ->
            if (branch.commits.removeIf { it.sha == sha }) {
                affectedBranchNames.add(branch.name)
            }
        }
        logger.trace(
            "Removed commit sha '{}' from {} branches: {}",
            sha,
            affectedBranchNames.size,
            affectedBranchNames.joinToString(", "),
        )
    }
}
