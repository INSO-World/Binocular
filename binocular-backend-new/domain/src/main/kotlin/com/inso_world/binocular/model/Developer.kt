package com.inso_world.binocular.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Developer(
    @field:NotBlank
    val name: String,
    @field:NotBlank
    val email: String,
) : AbstractDomainObject<Developer.Id, Developer.Key>(
    Id(Uuid.random())
) {
    data class Key(val gitSignature: String)

    @JvmInline
    value class Id(val value: Uuid)

    @Deprecated("Avoid using database specific id, use business key", ReplaceWith("iid"))
    var id: String? = null

    val gitSignature: String
        get() = "${name.trim()} "

    init {
        require(name.trim().isNotBlank()) { "name cannot be blank." }
        require(email.trim().isNotBlank()) { "email cannot be blank." }
    }

    override val uniqueKey: Key
        get() = Key(gitSignature)

    override fun equals(other: Any?) = super.equals(other)
    override fun hashCode(): Int = super.hashCode()

    override fun toString(): String =
        "Developer(id=$id, iid=$iid, name=$name, email=$email, gitSignature=$gitSignature)"
}

@Deprecated("Use Developer directly")
fun Developer.toLegacyUser(repository: Repository): User {
    val user = User(
        name = this@toLegacyUser.name,
        repository = repository
    ).apply {
        this.id = this@toLegacyUser.id
        this.email = this@toLegacyUser.email
    }

    return user
}
