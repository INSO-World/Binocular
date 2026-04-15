#[derive(Debug, Clone)]
pub struct BlameOutcome {
    pub entries: Vec<gix::blame::BlameEntry>,
    pub file_path: String,
}