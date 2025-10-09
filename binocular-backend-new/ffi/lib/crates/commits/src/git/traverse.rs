use crate::git::metrics::GitCommitMetric;
use gix::actor::SignatureRef;
use gix::traverse::commit::topo::Sorting;
use gix::traverse::commit::Parents;
use gix::{Commit, Reference};
use log::{debug, trace};
use shared::signature::Sig;

pub fn traverse_from_to(
    repo: &gix::Repository,
    source_commit: &Commit,
    target_commit: &Option<Commit>,
) -> anyhow::Result<Vec<GitCommitMetric>> {
    let mailmap = repo.open_mailmap();
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

    let apply_mailmap = |gix_sig: Result<SignatureRef<'_>, _>| {
        gix_sig.ok().map(|sig| Sig::from(mailmap.resolve(sig)))
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
                    .and_then(|info| Some(gix::revision::walk::Info::new(info, &repo)))
            });

    let walk_result: Vec<_> = traverse_result
        .map(|a| {
            let commit = &a.object().unwrap();
            let mut gcm = GitCommitMetric::from(a);
            match apply_mailmap(commit.committer()) {
                None => {}
                Some(mailmap_committer) => gcm.committer = Some(mailmap_committer),
            }

            match apply_mailmap(commit.author()) {
                None => {}
                Some(mailmap_author) => gcm.author = Some(mailmap_author),
            }

            gcm
        })
        .collect();

    Ok(walk_result)
}

pub fn traverse_commit_graph(
    repo: gix::Repository,
    branches: Vec<String>,
    skip_merges: bool,
) -> anyhow::Result<Vec<GitCommitMetric>> {
    let prefixed_branches: Vec<String> = branches
        .iter()
        .map(|b| {
            if b.contains("origin/") {
                format!("refs/remotes/{b}")
            } else {
                format!("refs/heads/{b}")
            }
        })
        .collect();
    let references = repo.references()?;

    let local_branches = references.local_branches()?;
    let remote_branches = references.remote_branches()?;
    let local_and_remote_branches = local_branches
        .chain(remote_branches)
        .flatten()
        .collect::<Vec<Reference>>();
    println!("local_and_remote_branches: {:?}", local_and_remote_branches);

    let available_branches: Vec<&Reference> = local_and_remote_branches
        .iter()
        .filter(|r| prefixed_branches.contains(&r.name().as_bstr().to_string()))
        .collect();
    if available_branches.is_empty() {
        // bail!("No branches with '{:?}' available", branches);
        return Err(anyhow::anyhow!(
            "No branches with '{:?}' available",
            branches
        ));
    }

    let mut commit_metric_vec: Vec<GitCommitMetric> = Vec::new();
    for branch in available_branches {
        let mut val: Vec<_> = if let Ok(id) = branch.clone().peel_to_commit() {
            traverse_from_to(&repo, &id, &None)?
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
            gcm.branch = Option::from(branch.name().shorten().to_string());
            gcm
        })
        .collect();
        commit_metric_vec.append(&mut val);
    }

    Ok(commit_metric_vec)
}
