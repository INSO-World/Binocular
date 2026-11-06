package com.inso_world.binocular.model.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * Validates that a string is a valid Git repository URL.
 *
 * Supports all common Git URL formats:
 * - HTTPS: `https://github.com/user/repo.git`
 * - HTTP: `http://github.com/user/repo.git`
 * - SSH: `ssh://git@github.com/user/repo.git`
 * - Git protocol: `git://github.com/user/repo.git`
 * - SCP-like SSH: `git@github.com:user/repo.git`
 * - File: `file:///path/to/repo.git`
 * - Absolute paths: `/path/to/repo`
 * - Relative paths: `../relative/path`
 *
 * @see GitUrlValidator
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [GitUrlValidator::class])
@MustBeDocumented
annotation class GitUrl(
    val message: String = "must be a valid Git repository URL",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)