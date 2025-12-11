package com.inso_world.binocular.cli.service;

import com.inso_world.binocular.cli.commands.Index
import com.inso_world.binocular.infrastructure.arangodb.service.BranchInfrastructurePortImpl;
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.BranchExportData
import com.inso_world.binocular.model.ChildCommitDetail
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Content
import com.inso_world.binocular.model.FileContent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class BranchService (
    @Autowired private val branchPort:BranchInfrastructurePortImpl,
    @Autowired private val commitService: CommitService,
    @Autowired private val userService: UserService,
) {
    companion object {
        private var logger: Logger = LoggerFactory.getLogger(Index::class.java)
    }

    fun getBranchExportData(branchId: String): BranchExportData {
        // 1. Fetch the Branch (nullable)
        val branch = getBranch(branchId)

        return branch?.let { b ->
            val commitSha = b.latestCommit ?: return@let createEmptyExportData(b.name, "No SHA")

            val commit = getLatestCommit(b) ?: run {
                println("FATAL: Commit with SHA $commitSha was retrieved but has a NULL ID. Check database mapping!")
                return@let createEmptyExportData(b.name, commitSha)
            }

            val commitId = commit.id ?: run {
                println("FATAL: Commit with SHA $commitSha was retrieved but has a NULL ID. Check database mapping!")
                return@let createEmptyExportData(b.name, commitSha)
            }
            val committerId = userService.findUserByCommit(commitId).firstOrNull()?.id ?: "N/A"

            val message = commit.message ?: "N/A"

//            val commitDateTime = commit.commitDateTime ?: return@let createEmptyExportData(b.name, commitSha)
//
//            val authorDateTime = commit.authorDateTime ?: return@let createEmptyExportData(b.name, commitSha)

            // Build file content details
            val fileContentsList = try {
                commitService.findFilesByCommitId(commitId).map { file ->
                val sourceFilePath = file.path
//                println("File states" + file.states)
                val contentList = file.states.map { fileState ->
                    val stateId = fileState.id
                    val stateContent = fileState.content

                    Content(
                        id = stateId,
                        content = stateContent
                    )
                }

                FileContent(
                    filePath = sourceFilePath,
                    content = contentList
                )
            }

        } catch (e: Exception) {
                println("Failed to retrieve or map files for commit ID: $commitId")
                println(e)
                emptyList() // Fallback to an empty list
            }

            // Build children details
            val childrenDetails = commitService.findChildrenOfCommit(commitId).map { childCommit ->
                val childCommitId = childCommit.id ?: "N/A"
                val childCommitterId = userService.findUserByCommit(childCommitId).firstOrNull()?.id ?: "N/A"

                ChildCommitDetail(
                    commitId = childCommitId,
                    committerId = childCommitterId,
                    message = message,
//                    commitDateTime = commitDateTime,
//                    authorDateTime = authorDateTime,
                )
            }

            // Return the fully assembled DTO
            BranchExportData(
                branchName = b.name,
                commitSha = commitSha,
                commitId = commitId,
                committerId = committerId,
                message = message,
//                commitDateTime = commitDateTime,
//                authorDateTime = authorDateTime,
                fileContents = fileContentsList,
                childrenCommits = childrenDetails,

            )
        } ?: createEmptyExportData("Branch ID: $branchId", "Branch not found")
        // If 'branch' was null, return the default/empty DTO here.
    }

    private fun getBranch(branchId: String): Branch? {
        val branch = branchPort.findById(branchId)
        return branch
    }

    private fun getLatestCommit(branch: Branch): Commit? {
        val commit = commitService.findBySha(branch.latestCommit ?: "N/A")
        return commit
    }

    // --- Helper function for returning a predictable empty DTO ---
    private fun createEmptyExportData(branchName: String, latestCommitSha: String): BranchExportData {
        return BranchExportData(
            branchName = branchName,
            commitSha = latestCommitSha,
            commitId = "N/A",
            committerId = "N/A",
            message = "N/A",
//            commitDateTime = LocalDateTime.of(2000, 1, 1, 0, 0),
//            authorDateTime = LocalDateTime.of(2000, 1, 1, 0, 0),
            fileContents = emptyList(),
            childrenCommits = emptyList()
        )
    }
}
