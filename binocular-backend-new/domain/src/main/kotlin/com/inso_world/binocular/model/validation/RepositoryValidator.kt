package com.inso_world.binocular.model.validation

import com.inso_world.binocular.model.Repository
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class RepositoryValidator : ConstraintValidator<RepositoryValidation, Repository> {
    
    override fun isValid(repository: Repository?, context: ConstraintValidatorContext): Boolean {
        if (repository == null) return true
        
        val projectId = repository.projectId
        val project = repository.project
        
        // If project is null, we can't validate the relationship
        // In this case, we'll assume it's valid (project might be set later)
        if (project == null) return true
        
        val projectActualId = project.id
        
        return when {
            projectActualId == null -> projectId == null
            else -> projectId == projectActualId
        }
    }
} 