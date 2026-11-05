mod objects {
    mod change;
    mod entry;
    mod file_diff;
    mod outcome;

    pub use change::ChangeType;
    pub(crate) use entry::Entry;
    pub use file_diff::FileDiff;
    pub use outcome::GitDiffOutcome;
}

pub use objects::GitDiffOutcome;
pub use objects::{ChangeType, FileDiff};

mod git {
    pub(crate) mod commit;
    pub(crate) mod traverse;
}

pub mod utils {
    pub mod git_helper;
    pub(crate) mod thread_helper;
}

pub mod calculation {
    use crate::GitDiffOutcome;
    use gix::ObjectId;
    use log::{info, trace};

    pub fn diff_pairs(
        repo: &gix::Repository,
        pairs: Vec<(ObjectId, Option<ObjectId>)>,
        max_threads: usize,
        diff_algorithm: Option<gix::diff::blob::Algorithm>,
    ) -> anyhow::Result<Vec<GitDiffOutcome>> {
        {
            info!("Processing {} pair(s)", pairs.len());
            let num_threads = crate::utils::thread_helper::num_threads(max_threads);
            trace!("threads used: {:?}", num_threads);
            let diffs =
                crate::git::traverse::calculate_pairs(repo, pairs, num_threads, diff_algorithm)?;

            Ok(diffs)
        }
    }
}

pub mod traversal {
    use crate::objects::GitDiffOutcome;
    use log::{info, trace};

    pub fn main(
        repo: &gix::Repository,
        commitlist: Vec<String>,
        max_threads: usize,
        skip_merges: bool,
        diff_algorithm: Option<gix::diff::blob::Algorithm>,
        breadth_first: bool,
        follow: bool,
        limit: Option<usize>, // TODO remove or implement smth else here
    ) -> anyhow::Result<Vec<GitDiffOutcome>> {
        let cl = crate::git::commit::prepare_commit_list(
            repo,
            commitlist,
            skip_merges,
            breadth_first,
            follow,
            limit,
        )?;
        info!("Processing {} commit(s)", cl.iter().count());
        let num_threads = crate::utils::thread_helper::num_threads(max_threads);
        trace!("threads used: {:?}", num_threads);
        let diffs =
            crate::git::traverse::traverse_commit_graph(repo, cl, num_threads, diff_algorithm)?;

        Ok(diffs)
    }
}
