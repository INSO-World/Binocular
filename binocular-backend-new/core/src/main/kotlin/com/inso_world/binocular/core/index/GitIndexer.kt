package com.inso_world.binocular.core.index

import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Revision
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.vcs.ReferenceCategory
import com.inso_world.binocular.model.vcs.Remote
import jakarta.validation.Valid
import java.nio.file.Path

/**
 * Port describing every SCM mining capability Binocular needs to populate the Software Engineering ONtology (SEON).
 *
 *
 */
interface GitIndexer {
    /**
     * Discovers a repository rooted at [path] and links it to the owning [project].
     *
     * Implementations should normalize [path], detect bare/worktree repositories, and populate
     * [Repository.project] to serve as the SEON `seon:SoftwareRepository` anchor.
     */
    fun findRepo(path: Path, project: Project): Repository
    fun traverseBranch(
        repo: Repository,
        branchName: String,
    ): Pair<Branch, List<Commit>>

    fun traverseBranch(
        repo: Repository,
        branch: Branch,
    ): Pair<Branch, List<Commit>> = traverseBranch(repo, branch.fullName)
    fun findAllBranches(repo: Repository): List<Branch>
    /**
     * Finds a commit by hash.
     */
    fun findCommit(
        repo: Repository,
        hash: String,
    ): Commit
    fun traverse(
        repo: Repository,
        source: Commit,
        target: Commit? = null,
    ): List<Commit>
}
