use gix::Reference;

#[derive(Debug, uniffi::Record)]
pub struct BinocularBranch {
    pub name: String,
    pub commits: Vec<String>,
}

impl From<Reference<'_>> for BinocularBranch {
    fn from(reference: Reference) -> Self {
        Self {
            name: reference.name().as_bstr().to_string(),
            commits: vec![],
        }
    }
}
