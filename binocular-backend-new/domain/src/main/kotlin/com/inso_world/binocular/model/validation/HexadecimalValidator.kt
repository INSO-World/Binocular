package com.inso_world.binocular.model.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

internal class HexadecimalValidator : ConstraintValidator<Hexadecimal, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        // Null or blank is invalid (use @NotBlank separately)
        if (value.isNullOrBlank()) {
            return false
        }

        val trimmedValue = value.trim()

        return trimmedValue.all { it.isHex() }
    }
}
