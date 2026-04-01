use commits::GitCommitMetric;
use gix::bstr::BString;
use gix::ObjectId;

#[derive(uniffi::Record, Debug)]
pub struct GixCommit {
    pub oid: gix::ObjectId,
    pub message: String,
    pub committer: crate::types::signature::GixSignature,
    pub author: crate::types::signature::GixSignature,
    pub branch: Option<String>,
    pub parents: Vec<gix::ObjectId>,
    pub file_tree: Vec<BString>,
}

uniffi::custom_type!(ObjectId, String, {
    remote,
    lower: move |r| r.to_string(),
    try_lift: |r| Ok(gix::ObjectId::from_hex(r.as_bytes())?),
});

impl From<GitCommitMetric> for GixCommit {
    fn from(commit: GitCommitMetric) -> Self {
        GixCommit {
            oid: commit.commit,
            message: commit.message,
            committer: commit.committer,
            author: commit.author,
            branch: commit.branch,
            parents: commit.parents,
            file_tree: commit.file_tree,
        }
    }
}
