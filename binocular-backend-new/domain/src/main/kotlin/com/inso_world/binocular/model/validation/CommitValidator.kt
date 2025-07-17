package com.inso_world.binocular.model.validation

import com.inso_world.binocular.model.Repository
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class CommitValidator : ConstraintValidator<CommitValidation, Repository> {
    override fun isValid(
        repository: Repository,
        context: ConstraintValidatorContext,
    ): Boolean {
        val checks =
            repository.commits
                .mapIndexed { index, commit ->
                    val repositoryId = commit.repositoryId
                    val repositoryActualId = repository.id

                    when {
                        repositoryActualId == null -> {
                            if (repositoryId != null) {
                                context.disableDefaultConstraintViolation()
                                context
                                    .buildConstraintViolationWithTemplate(
                                        "Repository ID of Commit=${commit.sha} is null, but commit has a repositoryId=$repositoryActualId.",
                                    ).addPropertyNode("commits")
                                    .addPropertyNode("repositoryId")
                                    .inIterable()
                                    .addConstraintViolation()
                                return@mapIndexed false
                            }
                        }

                        repositoryId != repositoryActualId -> {
                            context.disableDefaultConstraintViolation()
                            context
                                .buildConstraintViolationWithTemplate(
                                    "Commit repositoryId=$repositoryId does not match repository.id=$repositoryActualId.",
                                ).addPropertyNode("commits")
                                .addPropertyNode("repositoryId")
                                .inIterable()
                                .addConstraintViolation()
                            return@mapIndexed false
                        }
                    }
                    true
                }
        return checks.all { it }
    }
}
