// Internal modules
pub(crate) mod types {
    pub(crate) mod blame;
    pub(crate) mod branch;
    pub(crate) mod commit;
    pub(crate) mod diff;
    pub(crate) mod error;
    pub(crate) mod remote;
    pub(crate) mod reference;
    pub(crate) mod repo;
    pub(crate) mod signature;

    // Re-export error types for FFI modules
    pub use error::UniffiError;

    // UniFFI remote type wrappers
    type AnyhowError = anyhow::Error;
    #[uniffi::remote(Object)]
    pub struct AnyhowError;

    type LogLevel = log::Level;
    #[uniffi::remote(Enum)]
    pub enum LogLevel {
        Error = 1,
        Warn = 2,
        Info = 3,
        Debug = 4,
        Trace = 5,
    }

    // Result types
    pub mod result {
        use crate::types::branch::GixBranch;
        use crate::types::commit::GixCommit;

        #[derive(uniffi::Record)]
        pub struct BranchTraversalResult {
            pub(crate) branch: GixBranch,
            pub(crate) commits: Vec<GixCommit>,
        }
    }
}

// FFI exports
pub mod ffi;

// Re-export all FFI functions for use
pub use ffi::blame::blames;
pub use ffi::branch::traverse_branch;
pub use ffi::commit::{find_commit, traverse_history};
pub use ffi::diff::diffs;
pub use ffi::repository::{find_all_branches, find_repo};
pub use ffi::utils::hello;

// Setup UniFFI scaffolding
uniffi::setup_scaffolding!();