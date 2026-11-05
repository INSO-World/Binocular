use gix::bstr::BString;
use gix::objs::tree::EntryMode;
use gix::ObjectId;

#[derive(Debug, Clone)]
pub struct Entry {
    mode: EntryMode,
    hash: ObjectId,
    path: BString,
}

impl Entry {
    pub(crate) fn new(mode: EntryMode, hash: ObjectId, path: BString) -> Self {
        Self { mode, hash, path }
    }
}
