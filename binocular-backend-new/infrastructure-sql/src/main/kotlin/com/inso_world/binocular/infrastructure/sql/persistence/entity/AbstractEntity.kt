package com.inso_world.binocular.infrastructure.sql.persistence.entity

abstract class AbstractEntity<Id, Key> {
    abstract var id: Id?

    abstract val uniqueKey: Key

    override fun hashCode(): Int = id.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AbstractEntity<*, *>

        if (id != other.id) return false
        if (uniqueKey != other.uniqueKey) return false

        return true
    }
}
