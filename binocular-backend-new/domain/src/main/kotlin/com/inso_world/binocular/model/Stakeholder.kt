package com.inso_world.binocular.model

/**
 * Abstract base class representing any person involved in a project.
 *
 * ## Purpose
 * Stakeholder serves as the root type in the person hierarchy, abstracting
 * common identity attributes ([name], [email]) that all project participants share.
 * Concrete subtypes include [Developer] (Git users with repository-scoped identity).
 *
 * ## Identity & Equality
 * - Inherits entity identity from [AbstractDomainObject].
 * - Technical id: [iid] of generic type [Iid], generated at construction.
 * - Business key: [uniqueKey] of generic type [Key], defined by subclasses.
 * - Equality follows [AbstractDomainObject]: same runtime class **and** equal [iid] and [uniqueKey].
 *
 * ## Design Rationale
 * - Separates identity concerns (name/email) from role-specific behavior (e.g., commit authorship).
 * - Enables future extension for non-developer stakeholders (reviewers, project managers, etc.).
 * - Provides type-safe base for collections that may contain mixed stakeholder types.
 *
 * ## Subclassing Contract
 * - Subclasses must provide [name] and [email] implementations (non-null, typically non-blank).
 * - Subclasses should override `equals`/`hashCode` to delegate to super (avoid data class auto-generation).
 *
 * @param Iid The type of the technical identifier (e.g., `Developer.Id`).
 * @param Key The type of the business key (e.g., `Developer.Key`).
 * @property name Display name of the stakeholder; must be non-blank in concrete implementations.
 * @property email Email address of the stakeholder; must be non-blank in concrete implementations.
 * @see Developer
 * @see AbstractDomainObject
 */
abstract class Stakeholder<Iid, Key>(
    iid: Iid
) : AbstractDomainObject<Iid, Key>(iid) {

    /**
     * Display name of the stakeholder.
     * Concrete implementations should validate non-blank values.
     */
    abstract val name: String

    /**
     * Email address of the stakeholder.
     * Concrete implementations should validate non-blank values.
     */
    abstract val email: String
}
