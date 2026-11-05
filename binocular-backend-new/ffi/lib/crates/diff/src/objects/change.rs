use gix::bstr::BString;

#[derive(Debug, Clone)]
pub enum ChangeType {
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
