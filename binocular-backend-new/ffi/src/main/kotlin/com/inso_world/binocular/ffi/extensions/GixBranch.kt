package com.inso_world.binocular.ffi.extensions

import com.inso_world.binocular.ffi.internal.GixBranch
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository

internal fun GixBranch.toDomain(
    repositoryId: Repository.Id,
    head: Commit,
    branchRegistry: MutableMap<String, Branch> = mutableMapOf(),
): Branch {
    val existing = branchRegistry[this.name]
    if (existing != null) {
        if (existing.headCommitId != head.iid) {
            existing.headCommitId = head.iid
        }
        return existing
    }

    val branch = Branch(
        name = this.name,
        fullName = this.fullName.toString(),
        category = this.category.toDomain(),
        repositoryId = repositoryId,
        headCommitId = head.iid,
    )
    branchRegistry[this.name] = branch
    return branch
}
