package com.inso_world.binocular.jgit;

import com.inso_world.binocular.core.BinocularConfig;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for JGit-based Git operations.
 *
 * <h2>Semantics</h2>
 * Extends {@link BinocularConfig} to bind properties under the {@code binocular.jgit} prefix.
 * Provides configuration options for controlling Git traversal and identity resolution behavior.
 *
 * <h2>Configuration Properties</h2>
 * <ul>
 *   <li>{@code binocular.jgit.skip-merges}: When true, filters out merge commits during branch traversal</li>
 *   <li>{@code binocular.jgit.use-mailmap}: When true, applies .mailmap transformations to author/committer identities</li>
 * </ul>
 *
 * <h2>Example Configuration</h2>
 * <pre>{@code
 * binocular:
 *   jgit:
 *     skip-merges: true
 *     use-mailmap: true
 * }</pre>
 *
 * @see BinocularConfig
 * @see JGitGitIndexer
 */
@Configuration
public class JGitConfig extends BinocularConfig {

    private JGitSettings jgit = new JGitSettings();

    public JGitSettings getJgit() {
        return jgit;
    }

    public void setJgit(JGitSettings jgit) {
        this.jgit = jgit;
    }

    /**
     * Settings specific to JGit Git operations.
     *
     * <h2>Invariants</h2>
     * <ul>
     *   <li>Default values: skipMerges=false, useMailmap=false</li>
     *   <li>Settings are mutable to support Spring property binding</li>
     * </ul>
     */
    public static class JGitSettings {
        private boolean skipMerges = false;
        private boolean useMailmap = false;

        public boolean isSkipMerges() {
            return skipMerges;
        }

        public void setSkipMerges(boolean skipMerges) {
            this.skipMerges = skipMerges;
        }

        public boolean isUseMailmap() {
            return useMailmap;
        }

        public void setUseMailmap(boolean useMailmap) {
            this.useMailmap = useMailmap;
        }
    }
}
