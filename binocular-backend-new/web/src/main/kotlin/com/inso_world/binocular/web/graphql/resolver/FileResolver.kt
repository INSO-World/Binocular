package com.inso_world.binocular.web.graphql.resolver

import com.inso_world.binocular.core.service.FileInfrastructurePort
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.User
import com.inso_world.binocular.web.graphql.model.CommitInFile
import com.inso_world.binocular.web.graphql.model.PaginatedCommitInFileDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.stereotype.Controller
import java.time.LocalDateTime

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
    fun commits(file: File, @Argument page: Int?, @Argument perPage: Int?): PaginatedCommitInFileDto {
        val id = file.id ?: return PaginatedCommitInFileDto(
            count = 0,
            page = page ?: 1,
            perPage = perPage ?: 20,
            data = emptyList()
        )
        logger.info("Resolving commits for file: $id")
        val currentPage = (page ?: 1).coerceAtLeast(1)
        val pageSize = perPage ?: Int.MAX_VALUE
        val sort = Sort.by(Sort.Order.asc("commitDateTime"), Sort.Order.asc("sha"),)
        val pageable = PageRequest.of(currentPage - 1, pageSize, sort)
        val commitsPage = fileService.findCommitsByFileId(id, pageable)

        logger.info("First 5 commit SHAs for file $id: ${commitsPage.content.take(5).joinToString(",") { it.sha.substring(0,7) }}")

        val data = commitsPage.content.map { c -> CommitInFile(commit = c) }
        return PaginatedCommitInFileDto(
            count = commitsPage.totalElements.toInt(),
            page = currentPage,
            perPage = pageSize,
            data = data,
        )
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
