package com.inso_world.binocular.model.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NoCommitCycleValidator::class])
annotation class NoCommitCycle(
    val message: String = "Commit and its parents must not form a cycle",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
) 