package com.inso_world.binocular.model.git

/**
 * Edge type for the git dependency graph.
 *
 * FIRST_PARENT: the mainline edge.
 * MERGE_PARENT: additional parent edges (merge).
 */
enum class EdgeType {
    FIRST_PARENT,
    MERGE_PARENT,
}
