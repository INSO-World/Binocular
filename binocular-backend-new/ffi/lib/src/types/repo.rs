use std::path::{Path, PathBuf};
use gix::remote::Direction;
use gix::Remote;

#[derive(Debug, uniffi::Record)]
pub struct BinocularRepository {
    pub git_dir: String,
    pub work_tree: Option<String>,
    pub origin: Option<RepositoryRemote>,
}

uniffi::custom_type!(PathBuf, String, {
    remote,
    lower: move |r| r.into_os_string().into_string().unwrap(),
    try_lift: |r| Ok(PathBuf::from(r)),
});

#[derive(Debug, uniffi::Record)]
pub struct RepositoryRemote {
    pub name: Option<String>,
    pub url: Option<String>,
    pub path: Option<String>,
}

impl From<Remote<'_>> for RepositoryRemote {
    fn from(remote: Remote) -> Self {
        let remote_url = remote
            .url(Direction::Push)
            .or_else(|| remote.url(Direction::Fetch));
        RepositoryRemote {
            name: remote.name().map(|s| s.as_bstr().to_string()),
            url: remote_url.and_then(|e| e.host().map(|h| h.to_string())),
            path: remote_url.and_then(|e| Option::from(e.path.to_string())),
        }
    }
}

// uniffi::custom_type!(ThreadSafeRepository, BinocularRepository, {
//     remote,
//     lower: move |r| BinocularRepository {
//
//         git_dir: r.refs.git_dir().display().to_string()
//     },
//     try_lift: |r| Ok(gix::ThreadSafeRepository::discover(r.git_dir)?),
// });
