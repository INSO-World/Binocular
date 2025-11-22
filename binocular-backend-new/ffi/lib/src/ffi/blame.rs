use crate::types::error::UniffiError;
use crate::types::repo::GixRepository;
use gix::ThreadSafeRepository;
use std::collections::HashMap;

/// Calculates blame information for files in commits
///
/// # Arguments
/// * `gix_repo` - The repository to analyze
/// * `defines` - Map of commit IDs to file paths to blame
/// * `diff_algorithm` - Optional diff algorithm to use
/// * `max_threads` - Maximum number of threads for parallel processing
///
/// # Returns
/// A vector of blame results for all requested files
///
/// # Errors
/// - `GixDiscoverError` if repository discovery fails
/// - `GixError` for blame calculation errors
#[uniffi::export]
pub fn blames(
    gix_repo: GixRepository,
    defines: HashMap<gix::ObjectId, Vec<String>>,
    diff_algorithm: Option<crate::types::diff::GixDiffAlgorithm>,
    max_threads: u8,
) -> Result<Vec<crate::types::blame::GixBlameResult>, UniffiError> {

    use binocular_blame::process;
    use std::time::Instant;

    let binding = ThreadSafeRepository::try_from(gix_repo)?.to_thread_local();

    let mut start = Instant::now();
    let iterable = gix::hashtable::HashMap::from_iter(defines);
    let mut duration = start.elapsed();

    println!("Time elapsed in from_iter() is: {:?}", duration);

    start = Instant::now();
    let result = process(&binding, iterable, diff_algorithm, max_threads as usize)?;
    duration = start.elapsed();
    println!("Time elapsed in process() is: {:?}", duration);

    Ok(result
        .into_iter()
        .map(crate::types::blame::GixBlameResult::from)
        .collect())
}