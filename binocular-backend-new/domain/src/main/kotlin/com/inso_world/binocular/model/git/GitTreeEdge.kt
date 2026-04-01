package com.inso_world.binocular.model.git

/**
 * Directed edge from a commit to one of its parents.
 */
data class GitTreeEdge @JvmOverloads constructor(
    val fromCommitSha: String,
    val toCommitSha: String,
    val type: EdgeType = EdgeType.FIRST_PARENT,
)
