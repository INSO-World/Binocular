package com.inso_world.binocular.model.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.net.URI

/**
 * Validator for [GitUrl] annotation.
 *
 * Validates Git repository URLs by checking against common Git URL patterns:
 *
 * 1. **HTTP/HTTPS URLs:** Standard web URLs (e.g., `https://github.com/user/repo.git`)
 * 2. **SSH URLs:** SSH protocol URLs (e.g., `ssh://git@github.com/user/repo.git`)
 * 3. **Git protocol URLs:** Git-specific protocol (e.g., `git://github.com/user/repo.git`)
 * 4. **SCP-like syntax:** SSH shorthand (e.g., `git@github.com:user/repo.git`)
 * 5. **File URLs:** Local file paths (e.g., `file:///path/to/repo.git`)
 * 6. **Absolute paths:** Unix/Windows absolute paths (e.g., `/path/to/repo`, `C:\path\to\repo`)
 * 7. **Relative paths:** Relative paths (e.g., `../repo`, `./repo`)
 *
 * ### Validation logic
 * - Null or blank values are considered invalid (use `@NotBlank` separately if needed)
 * - Attempts to parse as URI/URL for standard protocols
 * - Falls back to regex pattern matching for SCP-like syntax and paths
 *
 * ### Implementation notes
 * - Thread-safe and stateless
 * - Lenient parsing to accommodate various Git configurations
 * - Does not perform network validation (URL existence/accessibility)
 *
 * @see GitUrl
 */
class GitUrlValidator : ConstraintValidator<GitUrl, String> {

    companion object {
        /**
         * Regex pattern for SCP-like SSH URLs (e.g., `git@github.com:user/repo.git`).
         *
         * Format: `[user@]host:path`
         * - Optional user (e.g., `git@`)
         * - Required host (domain or IP)
         * - Colon separator
         * - Path to repository
         */
        private val SCP_PATTERN = Regex(
            """^(?:[\w.-]+@)?[\w.-]+:[\w./_-]+$"""
        )

        /**
         * Regex pattern for local file paths (absolute and relative).
         *
         * Covers:
         * - Unix absolute: `/path/to/repo`
         * - Unix relative: `../repo`, `./repo`, `relative/path`
         * - Windows absolute: `C:\path\to\repo`
         * - Windows UNC: `\\server\share\repo`
         */
        private val PATH_PATTERN = Regex(
            """^(?:[a-zA-Z]:[\\/]|/|\\\\|\.\.?/|[\w-]+/).*$"""
        )

        /**
         * Supported Git URL schemes.
         */
        private val VALID_SCHEMES = setOf(
            "http", "https", "ssh", "git", "file", "ftp", "ftps"
        )
    }

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        // Null or blank is invalid (use @NotBlank separately)
        if (value.isNullOrBlank()) {
            return false
        }

        val trimmedValue = value.trim()

        // URLs should not contain spaces
        if (trimmedValue.contains(' ')) {
            return false
        }

        // If value looks like a URL with scheme (contains "://"), it must be valid
        // Reject malformed URLs that have scheme-like patterns but are invalid
        if (trimmedValue.contains("://")) {
            try {
                val uri = URI(trimmedValue)
                val scheme = uri.scheme?.lowercase()

                // If has a scheme, validate it's a Git-compatible protocol
                if (scheme != null) {
                    if (!VALID_SCHEMES.contains(scheme)) {
                        return false
                    }

                    // Validate scheme-specific part is not blank
                    if (uri.schemeSpecificPart.isBlank()) {
                        return false
                    }

                    // For network protocols (http, https, ssh, git, ftp, ftps), require a valid host
                    if (scheme in setOf("http", "https", "ssh", "git", "ftp", "ftps")) {
                        val host = uri.host
                        // Host must exist and not be blank
                        if (host.isNullOrBlank()) {
                            return false
                        }
                    }

                    return true
                }
                // Has "://" but no valid scheme - invalid
                return false
            } catch (e: Exception) {
                // Looks like a URL but failed to parse - invalid
                return false
            }
        }

        // Try parsing as URI without scheme (for Windows paths like C:\path)
        try {
            val uri = URI(trimmedValue)
            val scheme = uri.scheme?.lowercase()

            if (scheme != null && VALID_SCHEMES.contains(scheme)) {
                // Already handled above
                return true
            }
        } catch (e: Exception) {
            // Not a valid URI, continue to pattern matching
        }

        // Check for SCP-like SSH syntax (git@host:path)
        if (SCP_PATTERN.matches(trimmedValue)) {
            return true
        }

        // Check for local paths (absolute or relative)
        if (PATH_PATTERN.matches(trimmedValue)) {
            return true
        }

        // If none of the above match, it's invalid
        return false
    }
}