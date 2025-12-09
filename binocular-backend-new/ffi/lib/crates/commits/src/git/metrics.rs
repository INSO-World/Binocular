use base64::prelude::*;
use gix::bstr::BString;
use gix::Commit;
use shared::signature::Sig;
#[derive(Debug, Clone)]
pub struct GitCommitMetric {
    pub commit: gix::ObjectId,
    pub message: String,
    /// The committer is the person who last applied the work
    pub committer: Sig,
    /// The author is the person who originally wrote the work
    pub author: Sig,
    pub branch: Option<String>,
    pub parents: Vec<gix::ObjectId>,
    pub file_tree: Vec<BString>,
}

impl From<Commit<'_>> for GitCommitMetric {
    fn from(commit: Commit<'_>) -> Self {
        let commit_ref = commit.decode().unwrap();
        let parents = commit
            .parent_ids()
            .filter(|p| p.object().is_ok())
            .map(|p| p.object().unwrap().into_commit())
            .map(|p_id| p_id.id)
            .collect::<Vec<_>>();

        let file_tree = commit
            .tree()
            .unwrap_or(commit.repo.empty_tree())
            .iter()
            .filter_map(|res| res.ok().map(|entry| entry.inner)) // dropping errors silently
            // .filter(|entry| !args.tree_only || (entry.mode.is_tree()))
            .map(|entry| entry.filename.to_owned())
            .collect::<Vec<_>>();

        Self {
            commit: commit.id,
            //message: commit_ref.message.to_string().trim().to_string(),
            message: BASE64_STANDARD.encode(commit_ref.message.to_string().trim()),
            author: Sig::from(commit_ref.author()),
            committer: Sig::from(commit_ref.committer()),
            branch: None,
            parents,
            file_tree,
        }
    }
}

impl From<gix::revision::walk::Info<'_>> for GitCommitMetric {
    fn from(info: gix::revision::walk::Info) -> Self {
        let commit = info.object().unwrap();
        GitCommitMetric::from(commit)
    }
}
