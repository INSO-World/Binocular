use crate::types::branch::GixBranch;
use crate::types::error::UniffiError;
use crate::types::remote::GixRemote;
use crate::types::repo::{discover_repo, GixRepository};
use gix::bstr::ByteSlice;
use gix::refs::Kind;
use gix::ThreadSafeRepository;

/// Discovers and opens a Git repository at the given path
///
/// # Arguments
/// * `path` - Path to the Git repository
///
/// # Returns
/// A `GixRepository` containing repository metadata and remotes
///
/// # Errors
/// Returns `GixDiscoverError` if the repository cannot be discovered
#[uniffi::export]
pub fn find_repo(path: String) -> Result<GixRepository, UniffiError> {
    let repo = discover_repo(path)?;

    let binding = repo.to_thread_local();

    let remotes = binding
        .remote_names()
        .iter()
        .map(|r| binding.find_remote(r.as_bstr()))
        .filter_map(Result::ok)
        .map(GixRemote::from)
        .collect::<Vec<_>>();

    Ok(GixRepository {
        git_dir: repo.refs.git_dir().display().to_string(),
        work_tree: repo.work_tree.map(|val| val.display().to_string()),
        remotes,
    })
}

/// Finds all branches (local and remote) in a repository
///
/// # Arguments
/// * `gix_repo` - The repository to query
///
/// # Returns
/// A vector of all branches found in the repository
///
/// # Errors
/// Returns `ReferenceError` if branch enumeration fails
#[uniffi::export]
pub fn find_all_branches(gix_repo: GixRepository) -> Result<Vec<GixBranch>, UniffiError> {
    println!("repo at {:?}", gix_repo);
    let binding = ThreadSafeRepository::try_from(gix_repo)?.to_thread_local();
    
    let references = binding
        .references()
        .map_err(|e| UniffiError::ReferenceError(format!("Failed to get references: {}", e)))?;
    let local_branches = references
        .local_branches()
        .map_err(|e| UniffiError::ReferenceError(format!("Failed to get local branches: {}", e)))?;
    let remote_branches = references.remote_branches().map_err(|e| {
        UniffiError::ReferenceError(format!("Failed to get remote branches: {}", e))
    })?;

    Ok(remote_branches
        .chain(local_branches)
        .filter(Result::is_ok)
        .map(Result::unwrap)
        .map(|b| b.inner)
        .filter(|b| b.kind() == Kind::Object)
        .map(GixBranch::from)
        .collect())
}
