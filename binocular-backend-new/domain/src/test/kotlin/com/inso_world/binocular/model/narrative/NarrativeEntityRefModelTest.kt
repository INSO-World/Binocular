package com.inso_world.binocular.model.narrative

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

/**
 * BDD tests for the NarrativeEntityRef value object.
 * A NarrativeEntityRef points at the commit, issue or build a narrative event is about.
 */
class NarrativeEntityRefModelTest {
    /** Empty and whitespace-only refs cannot identify an entity and must be rejected. */
    @ParameterizedTest
    @ValueSource(strings = ["", " "])
    fun `given blank ref, when creating entity ref, then it throws IllegalArgumentException`(ref: String) {
        // When & Then
        assertThrows<IllegalArgumentException> {
            NarrativeEntityRef(source = NarrativeSource.VCS, ref = ref)
        }
    }

    /** The key joins source and ref, so the same ref in two sources (here 431) yields different keys. */
    @ParameterizedTest
    @CsvSource("VCS, a1b2c3d, VCS:a1b2c3d", "ITS, 431, ITS:431", "CI, 431, CI:431")
    fun `given entity ref, when reading key, then it is source and ref joined by a colon`(
        source: NarrativeSource,
        ref: String,
        expectedKey: String,
    ) {
        // When
        val entity = NarrativeEntityRef(source = source, ref = ref)

        // Then
        assertThat(entity.key).isEqualTo(expectedKey)
    }
}
