package com.inso_world.binocular.cli.commands

import com.inso_world.binocular.cli.BinocularCliConfiguration
import com.inso_world.binocular.cli.uniffi.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.command.annotation.Command
import kotlin.time.measureTimedValue

@Command(command = ["index"])
class Index(
  @Autowired val binocularCliConfiguration: BinocularCliConfiguration,
) {
  private var logger: Logger = LoggerFactory.getLogger(Index::class.java)
  private var repository = findRepo();

  private fun findRepo(): ThreadSafeRepository {
    logger.trace("Searching repository... at '${binocularCliConfiguration.index.path}'")
    val (p, findRepoTime) = measureTimedValue {
      findRepo(binocularCliConfiguration.index.path)
    };
    logger.debug("CMD findRepo took $findRepoTime")

    return p
  }

  @Command(command = ["commits"])
  fun commits() {
    val cmt = findCommit(this.repository, "HEAD")
    println("cmt = $cmt")

    val (values, traverseTime) = measureTimedValue {
      traverse(this.repository, cmt)
    };
    println("CMD traverse took $traverseTime")

    val hashes = values.map { it.commit }
  }

  @Command(command = ["rust"])
  fun rust() {
    println("++++++++++ cartography ++++++++++")

    val repo = findRepo();

    println("p = $repo")

    val cmt = findCommit(repo, "HEAD")
    println("cmt = $cmt")

    println("++++++++++ cartography ++++++++++")

    val (values, traverseTime) = measureTimedValue {
      traverse(repo, cmt)
    };
    println("CMD traverse took $traverseTime")

    val hashes = values.map { it.commit }

    val (diffs, diffsTime) = measureTimedValue {
      diffs(
        repo,
        commitlist = hashes,
        maxThreads = 12u,
        skipMerges = false,
        diffAlgorithm = GixDiffAlgorithm.HISTOGRAM,
        breadthFirst = false,
        follow = false,
      )
    };
    println("CMD diffs took $diffsTime")

    println("#diffs = ${diffs.count()}")

    val groups = diffs.groupingBy { it.commit }
      .fold(listOf<String>()) { acc, e -> acc + e.changeMap.keys }

    val (blames, timeTaken) = measureTimedValue {
      blames(
        repo,
        groups,
        maxThreads = 12u,
        diffAlgorithm = GixDiffAlgorithm.HISTOGRAM
      )
    }
    println("CMD blames took $timeTaken")


    println("#blames = ${blames.count()}")
    println("#blames = ${blames.sumOf { it.blames.count() }}")
    println("#blames = ${blames.sumOf { it -> it.blames.sumOf { it.entries.count() } }}")
  }
}
