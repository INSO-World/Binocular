package com.inso_world.binocular.cli.service

import com.inso_world.binocular.cli.service.its.AccountService
import com.inso_world.binocular.cli.service.its.IssueService
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.github.dto.issue.ItsGitHubIssue
import com.inso_world.binocular.github.service.GitHubService
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProjectService(
    @Autowired private val projectInfrastructurePort: ProjectInfrastructurePort,
    @Autowired private val issueService: IssueService,
    @Autowired private val gitHubService: GitHubService,
    @Autowired private val accountService: AccountService,
) {
    private val logger: Logger = LoggerFactory.getLogger(ProjectService::class.java)

    fun findByName(name: String): Project? = projectInfrastructurePort.findByName(name)

    fun deleteAll() {
        this.projectInfrastructurePort.deleteAll()
    }

    @Transactional
    fun getOrCreateProject(name: String): Project {
        val find = this.findByName(name)
        if (find == null) {
            logger.info("Project '$name' does not exists, creating new project")
            return this.projectInfrastructurePort.create(
                Project(
                    name = name,
                ),
            )
        } else {
            logger.info("Project '$name' already exists")
            return find
        }
    }

    fun addIssues(issueDtos: List<ItsGitHubIssue>, project: Project, repo: String, owner: String) {

        // check for all issues if they already exist
        val existingIssueEntites = this.issueService.checkExisting(issueDtos, project)

        logger.debug("Existing issues: ${existingIssueEntites.first.count()}")
        logger.trace("New issues to add: ${existingIssueEntites.second.count()}")

        if (existingIssueEntites.second.isNotEmpty()) {
            // if any new issues were found, get accounts from GitHub
            val accounts = gitHubService.loadAllAssignableUsers(owner, repo).block()
                ?.map { it.toDomain() }
                ?: emptyList()

            // check which accounts already exist in database
            val checkedAccounts = accountService.checkExisting(accounts)
            logger.debug("Found ${checkedAccounts.first.size} existing accounts, ${checkedAccounts.second.size} new accounts")


            // transform and add these issues as they are new
            // TODO construct updated project
            this.transformIssues(project, existingIssueEntites.second, checkedAccounts)

            // update project
            this.projectInfrastructurePort.update(project)
        } else {
            // no new issues to add
            logger.info("No new issues were found, skipping update")
        }
    }

    /**
     * Transforms the ItsGitHubIssues to model Issues including their relationships.
     *
     * @param project the Project (model) to update
     * @param issues the ItsGitHubIssues to transform
     * @param checkedAccounts a Pair of Collections of existing and new domain model Accounts
     * @return Collection<Issue> the Collection of domain model Issues
     */
    fun transformIssues(
        project: Project,
        issues: Iterable<ItsGitHubIssue>,
        checkedAccounts:  Pair<Collection<Account>, Collection<Account>>
    ): Collection<Issue> {
        logger.trace(">>> transformIssues({})", project)

        // mapped via login, may cause problems once GitLab is introduced
        val accountCache =
            project.accounts.associateBy { it.login }.toMutableMap()
        // cache for new accounts loaded via GitHub API
        val newAccountCache = checkedAccounts.second.associateBy { it.login }

        // TODO connect to commits if repo and commits are not null
//        val commitCache =
//            project.repo.commits.associateBy { it.sha }.toMutableMap()

        // create a map of GitHub IDs to Issue entities for lookups
        val issueMap =
            issues.associate {
                it.id to it.toDomain()
            }

        issueMap.forEach { (id, issue) ->
            // Get the ItsGitHubIssue corresponding to this entity
            val itsIssue = issues.find { it.id == id }

            // get the assignees from the itsIssue
            itsIssue?.assignees?.nodes?.forEach { node ->
                val login = node.login

                // connect account for assignee to issue
                // case 1: account already exists in database (is in cache)
                if (login in accountCache.keys) {
                    val account = accountCache[login]
                    issue.accounts.add(account!!)
                }
                // case 2: account exists in new accounts taken from GitHub
                else if (login in newAccountCache.keys) {

                    val newAccount = newAccountCache[login]
                    if (newAccount != null) {
                        // add account to cache for other issues to find
                        accountCache[login] = newAccount
                        issue.accounts.add(newAccount)
                    } else {
                        logger.warn("Expected account for login '$login', but got null in newAccountCache")
                    }
                }
                // case 3: account is neither in db nor in GitHub response ...
                else {
                    logger.warn("No account found for login '$login', skipping assignee")
                }

                // add issue with accounts to project
                project.issues.add(issue)
            }



        }

        // for testing the connected issues/accounts
        // val issueString = project.issues.joinToString(separator = ", ") { it.toString() }
        // logger.trace(issueString)

        logger.trace("<<< transformIssues({})", project)
        return issueMap.values.toList()
    }

    //    @Transactional
    //    fun getOrCreate(
    //        gitDir: String,
    //        p: Project,
    //    ): Repository {
    //        val find = this.findRepo(gitDir)
    //        if (find == null) {
    //            logger.info("Repository does not exists, creating new repository")
    //            return this.repositoryDao.create(
    //                Repository(
    //                    name = normalizePath(gitDir),
    //                    project = p,
    //                ),
    //            )
    //        } else {
    //            logger.debug("Repository already exists, returning existing repository")
    //            return find
    //        }
    //    }
}
