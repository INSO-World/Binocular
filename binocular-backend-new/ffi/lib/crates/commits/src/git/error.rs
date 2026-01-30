use thiserror::Error;

/// Error types for commit lookup operations.
///
/// Provides detailed error information for different failure scenarios
/// when looking up commits in a Git repository.
#[derive(Debug, Error)]
pub enum CommitLookupError {
    /// The provided commit hash could not be parsed as a valid revision.
    ///
    /// This can happen when:
    /// - The hash is malformed (not a valid hex string)
    /// - The hash length is incorrect
    /// - The revision spec syntax is invalid
    #[error("Failed to parse revision '{hash}': {source}")]
    RevisionParseError {
        hash: String,
        #[source]
        source: gix::revision::spec::parse::single::Error,
    },

    /// The parsed revision could not be resolved to an object in the repository.
    ///
    /// This typically occurs when the hash is syntactically valid but
    /// doesn't correspond to any object in the repository's object database.
    #[error("Object not found for revision '{hash}': {source}")]
    ObjectNotFound {
        hash: String,
        #[source]
        source: gix::object::find::existing::Error,
    },

    /// The object exists but could not be converted to a commit.
    ///
    /// This happens when the object ID points to a non-commit object
    /// (e.g., a tree, blob, or tag that doesn't dereference to a commit).
    #[error("Object '{hash}' is not a commit: {source}")]
    NotACommit {
        hash: String,
        #[source]
        source: gix::object::try_into::Error,
    },

    /// Failed to read commit author information.
    #[error("Failed to read author information from commit '{hash}': {message}")]
    AuthorReadError { hash: String, message: String },

    /// Failed to read commit committer information.
    #[error("Failed to read committer information from commit '{hash}': {message}")]
    CommitterReadError { hash: String, message: String },
}

impl CommitLookupError {
    /// Returns the hash that caused this error, if available.
    pub fn hash(&self) -> &str {
        match self {
            CommitLookupError::RevisionParseError { hash, .. } => hash,
            CommitLookupError::ObjectNotFound { hash, .. } => hash,
            CommitLookupError::NotACommit { hash, .. } => hash,
            CommitLookupError::AuthorReadError { hash, .. } => hash,
            CommitLookupError::CommitterReadError { hash, .. } => hash,
        }
    }

    /// Returns true if this error indicates the commit does not exist.
    pub fn is_not_found(&self) -> bool {
        matches!(
            self,
            CommitLookupError::RevisionParseError { .. } | CommitLookupError::ObjectNotFound { .. }
        )
    }
}
