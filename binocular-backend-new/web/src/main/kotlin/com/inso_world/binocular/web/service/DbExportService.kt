package com.inso_world.binocular.web.service

/**
 * Service interface for exporting the contents of the database.
 * Provides a method to retrieve and return all data as a structured map.
 */
interface DbExportService {

    /**
     * Exports the contents of the database.
     *
     * @return A map where each key is a collection/table name and the value is the list of documents/rows.
     */
    fun exportDb(): Map<String, Any>
}
