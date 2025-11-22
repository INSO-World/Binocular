use gix::remote::Direction;
use gix::Remote;

#[derive(Debug, uniffi::Record)]
pub struct GixRemote {
    pub name: String,
    pub url: String,
}

impl From<Remote<'_>> for GixRemote {
    fn from(remote: Remote) -> Self {
        let remote_url = remote
            .url(Direction::Fetch)
            .or_else(|| remote.url(Direction::Push))
            .expect("should be the remote URL");
        GixRemote {
            name: remote
                .name()
                .expect("remote should have name")
                .as_bstr()
                .to_string(),
            url: remote_url.to_string(),
        }
    }
}
