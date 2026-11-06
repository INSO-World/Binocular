package com.inso_world.binocular.model.vcs

enum class ReferenceCategory {
    LOCAL_BRANCH,
    REMOTE_BRANCH,
    TAG,
    NOTE,
    PSEUDO_REF,
    UNKNOWN,
    MAIN_PSEUDO_REF,
    MAIN_REF,
    LINKED_PSEUDO_REF,
    LINKED_REF,
    BISECT,
    REWRITTEN,
    WORKTREE_PRIVATE,
}
