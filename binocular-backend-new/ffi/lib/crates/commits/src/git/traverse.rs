use crate::git::metrics::GitCommitMetric;
use crate::git::utils::apply_mailmap;
use gix::mailmap::Snapshot;
use gix::refs::Reference;
use gix::revision::walk::Info;
use gix::traverse::commit::topo::Sorting;
use gix::traverse::commit::Parents;
use gix::Commit;
use log::{debug, trace};
use std::collections::HashMap;

pub fn traverse_from_to(
    repo: &gix::Repository,
    source_commit: &Commit,
    target_commit: &Option<Commit>,
    use_mailmap: bool,
) -> anyhow::Result<Vec<GitCommitMetric>> {
    let mut mailmap: Option<Snapshot> = None;
    if use_mailmap {
        mailmap = Some(repo.open_mailmap())
    }
    let tc_id = match target_commit {
        None => {
            debug!("No Target commit specified");
            None::<Vec<gix::ObjectId>>
        }
        Some(to) => {
            debug!("Target commit specified");
            Some(vec![to.id])
        }
    };

    let sorting = Sorting::TopoOrder;
    let parents = Parents::All;
    let commit_graph = repo.commit_graph().ok();

    let traverse_result =
        gix::traverse::commit::topo::Builder::from_iters(&repo, [source_commit.id], tc_id)
            .with_commit_graph(commit_graph)
            .sorting(sorting)
            .parents(parents)
            .build()?
            .filter_map(|info| {
                info.ok()
                    .and_then(|info| Some(Info::new(info, &repo)))
            });

    let walk_result: Vec<_> = traverse_result
        .map(|a| {
            let commit = &a.object().unwrap();

            to_metric(mailmap.clone(), a, commit)
        })
        .collect();

    Ok(walk_result)
}

fn to_metric(mailmap: Option<Snapshot>, a: Info, commit: &Commit) -> GitCommitMetric {
    let mut gcm = GitCommitMetric::from(a);
    match mailmap {
        None => {}
        Some(mm) => {
            match apply_mailmap(commit.committer(), &mm) {
                None => {}
                Some(mailmap_committer) => gcm.committer = mailmap_committer,
            }

            match apply_mailmap(commit.author(), &mm) {
                None => {}
                Some(mailmap_author) => gcm.author = mailmap_author,
            }
        }
    }

    gcm
}

pub fn traverse_commit_graph(
    repo: gix::Repository,
    references: Vec<Reference>,
    skip_merges: bool,
    use_mailmap: bool,
) -> anyhow::Result<HashMap<Reference, Vec<GitCommitMetric>>> {
    let mut branch_commits_map: HashMap<Reference, Vec<GitCommitMetric>> = HashMap::new();
    for reference in references {
        let target = reference.clone().target.into_id();

        let val: Vec<_> = if let Ok(target_commit) = repo.find_commit(target) {
            traverse_from_to(&repo, &target_commit, &None, use_mailmap)?
        } else {
            Vec::new()
        }
        .into_iter()
        .filter(|c| {
            return if skip_merges && c.parents.len() > 1 {
                trace!("Skipping Merge Commit {:?}", c.commit);
                false
            } else {
                true
            };
        })
        .map(|mut gcm| {
            gcm.branch = Option::from(reference.name.shorten().to_string());
            gcm
        })
        .collect();
        // branch_commits_map.append((branch.deref(), val));
        branch_commits_map.insert(reference.clone(), val);
    }

    Ok(branch_commits_map)
}
