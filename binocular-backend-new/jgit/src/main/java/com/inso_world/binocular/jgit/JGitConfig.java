package com.inso_world.binocular.jgit;

import com.inso_world.binocular.core.BinocularConfig;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for JGit-based Git operations.
 *
 * <h2>Semantics</h2>
 * Extends {@link BinocularConfig} to bind properties under the {@code binocular.vcs} prefix.
 * Provides configuration options for controlling Git traversal and identity resolution behavior.
 *
 * <h2>Configuration Properties</h2>
 * <ul>
 *   <li>{@code binocular.vcs.skip-merges}: When true, filters out merge commits during branch traversal</li>
 *   <li>{@code binocular.vcs.use-mailmap}: When true, applies .mailmap transformations to author/committer identities</li>
 * </ul>
 *
 * <h2>Example Configuration</h2>
 * <pre>{@code
 * binocular:
 *   vcs:
 *     skip-merges: true
 *     use-mailmap: true
 * }</pre>
 *
 * @see BinocularConfig
 * @see JGitGitIndexer
 */
@Configuration
public class JGitConfig extends BinocularConfig {
}
