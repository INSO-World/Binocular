use crate::types::commit::GixCommit;
use binocular_diff::{ChangeType, FileDiff, GitDiffOutcome};
use gix::bstr::BString;
use gix::ObjectId;

pub type GixDiffAlgorithm = gix::diff::blob::Algorithm;

#[derive(Debug, uniffi::Enum)]
pub enum GixChangeType {
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
        copy: bool,
    },
}

#[derive(Debug, uniffi::Record)]
pub struct GixFileDiff {
    pub insertions: u32,
    pub deletions: u32,
    pub change: GixChangeType,
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
pub struct GixDiff {
    pub files: Vec<GixFileDiff>,
    pub commit: GixCommit,
    pub parent: Option<GixCommit>,
}
#[derive(Debug, uniffi::Record)]
pub struct GixDiffStats {
    insertions: u32,
    deletions: u32,
    kind: String,
}

#[derive(Debug, uniffi::Record)]
pub struct GixDiffInput {
    pub suspect: ObjectId,
    pub target: Option<ObjectId>,
}

impl From<GitDiffOutcome> for GixDiff {
    fn from(value: GitDiffOutcome) -> Self {
        Self {
            files: value.files.into_iter().map(GixFileDiff::from).collect(),
            commit: GixCommit::from(value.commit),
            parent: value.parent.map(GixCommit::from),
        }
    }
}

impl From<FileDiff> for GixFileDiff {
    fn from(value: FileDiff) -> Self {
        Self {
            deletions: value.deletions,
            insertions: value.insertions,
            change: GixChangeType::from(value.change),
            old_file_content: value.old_file_content,
            new_file_content: value.new_file_content,
        }
    }
}

impl From<ChangeType> for GixChangeType {
    fn from(v: ChangeType) -> Self {
        match v {
            ChangeType::Addition { location } => Self::Addition { location },
            ChangeType::Deletion { location } => Self::Deletion { location },
            ChangeType::Modification { location } => Self::Modification { location },
            ChangeType::Rewrite {
                source_location,
                location,
                copy,
            } => Self::Rewrite {
                source_location,
                location,
                copy,
            },
        }
    }
}
