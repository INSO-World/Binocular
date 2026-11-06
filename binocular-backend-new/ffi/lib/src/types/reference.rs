use gix::refs::Reference;
use crate::types::branch::GixBranch;

#[derive(Eq, PartialEq, Hash, Debug, Clone, Copy, uniffi::Enum)]
pub enum GixReferenceCategory {
    LocalBranch,
    RemoteBranch,
    Tag,
    Note,
    PseudoRef,
    Unknown,
    MainPseudoRef,
    MainRef,
    LinkedPseudoRef,
    LinkedRef,
    Bisect,
    Rewritten,
    WorktreePrivate,
}

impl<'a> From<gix::refs::Category<'a>> for GixReferenceCategory {
    fn from(category: gix::refs::Category) -> Self {
        match category {
            gix::refs::Category::LocalBranch => GixReferenceCategory::LocalBranch,
            gix::refs::Category::RemoteBranch => GixReferenceCategory::RemoteBranch,
            gix::refs::Category::Tag => GixReferenceCategory::Tag,
            gix::refs::Category::Note => GixReferenceCategory::Note,
            gix::refs::Category::PseudoRef => GixReferenceCategory::PseudoRef,
            gix::refs::Category::MainPseudoRef => GixReferenceCategory::MainPseudoRef,
            gix::refs::Category::MainRef => GixReferenceCategory::MainRef,
            gix::refs::Category::LinkedPseudoRef { .. } => GixReferenceCategory::LinkedPseudoRef,
            gix::refs::Category::LinkedRef { .. } => GixReferenceCategory::LinkedRef,
            gix::refs::Category::Bisect => GixReferenceCategory::Bisect,
            gix::refs::Category::Rewritten => GixReferenceCategory::Rewritten,
            gix::refs::Category::WorktreePrivate => GixReferenceCategory::WorktreePrivate,
        }
    }
}


impl From<Reference> for GixBranch {
    fn from(r: Reference) -> Self {
        let category = r.name.category()
            .map(GixReferenceCategory::from)
            .unwrap_or(GixReferenceCategory::Unknown);

        Self {
            full_name: r.name.clone(),
            name: r.name.shorten().to_string(),
            target: r.target.into_id(),
            category,
        }
    }
}