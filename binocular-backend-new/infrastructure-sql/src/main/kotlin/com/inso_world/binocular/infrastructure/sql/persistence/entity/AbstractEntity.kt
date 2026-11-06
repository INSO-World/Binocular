package com.inso_world.binocular.infrastructure.sql.persistence.entity

abstract class AbstractEntity<Id, Key> {
    abstract var id: Id?

    abstract val uniqueKey: Key

    override fun hashCode(): Int = uniqueKey.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AbstractEntity<*, *>

        if (uniqueKey == other.uniqueKey) return true
        if (id != other.id) return false

        return true
    }
}
