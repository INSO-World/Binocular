use crate::git::objects::BlameOutcome;

#[derive(Debug, Clone)]
pub struct BlameResult {
    pub blames: Vec<BlameOutcome>,
    pub commit_oid: gix::ObjectId,
}

pub(crate) struct BlameResultVec(pub(crate) Vec<BlameResult>);
