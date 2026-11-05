package com.inso_world.binocular.indexer.vcs.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * Configuration that activates the FFI (gix) based GitIndexer implementation.
 *
 * This profile scans the FFI module for Spring components including the GixIndexer service.
 */
@Configuration
@Profile("gix")
@ComponentScan("com.inso_world.binocular.ffi")
internal open class GixTestConfig
