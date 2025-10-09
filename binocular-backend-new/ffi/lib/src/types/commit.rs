use gix::{Commit, ObjectId};
use gix::bstr::BString;

pub type GixCommit = ObjectId;

type BinocularCommitVec = commits::GitCommitMetric;
#[uniffi::remote(Record)]
pub struct BinocularCommitVec {
    pub commit: gix::ObjectId,
    pub message: String,
    pub committer: Option<crate::types::signature::BinocularSig>,
    pub author: Option<crate::types::signature::BinocularSig>,
    pub branch: Option<String>,
    pub parents: Vec<gix::ObjectId>,
    pub file_tree: Vec<BString>,
}

uniffi::custom_type!(ObjectId, String, {
    remote,
    lower: move |r| r.to_string(),
    try_lift: |r| Ok(gix::ObjectId::from_hex(r.as_bytes())?),
});
