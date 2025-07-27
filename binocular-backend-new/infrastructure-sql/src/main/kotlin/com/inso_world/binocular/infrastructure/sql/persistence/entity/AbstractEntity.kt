package com.inso_world.binocular.infrastructure.sql.persistence.entity

abstract class AbstractEntity {
    abstract fun uniqueKey(): String
}
