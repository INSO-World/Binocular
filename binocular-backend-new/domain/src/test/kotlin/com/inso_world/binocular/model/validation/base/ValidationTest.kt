package com.inso_world.binocular.model.validation.base

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator
import org.junit.jupiter.api.BeforeEach

internal open class ValidationTest {
    lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        validator =
            Validation
                .byDefaultProvider()
                .configure()
                .messageInterpolator(ParameterMessageInterpolator())
                .buildValidatorFactory()
                .validator
    }
}
