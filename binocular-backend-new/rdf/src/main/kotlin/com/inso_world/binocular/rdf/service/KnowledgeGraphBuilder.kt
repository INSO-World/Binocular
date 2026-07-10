package com.inso_world.binocular.rdf.service

import com.inso_world.binocular.rdf.arango.ArangoReader
import com.inso_world.binocular.rdf.mapping.*
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.Lang
import org.apache.jena.riot.RDFDataMgr
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.FileOutputStream

@Service
class KnowledgeGraphBuilder(
    private val arangoReader: ArangoReader,
    @Value("\${binocular.rdf.output.path:./output/knowledge-graph.ttl}") val outputPath: String,
    private val commitMapper: CommitMapper = CommitMapper(),
    private val contributorMapper: ContributorMapper = ContributorMapper(),
    private val fileMapper: FileMapper = FileMapper(),
    private val moduleMapper: ModuleMapper = ModuleMapper(),
    private val issueMapper: IssueMapper = IssueMapper(),
    private val mergeRequestMapper: MergeRequestMapper = MergeRequestMapper(),
    private val buildMapper: BuildMapper = BuildMapper(),
    private val edgeMapper: EdgeMapper = EdgeMapper()
) {
    private val logger = LoggerFactory.getLogger(KnowledgeGraphBuilder::class.java)

    fun buildAndExport() {
        logger.info("Starting knowledge graph build...")

        val model = ModelFactory.createDefaultModel()

        // Register namespaces
        model.setNsPrefix("bio", RdfNamespaces.BIO)
        model.setNsPrefix("vcs-git", RdfNamespaces.VCS_GIT)
        model.setNsPrefix("its-gh", RdfNamespaces.ITS_GH)
        model.setNsPrefix("ci-gha", RdfNamespaces.CI_GHA)
        model.setNsPrefix("inst", RdfNamespaces.INST)

        try {
            // Read all data from ArangoDB
            logger.info("Reading commits...")
            val commits = arangoReader.readCommits()
            logger.info("Read {} commits", commits.size)

            logger.info("Reading entities...")
            val users = arangoReader.readUsers()
            val files = arangoReader.readFiles()
            val modules = arangoReader.readModules()
            val issues = arangoReader.readIssues()
            val mergeRequests = arangoReader.readMergeRequests()
            val builds = arangoReader.readBuilds()
            val branches = arangoReader.readBranches()

            logger.info(
                "Read {} users, {} files, {} modules, {} issues, {} MRs, {} builds, {} branches",
                users.size,
                files.size,
                modules.size,
                issues.size,
                mergeRequests.size,
                builds.size,
                branches.size
            )

            // Map entity collections to RDF
            logger.info("Mapping entities...")
            commitMapper.map(commits, model)
            contributorMapper.map(users, model)
            fileMapper.map(files, model)
            moduleMapper.map(modules, model)
            issueMapper.map(issues, model)
            mergeRequestMapper.map(mergeRequests, model)
            buildMapper.map(builds, model)

            // Build SHA → URI lookup for branches
            val commitShaToUri =
                commits
                    .associate { commit ->
                        (commit["sha"] as? String) to "${RdfNamespaces.INST}commit/${commit["_key"]}"
                    }.filterKeys { it != null } as Map<String, String>

            val branchMapper = BranchMapper(commitShaToUri)
            branchMapper.map(branches, model)

            // Map edges
            logger.info("Reading and mapping edge collections...")
            val commitUserEdges = arangoReader.readCommitsUsers()
            edgeMapper.mapCommitsUsers(commitUserEdges, model)

            val commitFileEdges = arangoReader.readCommitsFiles()
            edgeMapper.mapCommitsFiles(commitFileEdges, model)

            val issueCommitEdges = arangoReader.readIssuesCommits()
            edgeMapper.mapIssuesCommits(issueCommitEdges, model)

            val commitModuleEdges = arangoReader.readCommitsModules()
            edgeMapper.mapCommitsModules(commitModuleEdges, model)

            val commitBuildEdges = arangoReader.readCommitsBuilds()
            edgeMapper.mapCommitsBuilds(commitBuildEdges, model)

            val moduleModuleEdges = arangoReader.readModulesModules()
            edgeMapper.mapModulesModules(moduleModuleEdges, model)

            // Write to file
            logger.info("Writing Turtle to {}", outputPath)
            val output = FileOutputStream(outputPath)
            RDFDataMgr.write(output, model, Lang.TURTLE)
            output.close()

            logger.info("Knowledge graph built successfully with {} triples", model.size())
        } catch (e: Exception) {
            logger.error("Error building knowledge graph", e)
            throw e
        }
    }
}
