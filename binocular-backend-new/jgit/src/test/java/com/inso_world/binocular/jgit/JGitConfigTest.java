package com.inso_world.binocular.jgit;

import com.inso_world.binocular.core.config.VcsConfig;
import com.inso_world.binocular.core.unit.base.BaseUnitTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link JGitConfig} and {@link VcsConfig}.
 * Tests all getters and setters to ensure full mutation coverage.
 */
class JGitConfigTest extends BaseUnitTest {

    @Nested
    class JGitConfigOperations {

        @Test
        void getJgit_returnsDefaultSettings() {
            JGitConfig config = new JGitConfig();

            VcsConfig settings = config.getVcs();

            assertNotNull(settings, "Default VcsConfig should not be null");
        }

        @Test
        void setJgit_updatesSettings() {
            JGitConfig config = new JGitConfig();
            VcsConfig newSettings = new VcsConfig();
            newSettings.setSkipMerges(true);
            newSettings.setUseMailmap(true);

            config.setVcs(newSettings);

            assertSame(newSettings, config.getVcs(), "getVcs should return the settings set by getVcs");
        }
    }

    @Nested
    class JGitSettingsOperations {

        @Nested
        class SkipMergesProperty {

            @Test
            void isSkipMerges_defaultsToFalse() {
                VcsConfig settings = new VcsConfig();

                assertFalse(settings.getSkipMerges(), "skipMerges should default to false");
            }

            @Test
            void isSkipMerges_returnsTrueWhenSetToTrue() {
                VcsConfig settings = new VcsConfig();

                settings.setSkipMerges(true);

                assertTrue(settings.getSkipMerges(), "isSkipMerges should return true after setting to true");
            }

            @Test
            void isSkipMerges_returnsFalseWhenSetToFalse() {
                VcsConfig settings = new VcsConfig();
                settings.setSkipMerges(true); // First set to true

                settings.setSkipMerges(false); // Then set back to false

                assertFalse(settings.getSkipMerges(), "isSkipMerges should return false after setting to false");
            }

            @Test
            void setSkipMerges_modifiesValue() {
                VcsConfig settings = new VcsConfig();

                settings.setSkipMerges(true);
                boolean afterSetTrue = settings.getSkipMerges();

                settings.setSkipMerges(false);
                boolean afterSetFalse = settings.getSkipMerges();

                assertAll(
                        () -> assertTrue(afterSetTrue, "Should be true after setSkipMerges(true)"),
                        () -> assertFalse(afterSetFalse, "Should be false after setSkipMerges(false)")
                );
            }
        }

        @Nested
        class UseMailmapProperty {

            @Test
            void isUseMailmap_defaultsToTrue() {
                VcsConfig settings = new VcsConfig();

                assertTrue(settings.getUseMailmap(), "useMailmap should default to true");
            }

            @Test
            void isUseMailmap_returnsTrueWhenSetToTrue() {
                VcsConfig settings = new VcsConfig();

                settings.setUseMailmap(true);

                assertTrue(settings.getUseMailmap(), "isUseMailmap should return true after setting to true");
            }

            @Test
            void isUseMailmap_returnsFalseWhenSetToFalse() {
                VcsConfig settings = new VcsConfig();
                settings.setUseMailmap(true); // First set to true

                settings.setUseMailmap(false); // Then set back to false

                assertFalse(settings.getUseMailmap(), "isUseMailmap should return false after setting to false");
            }

            @Test
            void setUseMailmap_modifiesValue() {
                VcsConfig settings = new VcsConfig();

                settings.setUseMailmap(true);
                boolean afterSetTrue = settings.getUseMailmap();

                settings.setUseMailmap(false);
                boolean afterSetFalse = settings.getUseMailmap();

                assertAll(
                        () -> assertTrue(afterSetTrue, "Should be true after setUseMailmap(true)"),
                        () -> assertFalse(afterSetFalse, "Should be false after setUseMailmap(false)")
                );
            }
        }

        @Nested
        class CombinedSettings {

            @Test
            void bothSettings_canBeSetIndependently() {
                VcsConfig settings = new VcsConfig();

                settings.setSkipMerges(true);
                settings.setUseMailmap(false);

                assertAll(
                        () -> assertTrue(settings.getSkipMerges(), "skipMerges should be true"),
                        () -> assertFalse(settings.getUseMailmap(), "useMailmap should be false")
                );
            }

            @Test
            void bothSettings_canBeTrueSimultaneously() {
                VcsConfig settings = new VcsConfig();

                settings.setSkipMerges(true);
                settings.setUseMailmap(true);

                assertAll(
                        () -> assertTrue(settings.getSkipMerges(), "skipMerges should be true"),
                        () -> assertTrue(settings.getUseMailmap(), "useMailmap should be true")
                );
            }
        }
    }

    @Nested
    class FullConfigIntegration {

        @Test
        void config_accessesNestedSettings() {
            JGitConfig config = new JGitConfig();

            config.getVcs().setSkipMerges(true);
            config.getVcs().setUseMailmap(true);

            assertAll(
                    () -> assertTrue(config.getVcs().getSkipMerges(), "skipMerges should be accessible through config"),
                    () -> assertTrue(config.getVcs().getUseMailmap(), "useMailmap should be accessible through config")
            );
        }
    }
}
