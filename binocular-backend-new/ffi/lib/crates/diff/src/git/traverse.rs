use crate::objects::{Entry, GitDiffOutcome};
use crate::utils;
use anyhow::Result;
use commits::GitCommitMetric;
use crossbeam_queue::SegQueue;
use gix::{diff::blob::Platform, Commit, ObjectId};
use log::{debug, error, trace};
use std::thread::JoinHandle;
#[cfg(feature = "progress")]
use tqdm::tqdm;

pub fn calculate_pairs(
    repo: &gix::Repository,
    pairs: Vec<(ObjectId, Option<ObjectId>)>,
    max_threads: usize,
    diff_algorithm: Option<gix::diff::blob::Algorithm>,
) -> Result<Vec<GitDiffOutcome>> {
    use rayon::prelude::*;

    let pool = rayon::ThreadPoolBuilder::new()
        .num_threads(max_threads)
        .build()?;

    let repo_sync = repo.clone().into_sync();

    let mut rewrite_cache =
        repo.diff_resource_cache(gix::diff::blob::pipeline::Mode::ToGit, Default::default())?;
    rewrite_cache
        .options
        .skip_internal_diff_if_external_is_configured = false;
    rewrite_cache.options.algorithm = diff_algorithm;

    let out = SegQueue::new();
    pool.install(|| -> Result<()> {
        pairs.par_iter().try_for_each_init(
            || {
                // Per-worker thread-local repo + cache
                let _repo = repo_sync.clone().to_thread_local();
                let mut rewrite_cache = rewrite_cache.clone();
                rewrite_cache.clear_resource_cache();
                (_repo, rewrite_cache)
            },
            |(_repo, rewrite_cache), (suspect, target)| -> Result<()> {
                // process items on THIS worker, reusing its cache
                let s_commit = _repo.find_object(*suspect)?.into_commit();
                let p_commit = match target {
                    Some(_target) => Some(_repo.find_object(*_target)?.into_commit()),
                    None => None,
                };

                // your existing function; returns Result<YourDiffType>
                if let Ok(diff) = compute_diff(s_commit, p_commit, rewrite_cache) {
                    out.push(diff);
                }
                Ok(())
            },
        )
    })?;

    // Drain results (order not guaranteed; stable order needs an indexed collect)
    let mut diffs = Vec::with_capacity(out.len());
    while let Some(d) = out.pop() {
        diffs.push(d)
    }
    Ok(diffs)
}

pub fn traverse_commit_graph(
    repo: &gix::Repository,
    commitlist: Vec<Commit>,
    num_threads: usize,
    diff_algorithm: Option<gix::diff::blob::Algorithm>,
) -> Result<Vec<GitDiffOutcome>> {
    let mailmap = repo.open_mailmap();
    trace!("Algorithm: {:?}", diff_algorithm);
    let prob_capacity = commitlist.len() * 2;

    let (churn_threads, churn_tx) = get_churn_channel(repo, num_threads, &mailmap, diff_algorithm)?;

    let mut count = 0;

    #[cfg(feature = "progress")]
    let iterable_list = tqdm(commitlist).desc(Some("Commits sent"));
    #[cfg(not(feature = "progress"))]
    let iterable_list = commitlist;

    for commit in iterable_list {
        match churn_tx.send(commit.id) {
            Ok(_) => {}
            Err(e) => {
                error!("Send Error {:?}", e);
            }
        }
        count += 1;
    }

    debug!("total_number_of_commits: {:?}", count);

    drop(churn_tx);

    // Wait for the threads to complete any remaining work
    let mut diff_results: Vec<GitDiffOutcome> = Vec::with_capacity(prob_capacity);
    for child in churn_threads {
        let result = child.join().expect("oops! the child thread panicked");
        match result {
            Ok(mut r) => {
                debug!("result.len: {:?}", r.iter().clone().len());
                diff_results.append(&mut r);
            }
            Err(_) => {}
        }
    }

    // let vectorized = GitDiffOutcomeVec(diff_results.clone());
    // let df = vectorized.to_df()?;
    //println!("{:?}", df.head(Some(10)));
    //diff_results.truncate(limit);
    Ok(diff_results)
}

fn get_churn_channel(
    repo: &gix::Repository,
    max_threads: usize,
    mailmap: &gix::mailmap::Snapshot,
    diff_algorithm: Option<gix::diff::blob::Algorithm>,
) -> Result<(
    Vec<JoinHandle<Result<Vec<GitDiffOutcome>>>>,
    crossbeam_channel::Sender<ObjectId>,
)> {
    let (tx, rx) = crossbeam_channel::bounded::<gix::hash::ObjectId>(8 * 100);
    let repo_sync = repo.clone().into_sync();
    // let mut children = Vec::new();
    let mut children = Vec::with_capacity(max_threads);

    let mut rewrite_cache =
        repo.diff_resource_cache(gix::diff::blob::pipeline::Mode::ToGit, Default::default())?;
    rewrite_cache
        .options
        .skip_internal_diff_if_external_is_configured = false;
    rewrite_cache.options.algorithm = diff_algorithm;

    for _ in 0..max_threads {
        let child = std::thread::spawn({
            let repo = repo_sync.clone().to_thread_local();
            let rx_clone = rx.clone();
            let mut commits_vec = Vec::new();
            let mut rewrite_cache = rewrite_cache.clone();
            let mailmap = mailmap.clone();
            rewrite_cache.clear_resource_cache();
            move || -> Result<_> {
                debug!("std::thread::spawn: {:?}", std::thread::current().id());
                while let Ok(commit_id) = rx_clone.recv() {
                    let commit = repo.find_object(commit_id)?.into_commit();

                    debug!("(recv)\t{:?}", commit.id);
                    let mut diff_result =
                        compute_diff_with_parent(&commit, &repo, &mut rewrite_cache)?;
                    let author = mailmap.resolve(commit.author()?);
                    let committer = mailmap.resolve(commit.committer()?);

                    for diff in &mut diff_result {
                        diff.author = Some(author.clone().into());
                        diff.committer = Some(committer.clone().into());
                    }

                    commits_vec.append(diff_result.as_mut())
                }
                drop(rx_clone);
                Ok(commits_vec)
            }
        });
        children.push(child);
    }
    drop(rx);
    Ok((children, tx))
}

fn compute_diff(
    suspect: Commit,
    target: Option<Commit>,
    rewrite_cache: &mut Platform,
) -> Result<GitDiffOutcome> {
    let suspect_tree = suspect.tree()?;
    let target_tree = match &target {
        Some(t) => t.tree()?,
        None => suspect.repo.empty_tree(),
    };

    // let source_tree = suspect_tree.iter()
    //     .filter_map(|res| res.ok().map(|entry| entry.inner)) // dropping errors silently
    //     // .filter(|entry| !args.tree_only || (entry.mode.is_tree()))
    //     .map(|entry| Entry::new(entry.mode, entry.oid.to_owned(), entry.filename.to_owned()))
    //     .collect::<Vec<_>>();

    let change_map = utils::git_helper::calculate_changes(
        &target_tree,
        &suspect_tree,
        rewrite_cache,
        &mut rewrite_cache.clone(),
    );
    Ok(GitDiffOutcome::new(
        change_map,
        GitCommitMetric::from(suspect),
        match target {
            Some(t) => Some(GitCommitMetric::from(t)),
            None => None,
        },
        None,
        None,
    )?)
}

fn compute_diff_with_parent(
    commit: &Commit,
    repo: &gix::Repository,
    rewrite_cache: &mut Platform,
) -> Result<Vec<GitDiffOutcome>> {
    let parent_commits: Vec<Commit> = commit
        .parent_ids()
        .filter(|p| p.object().is_ok())
        .map(|p| p.object().unwrap().into_commit())
        .collect();
    let mut parent_trees: Vec<(Option<&Commit>, gix::Tree)> = parent_commits
        .iter()
        .map(|parent_commit| {
            let parent_commit_id = parent_commit.id();
            match repo
                .find_object(parent_commit_id)
                .ok()
                .and_then(|c| c.peel_to_tree().ok())
            {
                Some(tree) => (Some(parent_commit), tree),
                None => panic!("parent_commit could not be found"),
            }
        })
        .collect();
    if parent_trees.is_empty() {
        debug!("Adding empty tree");
        parent_trees.push((None, repo.empty_tree()));
    }

    debug!("commit {:?}\tparents {:?}", commit, parent_commits);

    let diffs: Vec<GitDiffOutcome> = parent_trees
        .into_iter()
        .map(|(parent_commit, parent_tree)| {
            let change_map = utils::git_helper::calculate_changes(
                &parent_tree,
                &commit.tree().unwrap(),
                rewrite_cache,
                &mut rewrite_cache.clone(),
            );
            GitDiffOutcome::new(
                change_map,
                GitCommitMetric::from(commit.clone()),
                match parent_commit {
                    Some(pc) => Some(GitCommitMetric::from(pc.clone())),
                    None => None,
                },
                None,
                None,
            )
            .expect("Diff result should be processable")
        })
        .collect();

    Ok(diffs)
}
