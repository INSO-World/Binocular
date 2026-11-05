package com.inso_world.binocular.indexer.vcs.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * Configuration that activates the JGit based GitIndexer implementation.
 *
 * This profile scans the JGit module for Spring components including the JGitGitIndexer service.
 */
@Configuration
@Profile("jgit")
@ComponentScan("com.inso_world.binocular.jgit")
internal open class JGitTestConfig
