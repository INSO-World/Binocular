package com.inso_world.binocular.model.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [HexadecimalValidator::class])
@MustBeDocumented
annotation class Hexadecimal(
    val message: String = "must be a valid hexadecimal string",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
