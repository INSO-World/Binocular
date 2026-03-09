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
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.treewalk.TreeWalk
import java.io.File as JFile
import com.inso_world.binocular.cli.config.ExportConfigLoader
import com.inso_world.binocular.cli.config.ExportSelectionConfig
import org.eclipse.jgit.lib.ObjectId

@Service
class BranchService (
    @Autowired private val branchPort:BranchInfrastructurePortImpl,
    @Autowired private val commitService: CommitService,
    @Autowired private val userService: UserService,
) {
    companion object {
        private var logger: Logger = LoggerFactory.getLogger(Index::class.java)
    }

    fun getBranchExportData(
        branchIdentifier: String,
        repoPath: String,
        exportAll: Boolean,
        includeContent: Boolean
    ): BranchExportData {
        val branch = getBranch(branchIdentifier)

        return branch?.let { b ->
            val commitSha = b.head.sha ?: return@let createEmptyExportData(b.name, branchIdentifier, "No SHA")

            val commit = getLatestCommit(b) ?: run {
                println("FATAL: Commit with SHA $commitSha was retrieved but has a NULL ID. Check database mapping!")
                return@let createEmptyExportData(b.name, branchIdentifier, commitSha)
            }
            //Use IID not id
            val commitId = commit.id ?: run {
                println("FATAL: Commit with SHA $commitSha was retrieved but has a NULL ID. Check database mapping!")
                return@let createEmptyExportData(b.name, branchIdentifier,commitSha)
            }

            val gitFolder = JFile(repoPath, ".git")
            if (!gitFolder.exists()) {
                throw IllegalArgumentException("No git repository found at $repoPath")
            }
            val repository = Git.open(gitFolder).repository
            val cfg = if (exportAll) {
                ExportConfigLoader.exportAllConfig()
            } else {
                ExportConfigLoader.loadDefaultPolicy()
            }

            val fileContentList = getSnapshotFromGit(repository, commitSha, cfg, includeContent, exportAll)

            val committerId = userService.findUserByCommit(commitId).firstOrNull()?.id ?: "N/A"

            val message = commit.message ?: "N/A"

//            val commitDateTime = commit.commitDateTime ?: return@let createEmptyExportData(b.name, commitSha)
//
//            val authorDateTime = commit.authorDateTime ?: return@let createEmptyExportData(b.name, commitSha)

            // Build children details
            val childrenDetails = commitService.findChildrenOfCommit(commitId).map { childCommit ->
                val childCommitId = childCommit.id ?: "N/A"
                val childCommitterId = userService.findUserByCommit(childCommitId).firstOrNull()?.id ?: "N/A"
                val commitSha = childCommit.sha

                ChildCommitDetail(
                    commitSha = commitSha,
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
                branchId = branchIdentifier,
                commitSha = commitSha,
                commitId = commitId,
                committerId = committerId,
                message = message,
//                commitDateTime = commitDateTime,
//                authorDateTime = authorDateTime,
                fileContents = fileContentList,
                childrenCommits = childrenDetails,

            )
        } ?: createEmptyExportData("Branch name not found.","Branch ID: $branchIdentifier", "Branch not found")
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
    private fun createEmptyExportData(branchName: String, branchIdentifier: String, latestCommitSha: String): BranchExportData {
        return BranchExportData(
            branchName = branchName,
            branchId = branchIdentifier,
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

    private fun getSnapshotFromGit(
        repository: Repository,
        sha: String,
        cfg: ExportSelectionConfig,
        includeContent: Boolean,
        exportAll: Boolean
    ): List<FileContent>
    {
        val fileList = mutableListOf<FileContent>()

        val commitId = repository.resolve(sha) ?: return emptyList()
        val revWalk = RevWalk(repository)
        val revCommit = revWalk.parseCommit(commitId)

        val treeWalk = TreeWalk(repository)
        treeWalk.addTree(revCommit.tree)
        treeWalk.isRecursive = true

        while (treeWalk.next()) {
            if  (fileList.size > cfg.maxFiles) break

            val path = treeWalk.pathString


                if (cfg.includePrefixes.isNotEmpty() && cfg.includePrefixes.none {
                        path.startsWith(it)
                    }) continue

                if (cfg.excludePrefixes.any { path.startsWith(it) }) continue

            val objectId = treeWalk.getObjectId(0)
            val blobSha = objectId.name // This is the Blob SHA

            val contentString: String? =
                if (!includeContent) {
                    "Content omitted for export size"
                } else {
                    readBlobAsText(repository, objectId, cfg)
                }

            fileList.add(FileContent(
                filePath = path,
                content = listOf(
                    Content(
                        id = blobSha,
                        contentText = contentString
                    )
                )
            ))
        }
        return fileList
    }
}

private fun readBlobAsText(repository: Repository, objectId: ObjectId, cfg: ExportSelectionConfig): String {
    val loader = repository.open(objectId)

    if (loader.size > cfg.maxBlobBytes) {
        return "Content omitted: blob too large (${loader.size} bytes)"
    }

    val bytes = loader.getCachedBytes(cfg.maxBlobBytes.toInt())

    if (cfg.skipBinary && bytes.any { it == 0.toByte() }) {
        return "Content omitted: binary file"
    }

    return String(bytes, Charsets.UTF_8)
}
