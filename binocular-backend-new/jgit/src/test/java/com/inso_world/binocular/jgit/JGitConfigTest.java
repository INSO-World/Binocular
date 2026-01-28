package com.inso_world.binocular.jgit;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link JGitConfig} and {@link JGitConfig.JGitSettings}.
 * Tests all getters and setters to ensure full mutation coverage.
 */
@Tag("unit")
class JGitConfigTest {

    @Nested
    class JGitConfigOperations {

        @Test
        void getJgit_returnsDefaultSettings() {
            JGitConfig config = new JGitConfig();

            JGitConfig.JGitSettings settings = config.getJgit();

            assertNotNull(settings, "Default JGitSettings should not be null");
        }

        @Test
        void setJgit_updatesSettings() {
            JGitConfig config = new JGitConfig();
            JGitConfig.JGitSettings newSettings = new JGitConfig.JGitSettings();
            newSettings.setSkipMerges(true);
            newSettings.setUseMailmap(true);

            config.setJgit(newSettings);

            assertSame(newSettings, config.getJgit(), "getJgit should return the settings set by setJgit");
        }
    }

    @Nested
    class JGitSettingsOperations {

        @Nested
        class SkipMergesProperty {

            @Test
            void isSkipMerges_defaultsToFalse() {
                JGitConfig.JGitSettings settings = new JGitConfig.JGitSettings();

                assertFalse(settings.isSkipMerges(), "skipMerges should default to false");
            }

            @Test
            void isSkipMerges_returnsTrueWhenSetToTrue() {
                JGitConfig.JGitSettings settings = new JGitConfig.JGitSettings();

                settings.setSkipMerges(true);

                assertTrue(settings.isSkipMerges(), "isSkipMerges should return true after setting to true");
            }

            @Test
            void isSkipMerges_returnsFalseWhenSetToFalse() {
                JGitConfig.JGitSettings settings = new JGitConfig.JGitSettings();
                settings.setSkipMerges(true); // First set to true

                settings.setSkipMerges(false); // Then set back to false

                assertFalse(settings.isSkipMerges(), "isSkipMerges should return false after setting to false");
            }

            @Test
            void setSkipMerges_modifiesValue() {
                JGitConfig.JGitSettings settings = new JGitConfig.JGitSettings();

                settings.setSkipMerges(true);
                boolean afterSetTrue = settings.isSkipMerges();

                settings.setSkipMerges(false);
                boolean afterSetFalse = settings.isSkipMerges();

                assertAll(
                        () -> assertTrue(afterSetTrue, "Should be true after setSkipMerges(true)"),
                        () -> assertFalse(afterSetFalse, "Should be false after setSkipMerges(false)")
                );
            }
        }

        @Nested
        class UseMailmapProperty {

            @Test
            void isUseMailmap_defaultsToFalse() {
                JGitConfig.JGitSettings settings = new JGitConfig.JGitSettings();

                assertFalse(settings.isUseMailmap(), "useMailmap should default to false");
            }

            @Test
            void isUseMailmap_returnsTrueWhenSetToTrue() {
                JGitConfig.JGitSettings settings = new JGitConfig.JGitSettings();

                settings.setUseMailmap(true);

                assertTrue(settings.isUseMailmap(), "isUseMailmap should return true after setting to true");
            }

            @Test
            void isUseMailmap_returnsFalseWhenSetToFalse() {
                JGitConfig.JGitSettings settings = new JGitConfig.JGitSettings();
                settings.setUseMailmap(true); // First set to true

                settings.setUseMailmap(false); // Then set back to false

                assertFalse(settings.isUseMailmap(), "isUseMailmap should return false after setting to false");
            }

            @Test
            void setUseMailmap_modifiesValue() {
                JGitConfig.JGitSettings settings = new JGitConfig.JGitSettings();

                settings.setUseMailmap(true);
                boolean afterSetTrue = settings.isUseMailmap();

                settings.setUseMailmap(false);
                boolean afterSetFalse = settings.isUseMailmap();

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
                JGitConfig.JGitSettings settings = new JGitConfig.JGitSettings();

                settings.setSkipMerges(true);
                settings.setUseMailmap(false);

                assertAll(
                        () -> assertTrue(settings.isSkipMerges(), "skipMerges should be true"),
                        () -> assertFalse(settings.isUseMailmap(), "useMailmap should be false")
                );
            }

            @Test
            void bothSettings_canBeTrueSimultaneously() {
                JGitConfig.JGitSettings settings = new JGitConfig.JGitSettings();

                settings.setSkipMerges(true);
                settings.setUseMailmap(true);

                assertAll(
                        () -> assertTrue(settings.isSkipMerges(), "skipMerges should be true"),
                        () -> assertTrue(settings.isUseMailmap(), "useMailmap should be true")
                );
            }
        }
    }

    @Nested
    class FullConfigIntegration {

        @Test
        void config_accessesNestedSettings() {
            JGitConfig config = new JGitConfig();

            config.getJgit().setSkipMerges(true);
            config.getJgit().setUseMailmap(true);

            assertAll(
                    () -> assertTrue(config.getJgit().isSkipMerges(), "skipMerges should be accessible through config"),
                    () -> assertTrue(config.getJgit().isUseMailmap(), "useMailmap should be accessible through config")
            );
        }
    }
}