pub(crate) mod types {
    pub(crate) mod blame;
    pub(crate) mod branch;
    pub(crate) mod commit;
    pub(crate) mod diff;
    pub(crate) mod error;
    pub(crate) mod repo;
    pub(crate) mod signature;

    type AnyhowError = anyhow::Error;
    // For interfaces, wrap a unit struct with `#[uniffi::remote]`.
    #[uniffi::remote(Object)]
    pub struct AnyhowError;

    #[derive(Debug, thiserror::Error, uniffi::Error)]
    pub enum UniffiError {
        #[error("Invalid input: {0}")]
        InvalidInput(String),

        #[error("Operation failed: {0}")]
        OperationFailed(String),

        // #[error(transparent)]
        // GixDiscoverError(#[from] gix::discover::Error),
        #[error("Operation failed: {0}")]
        GixDiscoverError(String),
    }

    type LogLevel = log::Level;

    // Use #[uniffi::remote] to enable support for passing the types across the FFI

    // For records/enums, wrap the item definition with `#[uniffi::remote]`.
    // Copy each field/variant definitions exactly as they appear in the remote crate.
    #[uniffi::remote(Enum)]
    pub enum LogLevel {
        Error = 1,
        Warn = 2,
        Info = 3,
        Debug = 4,
        Trace = 5,
    }
}

pub mod ffi {
    use crate::types::branch::BinocularBranch;
    use crate::types::commit::GixCommit;
    use crate::types::diff::BinocularDiffInput;
    use crate::types::error::ProcErrorInterface;
    use crate::types::repo::{BinocularRepository, RepositoryRemote};
    use crate::types::UniffiError;
    use gix::remote::Direction;
    use gix::ThreadSafeRepository;
    use std::collections::HashMap;
    use std::ops::Deref;

    #[uniffi::export]
    fn hello() {
        println!("Hello, world!");
    }

    #[uniffi::export]
    fn find_repo(path: String) -> anyhow::Result<BinocularRepository> {
        let repo = match gix::discover(path) {
            Ok(r) => r.into_sync(),
            Err(e) => panic!("{:?}", e),
        };

        let binding = repo.to_thread_local();

        let origin = binding
            .find_default_remote(Direction::Push)
            .or_else(|| binding.find_default_remote(Direction::Fetch))
            .and_then(Result::ok);

        Ok(BinocularRepository {
            git_dir: repo.refs.git_dir().display().to_string(),
            work_tree: repo.work_tree.map(|val| val.display().to_string()),
            origin: origin.map(|o| RepositoryRemote::from(o)),
        })
    }

    #[uniffi::export]
    fn find_commit(
        binocular_repo: &BinocularRepository,
        hash: String,
    ) -> anyhow::Result<GixCommit> {
        println!("repo at {:?}", binocular_repo);
        let repo = ThreadSafeRepository::discover(&binocular_repo.git_dir)?;

        let binding = repo.to_thread_local();
        let commit = binding
            .rev_parse_single(hash.deref())?
            .object()?
            .try_into_commit()?;

        Ok(commit.id)
    }

    #[uniffi::export]
    fn find_all_branches(
        binocular_repo: &BinocularRepository,
    ) -> anyhow::Result<Vec<BinocularBranch>> {
        println!("repo at {:?}", binocular_repo);
        let repo = ThreadSafeRepository::discover(&binocular_repo.git_dir)?;

        let binding = repo.to_thread_local();

        let references = binding.references()?;
        let local_branches = references.local_branches()?;
        let remote_branches = references.remote_branches()?;

        Ok(remote_branches
            .chain(local_branches)
            .filter(Result::is_ok)
            .map(Result::unwrap)
            .map(BinocularBranch::from)
            .collect())
    }

    #[uniffi::export]
    fn traverse_branch(
        binocular_repo: &BinocularRepository,
        branch: String,
    ) -> Result<Vec<commits::GitCommitMetric>, UniffiError> {
        let repo = match ThreadSafeRepository::discover(&binocular_repo.git_dir) {
            Ok(r) => r,
            Err(e) => panic!("{:?}", UniffiError::GixDiscoverError(e.to_string())),
        };
        let binding = repo.to_thread_local();

        match commits::traversal::main(binding, vec![branch], false) {
            Ok(r) => Ok(r),
            Err(e) => Err(UniffiError::OperationFailed(e.to_string())),
        }
    }

    #[uniffi::export]
    fn traverse(
        binocular_repo: &BinocularRepository,
        source_commit: GixCommit,
        target_commit: Option<GixCommit>,
    ) -> anyhow::Result<Vec<commits::GitCommitMetric>> {
        let repo = ThreadSafeRepository::discover(&binocular_repo.git_dir)?;

        let binding = repo.to_thread_local();
        let cmt = binding.find_commit(source_commit)?;
        let trgt = match target_commit {
            None => None,
            Some(c) => Option::from(binding.find_commit(c)?),
        };

        let result = commits::traversal::traverse_from_to(&binding, &cmt, &trgt);

        result
    }

    #[uniffi::export]
    fn diffs(
        binocular_repo: &BinocularRepository,
        commit_pairs: Vec<BinocularDiffInput>,
        max_threads: u8,
        diff_algorithm: Option<crate::types::diff::GixDiffAlgorithm>,
    ) -> anyhow::Result<Vec<crate::types::diff::BinocularDiffVec>, ProcErrorInterface> {
        let repo = ThreadSafeRepository::discover(&binocular_repo.git_dir)
            .expect(format!("Cannot discover repository at '{}'", binocular_repo.git_dir).as_str());

        use binocular_diff::calculation::diff_pairs;

        let binding = repo.to_thread_local();
        let r = diff_pairs(
            &binding,
            commit_pairs.iter().map(|c| (c.suspect, c.target)).collect(),
            max_threads as usize,
            diff_algorithm,
        )
        .unwrap();

        let mapped = r
            .into_iter()
            .map(crate::types::diff::BinocularDiffVec::from)
            .collect();

        Ok(mapped)
    }

    #[uniffi::export]
    fn blames(
        binocular_repo: &BinocularRepository,
        defines: HashMap<gix::ObjectId, Vec<String>>,
        diff_algorithm: Option<crate::types::diff::GixDiffAlgorithm>,
        max_threads: u8,
    ) -> anyhow::Result<Vec<crate::types::blame::BinocularBlameResult>> {
        let repo = ThreadSafeRepository::discover(&binocular_repo.git_dir)?;

        use binocular_blame::process;
        use std::time::Instant;

        let binding = repo.to_thread_local();

        // println!(
        //     "process(repo={:?},#defines={},algo={:?},threads={})",
        //     repo,
        //     defines.len(),
        //     diff_algorithm,
        //     max_threads
        // );

        let mut start = Instant::now();
        let iterable = gix::hashtable::HashMap::from_iter(defines);
        let mut duration = start.elapsed();

        println!("Time elapsed in from_iter() is: {:?}", duration);

        start = Instant::now();
        let result = process(&binding, iterable, diff_algorithm, max_threads as usize);
        duration = start.elapsed();
        println!("Time elapsed in process() is: {:?}", duration);

        // println!("Found {} blames", result?.len());

        Ok(result?
            .into_iter()
            .map(crate::types::blame::BinocularBlameResult::from)
            .collect())
        // Ok(())
    }

    //     uniffi::custom_type!(gix::discover::Error, FfiError), {
    // // Remote is required since `Url` is from a different crate
    // remote,
    // try_lift: | val | Ok(Url::parse( & val) ? ),
    // lower: | obj | obj.into(),
    // });
}

uniffi::setup_scaffolding!();
