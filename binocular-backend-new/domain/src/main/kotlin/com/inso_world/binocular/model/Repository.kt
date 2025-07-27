package com.inso_world.binocular.model

import com.inso_world.binocular.model.validation.CommitValidation
import com.inso_world.binocular.model.validation.NoCommitCycle
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@CommitValidation
@NoCommitCycle
class Repository(
    val id: String? = null,
    @field:NotBlank
    val name: String,
    @field:Valid
    var commits: MutableSet<Commit> = mutableSetOf(),
    var user: MutableSet<User> = mutableSetOf(),
    @field:Valid
    var branches: MutableSet<Branch> = mutableSetOf(),
    @field:NotNull // TODO conditional validation, only when coming out of infra
    var project: Project? = null,
) {
    private val logger: Logger = LoggerFactory.getLogger(Repository::class.java)

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
            if (branch.commitShas.remove(sha)) {
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
