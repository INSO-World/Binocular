package com.inso_world.binocular.ffi.extensions

import com.inso_world.binocular.ffi.internal.GixReferenceCategory
import com.inso_world.binocular.model.vcs.ReferenceCategory

internal fun GixReferenceCategory.toDomain(): ReferenceCategory =
    when (this) {
        GixReferenceCategory.LOCAL_BRANCH -> ReferenceCategory.LOCAL_BRANCH
        GixReferenceCategory.REMOTE_BRANCH -> ReferenceCategory.REMOTE_BRANCH
        GixReferenceCategory.TAG -> ReferenceCategory.TAG
        GixReferenceCategory.NOTE -> ReferenceCategory.NOTE
        GixReferenceCategory.PSEUDO_REF -> ReferenceCategory.PSEUDO_REF
        GixReferenceCategory.UNKNOWN -> ReferenceCategory.UNKNOWN
        GixReferenceCategory.MAIN_PSEUDO_REF -> ReferenceCategory.MAIN_PSEUDO_REF
        GixReferenceCategory.MAIN_REF -> ReferenceCategory.MAIN_REF
        GixReferenceCategory.LINKED_PSEUDO_REF -> ReferenceCategory.LINKED_PSEUDO_REF
        GixReferenceCategory.LINKED_REF -> ReferenceCategory.LINKED_REF
        GixReferenceCategory.BISECT -> ReferenceCategory.BISECT
        GixReferenceCategory.REWRITTEN -> ReferenceCategory.REWRITTEN
        GixReferenceCategory.WORKTREE_PRIVATE -> ReferenceCategory.WORKTREE_PRIVATE
    }
