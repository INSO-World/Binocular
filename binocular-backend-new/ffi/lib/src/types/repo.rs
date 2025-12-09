use crate::types::remote::GixRemote;
use crate::types::UniffiError;
use gix::ThreadSafeRepository;
use std::path::PathBuf;

#[derive(Debug, Clone, uniffi::Record)]
pub struct GixRepository {
    pub git_dir: String,
    pub work_tree: Option<String>,
    pub remotes: Vec<GixRemote>,
}

uniffi::custom_type!(PathBuf, String, {
    remote,
    lower: move |r| r.into_os_string().into_string().unwrap(),
    try_lift: |r| Ok(PathBuf::from(r)),
});

impl TryFrom<GixRepository> for ThreadSafeRepository {
    type Error = UniffiError;

    fn try_from(gix_repo: GixRepository) -> Result<Self, Self::Error> {
        discover_repo(gix_repo.git_dir)
    }
}

pub fn discover_repo(path: String) -> Result<ThreadSafeRepository, UniffiError> {
    ThreadSafeRepository::discover(path)
        .map_err(|e| UniffiError::GixDiscoverError(e.to_string()))
}
