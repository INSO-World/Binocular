package com.inso_world.binocular.model.utils

import java.lang.reflect.Field

internal class ReflectionUtils {
    companion object {
        /**
         * Mimics [org.springframework.util.ReflectionUtils.setField]
         */
        fun setField(field: Field, target: Any, value: Any?) {
            field.isAccessible = true
            field.set(target, value)
        }
    }
}
