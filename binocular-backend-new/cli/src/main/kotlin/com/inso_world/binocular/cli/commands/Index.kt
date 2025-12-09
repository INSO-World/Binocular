package com.inso_world.binocular.cli.commands

import com.inso_world.binocular.cli.service.ProjectService
import com.inso_world.binocular.cli.service.RepositoryService
import com.inso_world.binocular.cli.service.VcsService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.command.annotation.Command
import org.springframework.shell.command.annotation.Option
import java.nio.file.Paths

@Command(
    command = ["index"],
    group = "Index Commands",
    description = "Commands for indexing repository and related data sources",
)
open class Index(
    @Autowired private val vcsService: VcsService,
    @Autowired private val repositoryService: RepositoryService,
    @Autowired private val projectService: ProjectService,
) {
    companion object {
        private var logger: Logger = LoggerFactory.getLogger(Index::class.java)
    }

    @Command(command = ["hello"], description = "Hello World")
    fun helloWorld() {
//        BinocularFfi().hello()
    }

    @Command(command = ["commits"])
    open fun commits(
        repoPath: String,
        @Option(
            longNames = ["branch"],
            shortNames = ['b'],
            required = true,
        ) @NotNull @NotEmpty branchName: String,
        @Option(
            longNames = ["project_name"],
            shortNames = ['n'],
            required = true,
            description = "Custom name of the project.",
        ) @NotNull @NotEmpty projectName: String,
    ) {
        val path = repoPath.let { Paths.get(it).toRealPath() }
        logger.trace(">>> index($path, $branchName)")
        logger.debug("Project '$projectName'")
        val project = this.projectService.getOrCreateProject(projectName)
        vcsService.indexRepository(path.toString(), branchName, project)
        logger.trace("<<< index($path, $branchName)")
    }

        @Option(
            longNames = ["project_name"],
            shortNames = ['n'],
            required = true,
            description = "Custom name of the project.",
        ) projectName: String,
    ) {
        val path = repoPath?.let { Paths.get(it).toRealPath() }
        logger.trace(">>> index($path, $branchName)")
        logger.debug("Project '$projectName'")
        val project = this.projectService.getOrCreateProject(projectName)
        vcsService.indexRepository(path.toString(), branchName, project)
        logger.trace("<<< index($path, $branchName)")
    }
}
