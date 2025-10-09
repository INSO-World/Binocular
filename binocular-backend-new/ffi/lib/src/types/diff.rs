use binocular_diff::{ChangeType, FileDiff, GitDiffOutcome};
use gix::bstr::BString;
use gix::ObjectId;

pub type GixDiffAlgorithm = gix::diff::blob::Algorithm;

#[derive(Debug, uniffi::Enum)]
pub enum BinocularChangeType {
    Addition {
        location: BString,
    },
    Deletion {
        location: BString,
    },
    Modification {
        location: BString,
    },
    Rewrite {
        source_location: BString,
        location: BString,
        copy: bool
    }
}

#[derive(Debug, uniffi::Record)]
pub struct BinocularFileDiff {
    pub insertions: u32,
    pub deletions: u32,
    pub change: BinocularChangeType,
    pub old_file_content: Option<String>,
    pub new_file_content: Option<String>,
}

#[uniffi::remote(Enum)]
pub enum GixDiffAlgorithm {
    Histogram,
    Myers,
    MyersMinimal,
}

#[derive(Debug, uniffi::Record)]
pub struct BinocularDiffVec {
    pub files: Vec<BinocularFileDiff>,
    pub commit: ObjectId,
    pub parent: Option<ObjectId>,
    pub committer: Option<crate::types::signature::BinocularSig>,
    pub author: Option<crate::types::signature::BinocularSig>,
}
#[derive(Debug, uniffi::Record)]
pub struct BinocularDiffStats {
    insertions: u32,
    deletions: u32,
    kind: String,
}

#[derive(Debug, uniffi::Record)]
pub struct BinocularDiffInput {
    pub suspect: ObjectId,
    pub target: Option<ObjectId>,
}

impl From<GitDiffOutcome> for BinocularDiffVec {
    fn from(value: GitDiffOutcome) -> Self {
        Self {
            files: value.files.into_iter().map(BinocularFileDiff::from).collect(),
            commit: value.commit,
            parent: value.parent,
            committer: value.committer,
            author: value.author,
        }
    }
}

impl From<FileDiff> for BinocularFileDiff {
    fn from(value: FileDiff) -> Self {
        Self {
            deletions: value.deletions,
            insertions: value.insertions,
            change: BinocularChangeType::from(value.change),
            old_file_content: value.old_file_content,
            new_file_content: value.new_file_content,
        }
    }
}

impl From<ChangeType> for BinocularChangeType {
    fn from(v: ChangeType) -> Self {
        match v {
            ChangeType::Addition { location } => Self::Addition { location },
            ChangeType::Deletion { location } => Self::Deletion { location },
            ChangeType::Modification { location } => Self::Modification { location },
            ChangeType::Rewrite { source_location, location, copy } =>
                Self::Rewrite { source_location, location, copy },
        }
    }
}

