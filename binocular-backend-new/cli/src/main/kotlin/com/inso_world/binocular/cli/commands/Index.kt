package com.inso_world.binocular.cli.commands

import com.inso_world.binocular.cli.service.ProjectService
import com.inso_world.binocular.cli.service.VcsService
import com.inso_world.binocular.ffi.BinocularFfi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.command.annotation.Command
import org.springframework.shell.command.annotation.Option

@Command(
    command = ["index"],
    group = "Index Commands",
    description = "Commands for indexing repository and related data sources",
)
open class Index(
    @Autowired private val vcsService: VcsService,
    @Autowired private val projectService: ProjectService,
) {
    private var logger: Logger = LoggerFactory.getLogger(Index::class.java)

    @Command(command = ["hello"], description = "Hello World")
    fun helloWorld() {
        BinocularFfi().hello()
    }

    @Command(command = ["commits"])
    open fun commits(
        @Option(longNames = ["repo_path"], required = false) repoPath: String?,
        @Option(
            longNames = ["branch"],
            shortNames = ['b'],
            required = true,
        ) branchName: String,
        @Option(
            longNames = ["project_name"],
            shortNames = ['n'],
            required = true,
            description = "Custom name of the project.",
        ) projectName: String,
    ) {
        logger.trace(">>> index($repoPath, $branchName)")
        logger.debug("Project '$projectName'")
        val project = this.projectService.getOrCreateProject(projectName)
        vcsService.indexRepository(repoPath, branchName, project)
        logger.trace("<<< index($repoPath, $branchName)")
    }

    @Command(command = ["diffs"])
    fun diffs(
        @Option(longNames = ["batch"], defaultValue = "1000") batch: String,
    ) {
        logger.info("Calculating diffs...")
        val batchSize = batch.toInt()
        var i = 1
        TODO()
//    do {
//      val hashes = commitService.findAll(i++, batchSize).map { it.sha!! }.stream().collect(Collectors.toList());
//
//      diffs(
//        this.repository,
//        commitlist = hashes,
//        maxThreads = 12u,
//        skipMerges = false,
//        diffAlgorithm = GixDiffAlgorithm.HISTOGRAM,
//        breadthFirst = false,
//        follow = false,
//      )
//
//    } while (hashes.count() == batchSize)
    }

    @Command(command = ["blames"])
    fun blames(
        @Option(longNames = ["batch"], defaultValue = "1000") batch: String,
    ) {
        logger.info("Calculating blames...")
        val batchSize = batch.toInt()
        var i = 1
//    val connections = commitCommitConnectionService.findAll();
//    connections.forEach {
//      println(it)
//    }
//    hashes.forEach {
//      it.parents.forEach { p -> p. }
//    }
    }

//  //  @Command(command = ["rust"])
//  fun rust() {
//    println("++++++++++ cartography ++++++++++")
//
//    val repo = findRepo();
//
//    println("p = $repo")
//
//    val cmt = findCommit(repo, "HEAD")
//    println("cmt = $cmt")
//
//    println("++++++++++ cartography ++++++++++")
//
//    val (values, traverseTime) = measureTimedValue {
//      traverse(repo, cmt, null)
//    };
//    println("CMD traverse took $traverseTime")
//
//    val hashes = values.map { it.commit }
//
//    val (diffs, diffsTime) = measureTimedValue {
//      diffs(
//        repo,
//        commitlist = hashes,
//        maxThreads = 12u,
//        skipMerges = false,
//        diffAlgorithm = GixDiffAlgorithm.HISTOGRAM,
//        breadthFirst = false,
//        follow = false,
//      )
//    };
//    println("CMD diffs took $diffsTime")
//
//    println("#diffs = ${diffs.count()}")
//
//    val groups = diffs.groupingBy { it.commit }
//      .fold(listOf<String>()) { acc, e -> acc + e.changeMap.keys }
//
//    val (blames, timeTaken) = measureTimedValue {
//      blames(
//        repo,
//        groups,
//        maxThreads = 12u,
//        diffAlgorithm = GixDiffAlgorithm.HISTOGRAM
//      )
//    }
//    println("CMD blames took $timeTaken")
//
//
//    println("#blames = ${blames.count()}")
//    println("#blames = ${blames.sumOf { it.blames.count() }}")
//    println("#blames = ${blames.sumOf { it -> it.blames.sumOf { it.entries.count() } }}")
//  }
}
