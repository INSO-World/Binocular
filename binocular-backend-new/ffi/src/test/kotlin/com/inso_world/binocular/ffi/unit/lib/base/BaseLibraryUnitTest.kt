package com.inso_world.binocular.ffi.unit.lib.base

import com.inso_world.binocular.core.unit.base.BaseUnitTest
import com.inso_world.binocular.ffi.util.Utils
import org.junit.jupiter.api.BeforeEach

abstract class BaseLibraryUnitTest : BaseUnitTest() {
    @BeforeEach
    fun setup() {
        Utils.Companion.loadPlatformLibrary("gix_binocular")
    }
}
