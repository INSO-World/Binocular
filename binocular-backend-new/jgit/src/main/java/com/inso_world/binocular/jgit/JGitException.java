package com.inso_world.binocular.jgit;

/**
 * Exception hierarchy for JGit operations.
 * Mirrors the structure of UniffiException from the FFI module
 * to enable unified error handling in tests.
 */
public abstract class JGitException extends RuntimeException {

    protected JGitException(String message) {
        super(message);
    }

    protected JGitException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Thrown when a Git repository cannot be discovered or opened.
     * Equivalent to UniffiException.GixDiscoverException.
     */
    public static class DiscoverException extends JGitException {
        public DiscoverException(String message) {
            super(message);
        }

        public DiscoverException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Thrown when a Git reference (branch, tag, commit) cannot be resolved.
     * Equivalent to UniffiException.ReferenceException.
     */
    public static class ReferenceException extends JGitException {
        public ReferenceException(String message) {
            super(message);
        }

        public ReferenceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Thrown when a traversal operation fails.
     * Equivalent to UniffiException.TraversalException.
     */
    public static class TraversalException extends JGitException {
        public TraversalException(String message) {
            super(message);
        }

        public TraversalException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Thrown when an operation fails.
     * Equivalent to UniffiException.OperationFailed.
     */
    public static class OperationFailedException extends JGitException {
        public OperationFailedException(String message) {
            super(message);
        }

        public OperationFailedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
