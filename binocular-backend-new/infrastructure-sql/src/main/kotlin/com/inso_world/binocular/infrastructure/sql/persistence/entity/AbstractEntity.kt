package com.inso_world.binocular.infrastructure.sql.persistence.entity

import java.util.Objects

abstract class AbstractEntity {
    abstract fun uniqueKey(): String

    override fun hashCode(): Int = Objects.hash(uniqueKey())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }
}
