/// Main error type for FFI operations
#[derive(Debug, thiserror::Error, uniffi::Error)]
pub enum UniffiError {
    #[error("Invalid input: {0}")]
    InvalidInput(String),

    #[error("Operation failed: {0}")]
    OperationFailed(String),

    #[error("Branch traversal failed: {0}")]
    TraversalError(String),

    #[error("Git repository discovery failed: {0}")]
    GixDiscoverError(String),

    #[error("Commit lookup failed: {0}")]
    CommitLookupError(String),

    #[error("Reference operation failed: {0}")]
    ReferenceError(String),

    #[error("Object operation failed: {0}")]
    ObjectError(String),

    #[error("Revision parsing failed: {0}")]
    RevisionParseError(String),

    #[error("Git operation failed: {0}")]
    GixError(String),
}

// Automatic error conversions from gix error types
impl From<gix::discover::Error> for UniffiError {
    fn from(err: gix::discover::Error) -> Self {
        UniffiError::GixDiscoverError(err.to_string())
    }
}

impl From<gix::reference::find::existing::Error> for UniffiError {
    fn from(err: gix::reference::find::existing::Error) -> Self {
        UniffiError::ReferenceError(err.to_string())
    }
}

impl From<gix::object::find::existing::Error> for UniffiError {
    fn from(err: gix::object::find::existing::Error) -> Self {
        UniffiError::ObjectError(err.to_string())
    }
}

impl From<gix::revision::spec::parse::single::Error> for UniffiError {
    fn from(err: gix::revision::spec::parse::single::Error) -> Self {
        UniffiError::RevisionParseError(err.to_string())
    }
}

impl From<gix::object::commit::Error> for UniffiError {
    fn from(err: gix::object::commit::Error) -> Self {
        UniffiError::CommitLookupError(err.to_string())
    }
}

impl From<gix::object::try_into::Error> for UniffiError {
    fn from(err: gix::object::try_into::Error) -> Self {
        UniffiError::ObjectError(err.to_string())
    }
}

impl From<gix::object::find::existing::with_conversion::Error> for UniffiError {
    fn from(value: gix::object::find::existing::with_conversion::Error) -> Self {
        UniffiError::CommitLookupError(value.to_string())
    }
}

impl From<anyhow::Error> for UniffiError {
    fn from(err: anyhow::Error) -> Self {
        UniffiError::GixError(err.to_string())
    }
}

impl From<commits::CommitLookupError> for UniffiError {
    fn from(err: commits::CommitLookupError) -> Self {
        match &err {
            commits::CommitLookupError::RevisionParseError { .. } => {
                UniffiError::RevisionParseError(err.to_string())
            }
            commits::CommitLookupError::ObjectNotFound { .. } => {
                UniffiError::ObjectError(err.to_string())
            }
            commits::CommitLookupError::NotACommit { .. } => {
                UniffiError::ObjectError(err.to_string())
            }
            commits::CommitLookupError::AuthorReadError { .. }
            | commits::CommitLookupError::CommitterReadError { .. } => {
                UniffiError::CommitLookupError(err.to_string())
            }
        }
    }
}

// Legacy error type - kept for compatibility
#[derive(Debug, uniffi::Object, thiserror::Error)]
#[uniffi::export(Debug, Display)]
pub struct ProcErrorInterface {
    pub e: String,
}

#[uniffi::export]
impl ProcErrorInterface {
    fn message(&self) -> String {
        self.e.clone()
    }
}

impl std::fmt::Display for ProcErrorInterface {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "ProcErrorInterface({})", self.e)
    }
}