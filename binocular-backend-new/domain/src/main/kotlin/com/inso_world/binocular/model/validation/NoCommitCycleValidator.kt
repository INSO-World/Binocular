package com.inso_world.binocular.model.validation

import com.inso_world.binocular.model.Commit
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class NoCommitCycleValidator : ConstraintValidator<NoCommitCycle, Commit> {
    override fun isValid(commit: Commit?, context: ConstraintValidatorContext): Boolean {
        if (commit == null) return true
        return !hasCycleBySha(commit, mutableSetOf())
    }

    private fun hasCycleBySha(commit: Commit, path: MutableSet<String>): Boolean {
        val sha = commit.sha
        if (!path.add(sha)) return true // already in path, cycle detected
        for (parent in commit.parents) {
            if (parent.sha in path) return true
            if (hasCycleBySha(parent, path)) return true
        }
        path.remove(sha)
        return false
    }
}
