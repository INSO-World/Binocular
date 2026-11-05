use crate::types::commit::GixCommit;
use crate::types::error::UniffiError;
use crate::types::repo::GixRepository;
use gix::{ObjectId, ThreadSafeRepository};

/// Finds a specific commit by its hash
///
/// # Arguments
/// * `gix_repo` - The repository to search in
/// * `hash` - The commit hash to find (full or abbreviated) or any valid revision spec
/// * `use_mailmap` - Whether to apply mailmap transformations to author/committer info
///
/// # Returns
/// The commit metadata if found
///
/// # Errors
/// - `RevisionParseError` if the hash/revision spec is invalid or malformed
/// - `ObjectError` if the object cannot be found or is not a commit
/// - `CommitLookupError` if author/committer information cannot be read
#[uniffi::export]
pub fn find_commit(gix_repo: GixRepository, hash: String, use_mailmap: bool) -> Result<GixCommit, UniffiError> {
    log::debug!("find_commit: repo at {:?}", gix_repo);
    let repo = ThreadSafeRepository::try_from(gix_repo)?;

    let binding = repo.to_thread_local();
    let gcm = commits::find_commit(&binding, hash, use_mailmap)?;

    Ok(GixCommit::from(gcm))
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
    use_mailmap: bool,
) -> Result<Vec<GixCommit>, UniffiError> {
    let repo = ThreadSafeRepository::discover(&gix_repo.git_dir)?;

    let binding = repo.to_thread_local();
    let cmt = binding.find_commit(source_commit)?;
    let trgt = match target_commit {
        None => None,
        Some(c) => Option::from(binding.find_commit(c)?),
    };

    let result = commits::traversal::traverse_from_to(&binding, &cmt, &trgt, use_mailmap)?;

    Ok(result.into_iter().map(|c| GixCommit::from(c)).collect())
}
