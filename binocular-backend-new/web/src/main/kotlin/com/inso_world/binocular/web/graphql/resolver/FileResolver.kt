package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.core.service.FileInfrastructurePort
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class FileResolver(
    private val fileService: FileInfrastructurePort,
) {
    private val logger: Logger = LoggerFactory.getLogger(FileResolver::class.java)

    /**
     * Resolves the branches field for a File in GraphQL.
     *
     * This method retrieves all branches associated with the given file.
     * If the file ID is null, an empty list is returned.
     *
     * @param file The file for which to retrieve branches
     * @return A list of branches associated with the file, or an empty list if the file ID is null
     */
    @SchemaMapping(typeName = "File", field = "branches")
    fun branches(file: File): List<Branch> {
        val id = file.id ?: return emptyList()
        logger.info("Resolving branches for file: $id")
        // Get all connections for this file and extract the branches
        return fileService.findBranchesByFileId(id)
    }

    /**
     * Resolves the commits field for a File in GraphQL.
     *
     * This method retrieves all commits associated with the given file.
     * If the file ID is null, an empty list is returned.
     *
     * @param file The file for which to retrieve commits
     * @return A list of commits associated with the file, or an empty list if the file ID is null
     */
    @SchemaMapping(typeName = "File", field = "commits")
    fun commits(file: File): List<Commit> {
        val id = file.id ?: return emptyList()
        logger.info("Resolving commits for file: $id")
        // Get all connections for this file and extract the commits
        return fileService.findCommitsByFileId(id)
    }

    /**
     * Resolves the modules field for a File in GraphQL.
     *
     * This method retrieves all modules associated with the given file.
     * If the file ID is null, an empty list is returned.
     *
     * @param file The file for which to retrieve modules
     * @return A list of modules associated with the file, or an empty list if the file ID is null
     */
    @SchemaMapping(typeName = "File", field = "modules")
    fun modules(file: File): List<com.inso_world.binocular.model.Module> {
        val id = file.id ?: return emptyList()
        logger.info("Resolving modules for file: $id")
        // Get all connections for this file and extract the modules
        return fileService.findModulesByFileId(id)
    }

    /**
     * Resolves the relatedFiles field for a File in GraphQL.
     *
     * This method retrieves all related files associated with the given file.
     * If the file ID is null, an empty list is returned.
     *
     * @param file The file for which to retrieve related files
     * @return A list of related files associated with the file, or an empty list if the file ID is null
     */
    @SchemaMapping(typeName = "File", field = "relatedFiles")
    fun relatedFiles(file: File): List<File> {
        val id = file.id ?: return emptyList()
        logger.info("Resolving related files for file: $id")
        // Get all connections for this file and extract the related files
        return fileService.findRelatedFilesByFileId(id)
    }

    /**
     * Resolves the users field for a File in GraphQL.
     *
     * This method retrieves all users associated with the given file.
     * If the file ID is null, an empty list is returned.
     *
     * @param file The file for which to retrieve users
     * @return A list of users associated with the file, or an empty list if the file ID is null
     */
    @SchemaMapping(typeName = "File", field = "users")
    fun users(file: File): List<User> {
        val id = file.id ?: return emptyList()
        logger.info("Resolving users for file: $id")
        // Get all connections for this file and extract the users
        return fileService.findUsersByFileId(id)
    }
}
