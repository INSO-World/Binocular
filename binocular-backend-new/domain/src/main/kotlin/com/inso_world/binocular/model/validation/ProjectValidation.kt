package com.inso_world.binocular.model.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.reflect.KClass

@Target(CLASS)
@Retention(RUNTIME)
@Constraint(validatedBy = [ProjectValidator::class])
annotation class ProjectValidation(
    val message: String = "Repository must reference back to this project if it exists",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
