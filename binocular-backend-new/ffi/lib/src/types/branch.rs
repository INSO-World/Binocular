use gix::bstr::BString;
use gix::refs::FullName;
use gix::ObjectId;
use crate::types::reference::GixReferenceCategory;

#[derive(Eq, PartialEq, Hash, Debug, uniffi::Record)]
pub struct GixBranch {
    pub full_name: FullName,
    pub name: String,
    pub target: ObjectId,
    pub category: GixReferenceCategory,
}

uniffi::custom_type!(FullName, BString, {
    remote,
    // Lowering our Rust SerializableStruct into a String.
    lower: |s| s.into_inner(),
    // Lifting our foreign String into our Rust SerializableStruct
    try_lift: |s| {
        let a = FullName::try_from(s)?;
        Ok(a)
    },
});
