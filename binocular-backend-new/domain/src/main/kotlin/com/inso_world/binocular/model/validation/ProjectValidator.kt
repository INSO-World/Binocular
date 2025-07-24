package com.inso_world.binocular.model.validation

import com.inso_world.binocular.model.Project
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class ProjectValidator : ConstraintValidator<ProjectValidation, Project> {
    override fun isValid(
        project: Project?,
        context: ConstraintValidatorContext,
    ): Boolean {
        if (project == null) return true

        val repository = project.repo

        // If repository is null, the relationship is valid
        if (repository == null) return true

        // Validate that the repository's projectId matches this project's id
        val repositoryProjectId = repository.project?.id
        val projectId = project.id

        return when {
            projectId == null -> repositoryProjectId == null
            else -> repositoryProjectId == projectId
        }
    }
}
