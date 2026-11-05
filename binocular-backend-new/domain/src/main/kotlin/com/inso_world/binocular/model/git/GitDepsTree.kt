package com.inso_world.binocular.model.git

/**
 * Lightweight representation of a git commit dependency graph that can be rendered as a tree.
 */
data class GitDepsTree(
    val nodes: List<GitTreeNode>,
    val edges: List<GitTreeEdge>,
    val columns: Int,
    val rows: Int,
)
