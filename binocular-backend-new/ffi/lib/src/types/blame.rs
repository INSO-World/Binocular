use binocular_blame::types::{BlameOutcome, BlameResult};
use gix::blame::BlameEntry;
use gix::ObjectId;

#[derive(Debug, uniffi::Record)]
pub struct GixBlameEntry {
    pub start_in_blamed_file: u32,
    pub start_in_source_file: u32,
    pub len: u32,
    pub commit_id: ObjectId,
}

#[derive(Debug, uniffi::Record)]
pub struct GixBlameOutcome {
    pub entries: Vec<GixBlameEntry>,
    pub file_path: String,
}

#[derive(Debug, uniffi::Record)]
pub struct GixBlameResult {
    pub blames: Vec<GixBlameOutcome>,
    pub commit: ObjectId,
}

impl From<BlameResult> for GixBlameResult {
    fn from(value: BlameResult) -> Self {
        Self {
            blames: value
                .blames
                .into_iter()
                .map(GixBlameOutcome::from)
                .collect(),
            commit: value.commit_oid,
        }
    }
}

impl From<gix::blame::BlameEntry> for GixBlameEntry {
    fn from(value: BlameEntry) -> Self {
        Self {
            start_in_blamed_file: value.start_in_blamed_file,
            start_in_source_file: value.start_in_source_file,
            len: u32::from(value.len),
            commit_id: value.commit_id,
        }
    }
}

impl From<BlameOutcome> for GixBlameOutcome {
    fn from(value: BlameOutcome) -> Self {
        Self {
            entries: value
                .entries
                .into_iter()
                .map(GixBlameEntry::from)
                .collect(),
            file_path: value.file_path,
        }
    }
}
