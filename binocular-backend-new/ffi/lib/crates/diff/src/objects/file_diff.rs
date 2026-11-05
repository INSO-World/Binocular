use super::change::ChangeType;

#[derive(Debug, Clone)]
pub struct FileDiff {
    pub insertions: u32,
    pub deletions: u32,
    pub change: ChangeType,
    pub old_file_content: Option<String>,
    pub new_file_content: Option<String>,
}