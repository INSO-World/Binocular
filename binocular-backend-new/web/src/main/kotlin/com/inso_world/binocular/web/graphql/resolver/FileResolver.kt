package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.web.entity.Branch
import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.service.FileService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class FileResolver(
    private val fileService: FileService
) {
    private val logger: Logger = LoggerFactory.getLogger(FileResolver::class.java)
    @SchemaMapping(typeName = "File", field = "branches")
    fun branches(file: File): List<Branch> {
        val id = file.id ?: return emptyList()
        logger.info("Resolving branches for file: $id")
        // Get all connections for this file and extract the branches
        return fileService.findBranchesByFileId(id)
    }

    @SchemaMapping(typeName = "File", field = "commits")
    fun commits(file: File): List<Commit> {
        val id = file.id ?: return emptyList()
        logger.info("Resolving commits for file: $id")
        // Get all connections for this file and extract the commits
        return fileService.findCommitsByFileId(id)
    }

    @SchemaMapping(typeName = "File", field = "modules")
    fun modules(file: File): List<Module> {
        val id = file.id ?: return emptyList()
        logger.info("Resolving modules for file: $id")
        // Get all connections for this file and extract the modules
        return fileService.findModulesByFileId(id)
    }

    @SchemaMapping(typeName = "File", field = "relatedFiles")
    fun relatedFiles(file: File): List<File> {
        val id = file.id ?: return emptyList()
        logger.info("Resolving related files for file: $id")
        // Get all connections for this file and extract the related files
        return fileService.findRelatedFilesByFileId(id)
    }

    @SchemaMapping(typeName = "File", field = "users")
    fun users(file: File): List<User> {
        val id = file.id ?: return emptyList()
        logger.info("Resolving users for file: $id")
        // Get all connections for this file and extract the users
        return fileService.findUsersByFileId(id)
    }
}
