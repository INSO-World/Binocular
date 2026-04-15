use log::trace;

mod git;
pub(crate) mod objects;

pub mod types {
    pub use crate::objects::blame_result::BlameResult;
    pub use crate::git::objects::BlameOutcome;
}

pub fn process(
    repo: &gix::Repository,
    defines: gix::hashtable::HashMap<gix::ObjectId, Vec<String>>,
    diff_algorithm: Option<gix::diff::blob::Algorithm>,
    max_threads: usize,
) -> anyhow::Result<Vec<types::BlameResult>> {
    let _span = gix::trace::coarse!(
        "process(repo={:?},#defines={},algo={:?},threads={})",
        repo,
        defines.len(),
        diff_algorithm,
        max_threads
    );
    trace!(
        "process(repo={:?},#defines={},algo={:?},threads={})",
        repo,
        defines.len(),
        diff_algorithm,
        max_threads
    );
    crate::git::blame::process(repo, defines, diff_algorithm, max_threads)
}
