package com.inso_world.binocular.model.validation

import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class UserValidationTest {
    private lateinit var validator: Validator

    private lateinit var repository: Repository

    @BeforeEach
    fun setUp() {
        val validatorFactory =
            Validation
                .byDefaultProvider()
                .configure()
                .messageInterpolator(ParameterMessageInterpolator())
                .buildValidatorFactory()
        validator = validatorFactory.validator

        repository = Repository(name = "test repo", project = Project(name = "test project"))
    }

    @ParameterizedTest
    @CsvSource(
        "gewerbe@brexner.com",
        "noreply@github.com",
        "johann.grabner@inso.tuwien.ac.at",
        "e1633058@student.tuwien.ac.at",
        "mail@matthiasweiss.at",
        "e1226762@student.tuwien.ac.at",
        "49699333+dependabot[bot]@users.noreply.github.com",
        "Michael.Thurner@dr-thurner.org",
        "1226762@student.tuwien.ac.at",
        "code@rala.io",
        "alexander.nemetz-fiedler@outlook.com",
        "me@juliankotrba.xyz",
        "roman.decker@gmail.com",
    )
    fun `test valid email`(mail: String) {
        val user = User(
            name = "test user",
            email = mail
        )
        repository.user.add(user)

        // When
        val violations = validator.validate(repository)

        // Then
        assertThat(violations).isEmpty()
    }
}
