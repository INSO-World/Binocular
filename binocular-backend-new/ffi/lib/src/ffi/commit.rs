use crate::types::commit::GixCommit;
use crate::types::error::UniffiError;
use crate::types::repo::GixRepository;
use commits::GitCommitMetric;
use gix::{ObjectId, ThreadSafeRepository};
use std::ops::Deref;

/// Finds a specific commit by its hash
///
/// # Arguments
/// * `gix_repo` - The repository to search in
/// * `hash` - The commit hash to find
///
/// # Returns
/// The commit metadata if found
///
/// # Errors
/// - `RevisionParseError` if the hash cannot be parsed
/// - `ObjectError` if the object cannot be found or converted to a commit
#[uniffi::export]
pub fn find_commit(
    gix_repo: GixRepository,
    hash: String,
) -> Result<GixCommit, UniffiError> {
    println!("repo at {:?}", gix_repo);
    let repo = ThreadSafeRepository::try_from(gix_repo)?;

    let binding = repo.to_thread_local();
    let commit = binding
        .rev_parse_single(hash.deref())?
        .object()?
        .try_into_commit()?;

    Ok(GixCommit::from(GitCommitMetric::from(commit)))
}

/// Traverses commit history from a source commit to an optional target commit
///
/// # Arguments
/// * `gix_repo` - The repository to traverse
/// * `source_commit` - The starting commit
/// * `target_commit` - Optional ending commit. If None, traverses to repository root
///
/// # Returns
/// A vector of commits between source and target
///
/// # Errors
/// - `GixDiscoverError` if repository discovery fails
/// - `CommitLookupError` if commits cannot be found
/// - `GixError` for other traversal errors
#[uniffi::export]
pub fn traverse_history(
    gix_repo: &GixRepository,
    source_commit: ObjectId,
    target_commit: Option<ObjectId>,
) -> Result<Vec<GixCommit>, UniffiError> {
    let repo = ThreadSafeRepository::discover(&gix_repo.git_dir)?;

    let binding = repo.to_thread_local();
    let cmt = binding.find_commit(source_commit)?;
    let trgt = match target_commit {
        None => None,
        Some(c) => Option::from(binding.find_commit(c)?),
    };

    let result = commits::traversal::traverse_from_to(&binding, &cmt, &trgt)?;

    Ok(result.into_iter().map(|c| GixCommit::from(c)).collect())
}
