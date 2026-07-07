package com.inso_world.binocular.infrastructure.sql.persistence.entity

import com.inso_world.binocular.infrastructure.sql.persistence.converter.KotlinUuidConverter
import com.inso_world.binocular.model.Note
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table

/**
 * SQL-specific Note entity.
 */
@Entity
@Table(name = "notes")
internal data class NoteEntity(
    @Id
    var id: Long? = null,
    @Column(nullable = false, updatable = false, unique = true)
    @Convert(KotlinUuidConverter::class)
    var iid: Note.Id,
    @Column(columnDefinition = "TEXT")
    var body: String,
    @Column(name = "created_at")
    var createdAt: String,
    @Column(name = "updated_at")
    var updatedAt: String,
    var system: Boolean = true,
    var resolvable: Boolean = false,
    var confidential: Boolean = false,
    var internal: Boolean = false,
    var imported: Boolean = false,
    @Column(name = "imported_from")
    var importedFrom: String,
    @ManyToMany(mappedBy = "notes")
    var accounts: MutableList<AccountEntity> = mutableListOf(),
    @ManyToMany(mappedBy = "notes")
    var issues: MutableList<IssueEntity> = mutableListOf(),
) {
    // Default constructor for Hibernate
    constructor() : this(
        null,
        "",
        "",
        "",
        true,
        false,
        false,
        false,
        false,
        "",
        mutableListOf(),
        mutableListOf(),
    )

    fun toDomain(): Note = Note(
        id = this.id?.toString(),
        body = this.body,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        system = this.system,
        resolvable = this.resolvable,
        confidential = this.confidential,
        internal = this.internal,
        imported = this.imported,
        importedFrom = this.importedFrom,
        accountIds = this.accounts.map { it.iid }.toMutableSet(),
        issueIds = this.issues.map { it.iid }.toMutableSet()
    ).apply {
        // We can't easily get MRs from NoteEntity as currently defined (missing MR relationship)
    }
}

internal fun Note.toSqlEntity(): NoteEntity = NoteEntity(
    id = this.id?.toLongOrNull(),
    iid = this.iid,
    body = this.body,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
    system = this.system,
    resolvable = this.resolvable,
    confidential = this.confidential,
    internal = this.internal,
    imported = this.imported,
    importedFrom = this.importedFrom
)
