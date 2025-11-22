use crate::types::branch::GixBranch;
use crate::types::commit::GixCommit;
use crate::types::repo::GixRepository;
use crate::types::result::BranchTraversalResult;
use crate::types::UniffiError;
use gix::ThreadSafeRepository;

/// Traverses a specific branch and returns its commits
///
/// # Arguments
/// * `gix_repo` - The repository containing the branch
/// * `branch` - The name of the branch to traverse
///
/// # Returns
/// A `BranchTraversalResult` containing the branch metadata and all commits
///
/// # Errors
/// - `TraversalError` if the traversal fails or returns unexpected results
/// - `OperationFailed` for other traversal errors
#[uniffi::export]
pub fn traverse_branch(
    gix_repo: GixRepository,
    branch: String,
) -> Result<BranchTraversalResult, UniffiError> {
    let binding = ThreadSafeRepository::try_from(gix_repo)?.to_thread_local();

    let reference = binding
        .find_reference(branch.as_str())
        .map_err(|e| UniffiError::ReferenceError(format!("Failed to get references: {}", e)))?
        .detach();

    match commits::traversal::main(binding, vec![reference], false) {
        Ok(r) => {
            // If you really expect exactly one:
            if r.len() != 1 {
                return Err(UniffiError::TraversalError(format!(
                    "expected 1 result, got {} for branch {:?}",
                    r.len(),
                    branch
                )));
            }

            let result = r
                .into_iter() // take ownership of items
                .map(|(reference, commits)| BranchTraversalResult {
                    branch: GixBranch::from(reference),
                    commits: commits.into_iter().map(|c| GixCommit::from(c)).collect(),
                })
                .next()
                .expect("len() checked above");

            Ok(result)
        }
        Err(e) => Err(UniffiError::TraversalError(e.to_string())),
    }
}
