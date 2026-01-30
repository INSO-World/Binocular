use crate::git::error::CommitLookupError;
use crate::git::utils::apply_mailmap;
use crate::GitCommitMetric;
use std::ops::Deref;

/// Finds a commit by its hash and returns its metadata.
///
/// # Arguments
/// * `repo` - The Git repository to search in
/// * `hash` - The commit hash (full or abbreviated) or any valid revision spec
/// * `use_mailmap` - Whether to apply mailmap transformations to author/committer info
///
/// # Returns
/// A `GitCommitMetric` containing the commit's metadata on success.
///
/// # Errors
/// Returns `CommitLookupError` in the following cases:
/// - `RevisionParseError`: The hash/revision spec is invalid or malformed
/// - `ObjectNotFound`: No object exists with the given hash
/// - `NotACommit`: The object exists but is not a commit
///
/// # Example
/// ```ignore
/// let repo = gix::discover(".")?;
/// let commit = find_commit(&repo, "HEAD".to_string(), true)?;
/// println!("Author: {}", commit.author.name);
/// ```
pub fn find_commit(
    repo: &gix::Repository,
    hash: String,
    use_mailmap: bool,
) -> Result<GitCommitMetric, CommitLookupError> {
    log::debug!("find_commit(..., {:?})", hash);

    let mailmap = if use_mailmap {
        Some(repo.open_mailmap())
    } else {
        None
    };

    // Parse the revision spec
    let object_id = repo
        .rev_parse_single(hash.deref())
        .map_err(|e| CommitLookupError::RevisionParseError {
            hash: hash.clone(),
            source: e,
        })?;

    // Get the object from the repository
    let object = object_id
        .object()
        .map_err(|e| CommitLookupError::ObjectNotFound {
            hash: hash.clone(),
            source: e,
        })?;

    // Convert to a commit
    let commit = object
        .try_into_commit()
        .map_err(|e| CommitLookupError::NotACommit {
            hash: hash.clone(),
            source: e,
        })?;

    let mut gcm = GitCommitMetric::from(commit.clone());

    // Apply mailmap transformations if enabled
    if let Some(ref mailmap) = mailmap {
        if let Some(mailmap_committer) = apply_mailmap(commit.committer(), mailmap) {
            gcm.committer = mailmap_committer;
        }

        if let Some(mailmap_author) = apply_mailmap(commit.author(), mailmap) {
            gcm.author = mailmap_author;
        }
    }

    Ok(gcm)
}