package com.inso_world.binocular.web.graphql

import com.inso_world.binocular.web.entity.*
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class RelationshipController {
  
  // Commit relationships
  @SchemaMapping(typeName = "Commit", field = "builds")
  fun getCommitBuilds(commit: Commit): List<Build>? {
    return commit.builds
  }
  
  @SchemaMapping(typeName = "Commit", field = "files")
  fun getCommitFiles(commit: Commit): List<File>? {
    return commit.files
  }
  
  @SchemaMapping(typeName = "Commit", field = "modules")
  fun getCommitModules(commit: Commit): List<Module>? {
    return commit.modules
  }
  
  @SchemaMapping(typeName = "Commit", field = "users")
  fun getCommitUsers(commit: Commit): List<User>? {
    return commit.users
  }
  
  @SchemaMapping(typeName = "Commit", field = "issues")
  fun getCommitIssues(commit: Commit): List<Issue>? {
    return commit.issues
  }
  
  // Account relationships
  @SchemaMapping(typeName = "Account", field = "issues")
  fun getAccountIssues(account: Account): List<Issue>? {
    return account.issues
  }
  
  @SchemaMapping(typeName = "Account", field = "mergeRequests")
  fun getAccountMergeRequests(account: Account): List<MergeRequest>? {
    return account.mergeRequests
  }
  
  @SchemaMapping(typeName = "Account", field = "notes")
  fun getAccountNotes(account: Account): List<Note>? {
    return account.notes
  }
  
  // Branch relationships
  @SchemaMapping(typeName = "Branch", field = "files")
  fun getBranchFiles(branch: Branch): List<File>? {
    return branch.files
  }
  
  // Build relationships
  @SchemaMapping(typeName = "Build", field = "commits")
  fun getBuildCommits(build: Build): List<Commit>? {
    return build.commits
  }
  
  // File relationships
  @SchemaMapping(typeName = "File", field = "commits")
  fun getFileCommits(file: File): List<Commit>? {
    return file.commits
  }
  
  @SchemaMapping(typeName = "File", field = "branches")
  fun getFileBranches(file: File): List<Branch>? {
    return file.branches
  }
  
  @SchemaMapping(typeName = "File", field = "modules")
  fun getFileModules(file: File): List<Module>? {
    return file.modules
  }
  
  // Issue relationships
  @SchemaMapping(typeName = "Issue", field = "accounts")
  fun getIssueAccounts(issue: Issue): List<Account>? {
    return issue.accounts
  }
  
  @SchemaMapping(typeName = "Issue", field = "commits")
  fun getIssueCommits(issue: Issue): List<Commit>? {
    return issue.commits
  }
  
  @SchemaMapping(typeName = "Issue", field = "milestones")
  fun getIssueMilestones(issue: Issue): List<Milestone>? {
    return issue.milestones
  }
  
  @SchemaMapping(typeName = "Issue", field = "notes")
  fun getIssueNotes(issue: Issue): List<Note>? {
    return issue.notes
  }
  
  @SchemaMapping(typeName = "Issue", field = "users")
  fun getIssueUsers(issue: Issue): List<User>? {
    return issue.users
  }
  
  // MergeRequest relationships
  @SchemaMapping(typeName = "MergeRequest", field = "accounts")
  fun getMergeRequestAccounts(mergeRequest: MergeRequest): List<Account>? {
    return mergeRequest.accounts
  }
  
  @SchemaMapping(typeName = "MergeRequest", field = "milestones")
  fun getMergeRequestMilestones(mergeRequest: MergeRequest): List<Milestone>? {
    return mergeRequest.milestones
  }
  
  @SchemaMapping(typeName = "MergeRequest", field = "notes")
  fun getMergeRequestNotes(mergeRequest: MergeRequest): List<Note>? {
    return mergeRequest.notes
  }
  
  // Milestone relationships
  @SchemaMapping(typeName = "Milestone", field = "issues")
  fun getMilestoneIssues(milestone: Milestone): List<Issue>? {
    return milestone.issues
  }
  
  @SchemaMapping(typeName = "Milestone", field = "mergeRequests")
  fun getMilestoneMergeRequests(milestone: Milestone): List<MergeRequest>? {
    return milestone.mergeRequests
  }
  
  // Module relationships
  @SchemaMapping(typeName = "Module", field = "commits")
  fun getModuleCommits(module: Module): List<Commit>? {
    return module.commits
  }
  
  @SchemaMapping(typeName = "Module", field = "files")
  fun getModuleFiles(module: Module): List<File>? {
    return module.files
  }
  
  @SchemaMapping(typeName = "Module", field = "childModules")
  fun getModuleChildModules(module: Module): List<Module>? {
    return module.childModules
  }
  
  @SchemaMapping(typeName = "Module", field = "parentModules")
  fun getModuleParentModules(module: Module): List<Module>? {
    return module.parentModules
  }
  
  // Note relationships
  @SchemaMapping(typeName = "Note", field = "accounts")
  fun getNoteAccounts(note: Note): List<Account>? {
    return note.accounts
  }
  
  @SchemaMapping(typeName = "Note", field = "issues")
  fun getNoteIssues(note: Note): List<Issue>? {
    return note.issues
  }
  
  @SchemaMapping(typeName = "Note", field = "mergeRequests")
  fun getNoteMergeRequests(note: Note): List<MergeRequest>? {
    return note.mergeRequests
  }
  
  // User relationships
  @SchemaMapping(typeName = "User", field = "commits")
  fun getUserCommits(user: User): List<Commit>? {
    return user.commits
  }
  
  @SchemaMapping(typeName = "User", field = "issues")
  fun getUserIssues(user: User): List<Issue>? {
    return user.issues
  }
}
