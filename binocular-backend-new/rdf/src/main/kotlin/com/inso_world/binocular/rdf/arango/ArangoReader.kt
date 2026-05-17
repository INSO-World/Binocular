package com.inso_world.binocular.rdf.arango

import com.arangodb.ArangoDatabase
import org.springframework.stereotype.Component

@Component
class ArangoReader(private val arangoDb: ArangoDatabase) {

    fun readCommits(): List<Map<String, Any?>> = executeQuery("FOR x IN commits RETURN x")

    fun readUsers(): List<Map<String, Any?>> = executeQuery("FOR x IN users RETURN x")

    fun readFiles(): List<Map<String, Any?>> = executeQuery("FOR x IN files RETURN x")

    fun readModules(): List<Map<String, Any?>> = executeQuery("FOR x IN modules RETURN x")

    fun readIssues(): List<Map<String, Any?>> = executeQuery("FOR x IN issues RETURN x")

    fun readMergeRequests(): List<Map<String, Any?>> = executeQuery("FOR x IN mergeRequests RETURN x")

    fun readBuilds(): List<Map<String, Any?>> = executeQuery("FOR x IN builds RETURN x")

    fun readBranches(): List<Map<String, Any?>> = executeQuery("FOR x IN branches RETURN x")

    fun readCommitsUsers(): List<Map<String, Any?>> = executeQuery("FOR x IN `commits-users` RETURN x")

    fun readCommitsFiles(): List<Map<String, Any?>> = executeQuery("FOR x IN `commits-files` RETURN x")

    fun readIssuesCommits(): List<Map<String, Any?>> = executeQuery("FOR x IN `issues-commits` RETURN x")

    fun readCommitsModules(): List<Map<String, Any?>> = executeQuery("FOR x IN `commits-modules` RETURN x")

    fun readCommitsBuilds(): List<Map<String, Any?>> = executeQuery("FOR x IN `commits-builds` RETURN x")

    fun readModulesFiles(): List<Map<String, Any?>> = executeQuery("FOR x IN `modules-files` RETURN x")

    fun readModulesModules(): List<Map<String, Any?>> = executeQuery("FOR x IN `modules-modules` RETURN x")

    @Suppress("UNCHECKED_CAST")
    private fun executeQuery(query: String): List<Map<String, Any?>> {
        val cursor = arangoDb.query(query, Map::class.java)
        return cursor.asListRemaining() as List<Map<String, Any?>>
    }
}
