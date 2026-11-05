package com.inso_world.binocular.model.git

/**
 * A visual node for a git commit in a dependency graph.
 */
data class GitTreeNode @JvmOverloads constructor(
    val commitSha: String,
    val rowIndex: Int,
    val columnIndex: Int,
    val branchNames: Set<String> = emptySet(),
)
