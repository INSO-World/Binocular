package com.inso_world.binocular.model.validation

import com.inso_world.binocular.model.Commit
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class CommitValidator : ConstraintValidator<CommitValidation, Commit> {
    override fun isValid(
        commit: Commit?,
        context: ConstraintValidatorContext,
    ): Boolean {
        if (commit == null) return true

        val repositoryId = commit.repositoryId
        val repository = commit.repository

        // If repository is null, we can't validate the relationship
        // In this case, we'll assume it's valid (repository might be set later)
        if (repository == null) return true

        val repositoryActualId = repository.id

        return when {
            repositoryActualId == null -> repositoryId == null
            else -> repositoryId == repositoryActualId
        }
    }
}
