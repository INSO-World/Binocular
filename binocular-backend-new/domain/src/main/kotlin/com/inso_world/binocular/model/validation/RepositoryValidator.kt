package com.inso_world.binocular.model.validation

import com.inso_world.binocular.model.Project
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class RepositoryValidator : ConstraintValidator<RepositoryValidation, Project> {
    override fun isValid(
        project: Project?,
        context: ConstraintValidatorContext,
    ): Boolean {
        if (project == null) return true

        val projectId = project.id
        val repository = project.repo

        // If project is null, we can't validate the relationship
        // In this case, we'll assume it's valid (project might be set later)
        if (repository == null) return true

        val projectActualId = repository.project?.id

        return when {
            projectActualId == null -> projectId == null
            else -> projectId == projectActualId
        }
    }
}
