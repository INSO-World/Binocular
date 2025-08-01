package com.inso_world.binocular.model.validation

import com.inso_world.binocular.model.Commit
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class NoCommitCycleValidator : ConstraintValidator<NoCommitCycle, Commit> {
    override fun isValid(
        commit: Commit,
        context: ConstraintValidatorContext,
    ): Boolean {
        if (commit.children.contains(commit)) {
            return false
        }
        if (commit.parents.contains(commit)) {
            return false
        }

        return true
    }
}
