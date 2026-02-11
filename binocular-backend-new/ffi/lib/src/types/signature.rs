use gix::bstr::BString;
use gix::date::{OffsetInSeconds, SecondsSinceUnixEpoch};

type GixTime = gix::date::Time;
#[uniffi::remote(Record)]
pub struct GixTime {
    /// The seconds that passed since UNIX epoch. This makes it UTC, or `<seconds>+0000`.
    pub seconds: SecondsSinceUnixEpoch,
    /// The time's offset in seconds, which may be negative to match the `sign` field.
    pub offset: OffsetInSeconds,
}

pub type GixSignature = shared::signature::Sig;
#[uniffi::remote(Record)]
pub struct GixSignature {
    pub name: BString,
    pub email: BString,
    pub time: GixTime,
}
uniffi::custom_type!(BString, String, {
    remote,
    lower: move |r| r.to_string(),
    try_lift: |r| Ok(BString::from(r)),
});
