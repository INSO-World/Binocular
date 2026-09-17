package com.inso_world.binocular.ffi.extensions

import com.inso_world.binocular.ffi.internal.GixDiff
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository

internal fun GixDiff.toDomain(
    repository: Repository,
    shaIndex: Map<String, Commit>? = null,
): Any {
    
    return this
}

internal fun Collection<GixDiff>.toDomain(repository: Repository): List<Any> {
    //// Seed a quick lookup from existing repo state (canonical instances).
    //val bySha = repository.commits.associateBy { it.uniqueKey.sha }.toMutableMap()
//
    //// ---- Pass 1: materialize commits using the single-item mapper with index ----
    //val mappedInOrder: List<Commit> =
    //    this.map { vec ->
    //        val c = vec.toDomain(repository, bySha) // <— reuse single-item logic with O(1) lookup
    //        bySha.putIfAbsent(c.sha, c)
    //        c
    //    }
//
    //// ---- Pass 2: wire parent edges (and implicit child back-links by domain invariants) ----
    //this.forEach { vec ->
    //    val child = bySha.getValue(vec.oid)
    //    vec.parents.forEach { parentSha ->
    //        parentSha.validateSha() // Validate parent SHA early
    //        val parent = bySha.getValue(parentSha)
    //        child.parents.add(parent) // domain ensures repository consistency & back-link to children
    //    }
    //}
//
    //return mappedInOrder
    return this.map { diff -> diff.toDomain(repository) }
}
