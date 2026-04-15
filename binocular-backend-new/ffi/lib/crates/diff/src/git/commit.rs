use gix::revision::walk::Sorting;
use gix::traverse::commit::simple::CommitTimeOrder;
use gix::Commit;
use log::{error, info, trace, warn};

pub(crate) fn prepare_commit_list(
    repo: &'_ gix::Repository,
    commitlist: Vec<String>,
    skip_merges: bool,
    breadth_first: bool,
    follow: bool,
    limit: Option<usize>,
) -> anyhow::Result<Vec<Commit<'_>>> {
    let result_limit = limit.unwrap_or(usize::MAX);
    // Do not calculate anything if limit is set to <= 0
    if result_limit <= 0 {
        warn!("limit = 0 provided, not doing any calculation!");
        return Ok(Vec::new());
    } else if commitlist.iter().count() == 0 {
        warn!("No commits provided, not doing any calculation!");
        return Ok(Vec::new());
    } else if follow && commitlist.iter().count() > 1 {
        error!("Cannot follow more than 1 commit");
        panic!("Cannot follow more than 1 commit")
    }

    let commit_graph = repo.commit_graph().ok();
    trace!("commit_graph available?: {:?}", commit_graph.is_some());

    let sorting = if breadth_first {
        Sorting::BreadthFirst
    } else {
        Sorting::ByCommitTime(CommitTimeOrder::NewestFirst)
    };

    let commit_iter: Vec<Commit> = commitlist
        .iter()
        .map(|c| {
            repo.rev_parse_single(gix::bstr::BStr::new(c.as_bytes()))
                .expect(format!("Commit '{:?}' must exist (1)", c).as_str())
                .object()
                .expect(format!("Commit object '{:?}' must exist", c).as_str())
                .try_into_commit()
                .expect(format!("Commit '{:?}' must exist (2)", c).as_str())
        })
        .collect();

    // Generate a commit list based on the 'follow' flag.
    let commits: Vec<Commit> = if follow {
        // When following ancestors, we expect exactly one commit.
        assert_eq!(
            commit_iter.len(),
            1,
            "Expected exactly one commit when 'follow' is enabled"
        );
        // Safely retrieve the single commit.
        let commit = commit_iter
            .get(0)
            .expect("Commit must exist when following ancestors");

        // Get the commit’s ancestors using the provided options.
        let ancestors = commit
            // .expect(format!("Commit '{:?}' must exist (3)", cmt).as_str())
            .id()
            .ancestors()
            .sorting(sorting)
            //.with_commit_graph(commit_graph)
            .all()
            .expect(&format!(
                "Failed to retrieve ancestors for commit {:?}",
                commit
            ));

        // Process the ancestor iterator:
        //   - Skip any erroneous entries by filtering with `filter_map`
        //   - Convert each successful entry to a commit object
        //   - Limit the number of results by `result_limit`
        ancestors
            .filter_map(|res| {
                res.ok() // Only consider Ok values
                    .and_then(|entry| entry.object().ok()) // Get the commit object if available
            })
            .take(result_limit)
            .collect()
    } else {
        // If not following ancestors, use the provided commit list as-is.
        commit_iter.into_iter().collect()
    };

    // Optionally filter out merge commits (those with more than one parent)
    // when the `no_merges` flag is enabled.
    let final_commit_list: Vec<Commit> = commits
        .into_iter()
        .filter(|commit| {
            if skip_merges && commit.parent_ids().count() > 1 {
                trace!("Skipping merge commit {:?}", commit.id);
                false
            } else {
                true
            }
        })
        .collect();

    if final_commit_list.iter().count() == 0 {
        warn!("No more commits left based on the arguments, aborting with empty result");
        return Ok(Vec::new());
    }

    info!("Processing {} commit(s)", final_commit_list.iter().count());

    anyhow::Ok(final_commit_list)
}
