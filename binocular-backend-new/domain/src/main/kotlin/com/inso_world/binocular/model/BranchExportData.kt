package com.inso_world.binocular.model

data class BranchExportData(
    val branchName: String,
    val branchId: String,

    // Latest Commit Information
    val commitSha: String,
    val commitId: String,
    val committerId: String,
    val message: String,
//    val commitDateTime: LocalDateTime,
//    val authorDateTime: LocalDateTime,

    //Link to the Commit's Content Snapshot
    val fileContents: List<FileContent>,

    // Child Commits Information
    val childrenCommits: List<ChildCommitDetail>
)

// A simple inner class or nested data class for the repeating child items
data class ChildCommitDetail(
    val commitSha: String,
    val commitId: String,
    val committerId: String,
    val message: String,
//    val commitDateTime: LocalDateTime,
//    val authorDateTime: LocalDateTime,
)

data class FileContent (
    val filePath: String,
    val content: List<Content>
)

data class Content (
    val id: String?,
    val contentText: String?
)
