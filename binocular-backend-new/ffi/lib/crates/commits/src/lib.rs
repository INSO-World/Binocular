mod git {
    pub mod metrics;
    pub mod traverse;
}

// pub use crate::git::traverse;
pub use git::metrics::GitCommitMetric;

pub mod traversal {
    pub use crate::git::traverse::{traverse_commit_graph as main, traverse_from_to};
    use crate::GitCommitMetric;
    use std::ops::Deref;

    pub fn from_to(
        repo: gix::Repository,
        source: String,
        target: Option<String>,
    ) -> anyhow::Result<Vec<GitCommitMetric>> {
        let binding = repo.clone();
        let source_commit = binding
            .rev_parse_single(source.deref())?
            .object()?
            .try_into_commit()?;
        let target_commit = match target {
            None => None,
            Some(target) => Option::from({
                binding
                    .rev_parse_single(target.deref())?
                    .object()?
                    .try_into_commit()?
            }),
        };

        traverse_from_to(&repo, &source_commit, &target_commit)
    }
}
