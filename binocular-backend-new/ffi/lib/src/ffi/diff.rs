use crate::types::diff::GixDiffInput;
use crate::types::error::UniffiError;
use crate::types::repo::GixRepository;
use gix::ThreadSafeRepository;

/// Calculates diffs for multiple commit pairs
///
/// # Arguments
/// * `gix_repo` - The repository to work with
/// * `commit_pairs` - Vector of commit pairs to diff (suspect, target)
/// * `max_threads` - Maximum number of threads to use for parallel processing
/// * `diff_algorithm` - Optional diff algorithm to use
///
/// # Returns
/// A vector of diff results for each commit pair
///
/// # Errors
/// - `GixDiscoverError` if repository discovery fails
/// - `GixError` for diff calculation errors
#[uniffi::export]
pub fn diffs(
    gix_repo: GixRepository,
    commit_pairs: Vec<GixDiffInput>,
    max_threads: u8,
    diff_algorithm: Option<crate::types::diff::GixDiffAlgorithm>,
) -> Result<Vec<crate::types::diff::GixDiff>, UniffiError> {
    log::debug!("diffs: processing {} commit pairs", commit_pairs.len());
    let binding = ThreadSafeRepository::try_from(gix_repo)?.to_thread_local();

    use binocular_diff::calculation::diff_pairs;

    let result = diff_pairs(
        &binding,
        commit_pairs.iter().map(|c| (c.suspect, c.target)).collect(),
        max_threads as usize,
        diff_algorithm,
    )?;

    Ok(result
        .into_iter()
        .map(crate::types::diff::GixDiff::from)
        .collect())
}