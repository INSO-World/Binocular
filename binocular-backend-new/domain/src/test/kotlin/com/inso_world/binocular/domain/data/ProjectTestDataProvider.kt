package com.inso_world.binocular.data

import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Platform
import com.inso_world.binocular.model.Project

class ProjectTestDataProvider {
    // projects
    val testProjects =
        listOf(
            Project(name = "proj-pg-0"),
            Project(name = "proj-pg-1"),
            Project(name = "proj-pg-2"),
            Project(name = "proj-pg-3"),
            Project(name = "proj-pg-4"),
            Project(name = "proj-pg-5"),
        )
    val projectsByName = testProjects.associateBy { requireNotNull(it.name) }

    // accounts
    val accounts: List<Account> =
        listOf(
            run {
                val account =
                    Account(gid = "gid1", platform = Platform.GitHub, login = "account1").apply {
                        this.projects.add(projectsByName.getValue("proj-pg-0"))
                        this.projects.add(projectsByName.getValue("proj-pg-4"))
                    }
                account
            },
            run {
                val account =
                    Account(gid = "gid2", platform = Platform.GitHub, login = "account2").apply {
                        this.projects.add(projectsByName.getValue("proj-pg-0"))
                        this.projects.add(projectsByName.getValue("proj-pg-4"))
                    }
                account
            },
            run {
                val account =
                    Account(gid = "gid3", platform = Platform.GitHub, login = "account3").apply {
                        this.projects.add(projectsByName.getValue("proj-pg-0"))
                        this.projects.add(projectsByName.getValue("proj-pg-4"))
                    }
                account
            },
            run {
                val account = Account(gid = "gid4", platform = Platform.GitHub, login = "account4")
                account
            },
            run {
                val account = Account(gid = "gid5", platform = Platform.GitHub, login = "account5")
                account
            }
        )
    val accountByLogin = accounts.associateBy { requireNotNull(it.login) }

    // issues
    val issues: List<Issue> =
        listOf(
            run {
                val issue =
                    Issue(
                        platformIid = 1,
                        gid = "1abc",
                        project = projectsByName.getValue("proj-pg-0").iid
                    )
                issue
            },
            run {
                val issue =
                    Issue(
                        platformIid = 2,
                        gid = "2abc",
                        project = projectsByName.getValue("proj-pg-0").iid
                    )
                issue
            },
            run {
                val issue =
                    Issue(
                        platformIid = 3,
                        gid = "3abc",
                        project = projectsByName.getValue("proj-pg-0").iid
                    )
                issue
            },
        )
    val issueByGid = issues.associateBy { requireNotNull(it.gid) }

    init {
        val project = projectsByName.getValue("proj-pg-0")
        val project4 = projectsByName.getValue("proj-pg-4")

        // --- link accounts to project
        accountByLogin.getValue("account1").projects.add(project)
        accountByLogin.getValue("account1").projects.add(project4)
        accountByLogin.getValue("account2").projects.add(project)
        accountByLogin.getValue("account2").projects.add(project4)
        accountByLogin.getValue("account3").projects.add(project)
        accountByLogin.getValue("account3").projects.add(project4)

        // --- issue 1
        issueByGid.getValue("1abc").apply {
            author = accountByLogin.getValue("account1")
            accounts.add(accountByLogin.getValue("account1"))
            accounts.add(accountByLogin.getValue("account2"))
        }

        // --- issue 2
        issueByGid.getValue("2abc").apply {
            author = accountByLogin.getValue("account2")
            accounts.add(accountByLogin.getValue("account1"))
            accounts.add(accountByLogin.getValue("account2"))
        }

        // --- issue 3
        issueByGid.getValue("3abc").apply {
            author = accountByLogin.getValue("account3")
            accounts.add(accountByLogin.getValue("account3"))
        }

        // --- reverse linking
        accountByLogin.getValue("account1").issues.add(issueByGid.getValue("1abc"))
        accountByLogin.getValue("account2").issues.add(issueByGid.getValue("1abc"))
        accountByLogin.getValue("account2").issues.add(issueByGid.getValue("2abc"))
        accountByLogin.getValue("account1").issues.add(issueByGid.getValue("2abc"))
        accountByLogin.getValue("account3").issues.add(issueByGid.getValue("3abc"))
    }
}
