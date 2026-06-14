package com.inso_world.binocular.infrastructure.sql.persistence.entity

import com.inso_world.binocular.infrastructure.sql.persistence.converter.KotlinUuidConverter
import com.inso_world.binocular.model.Account
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Entity
@Table(
    name = "accounts",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["platform", "login"]),
    ],
)
internal data class AccountEntity(
    @Column(nullable = false)
    var login: String,
    @Column(nullable = true)
    var name: String? = null,
    @Column(nullable = true)
    var email: String? = null,
    @Column(nullable = true)
    var url: String? = null,
    @Column(name = "avatar_url", nullable = true)
    var avatarUrl: String? = null,
    @Column(nullable = false)
    var platform: String,
    @Column(nullable = false, updatable = false, unique = true)
    @Convert(converter = KotlinUuidConverter::class)
    val iid: Account.Id,
) : AbstractEntity<Long, AccountEntity.Key>() {

    data class Key(val platform: String, val login: String)

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    override var id: Long? = null

    override val uniqueKey: Key
        get() = Key(platform = platform, login = login)

    override fun equals(other: Any?): Boolean = super.equals(other)
    override fun hashCode(): Int = super.hashCode()
}

@OptIn(ExperimentalUuidApi::class)
internal fun Account.toEntity(): AccountEntity =
    AccountEntity(
        iid = this.iid,
        login = this.login ?: "",
        name = this.name,
        email = this.email,
        url = this.url,
        avatarUrl = this.avatarUrl,
        platform = this.platform?.name ?: "GITHUB",
    ).apply {
        id = this@toEntity.id?.trim()?.toLongOrNull()
    }
