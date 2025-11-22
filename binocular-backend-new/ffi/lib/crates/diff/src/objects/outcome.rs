use crate::objects::file_diff::FileDiff;
use gix::ObjectId;
use commits::GitCommitMetric;
use shared::signature::Sig;

#[derive(Debug, Clone)]
pub struct GitDiffOutcome {
    pub files: Vec<FileDiff>,
    pub commit: GitCommitMetric,
    pub parent: Option<GitCommitMetric>,
    pub committer: Option<Sig>,
    pub author: Option<Sig>,
}

impl GitDiffOutcome {
    pub fn new(
        // change_map: HashMap<BString, (u32, u32, String)>,
        files: Vec<FileDiff>,
        commit: GitCommitMetric,
        parent: Option<GitCommitMetric>,
        committer: Option<Sig>,
        author: Option<Sig>,
    ) -> anyhow::Result<Self> {
        Ok(Self {
            // change_map,
            files,
            commit,
            parent,
            committer,
            author,
        })
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use gix::bstr::BString;
    use gix::ObjectId;

    // Mock ObjectId for testing
    fn mock_object_id() -> ObjectId {
        let hex_string = "35f39037f97d1a0da12a383506c83b1a58492917";

        // Convert the hex string to a buffer of 40 bytes
        let buffer = Vec::from(hex_string);

        ObjectId::from_hex(&*buffer).unwrap()
    }

    // Mock Sig for testing
    fn mock_signature() -> Sig {
        Sig {
            name: BString::from("John Doe"),
            email: BString::from("john@example.com"),
            time: gix::date::Time {
                seconds: 1609459200, // Jan 1, 2021
                offset: 0,
            },
        }
    }

    // Test 1: Test GitDiffMetrics::new() with a non-empty change_map
    // #[test]
    // fn test_git_diff_metrics_new_non_empty() {
    //     let mut change_map = HashMap::new();
    //     change_map.insert(BString::from("file1.txt"), (10u32, 5u32, String::new()));
    //     change_map.insert(BString::from("file2.rs"), (3u32, 1u32, String::new()));
    //
    //     let commit = mock_object_id();
    //     let parent = Some(mock_object_id());
    //     let committer = Some(mock_signature());
    //     let author = Some(mock_signature());
    //
    //     let metrics =
    //         GitDiffOutcome::new(change_map.clone(), commit, parent, committer, author).unwrap();
    //
    //     assert_eq!(metrics.change_map, change_map);
    //     assert_eq!(metrics.commit, commit);
    //     assert_eq!(metrics.parent.unwrap(), parent.unwrap());
    //     assert_eq!(metrics.committer.unwrap(), mock_signature());
    //     assert_eq!(metrics.author.unwrap(), mock_signature());
    // }

    // Test 2: Test GitDiffMetrics::new() with an empty change_map
    // #[test]
    // fn test_git_diff_metrics_new_empty() {
    //     let change_map: HashMap<BString, (u32, u32, String)> = HashMap::new();
    //     let commit = mock_object_id();
    //     let parent = None;
    //     let committer = Some(mock_signature());
    //     let author = None;
    //
    //     let metrics =
    //         GitDiffOutcome::new(change_map.clone(), commit, parent, committer, author).unwrap();
    //
    //     assert_eq!(metrics.change_map, change_map);
    //     assert_eq!(metrics.commit, commit);
    //     assert!(metrics.parent.is_none());
    //     assert_eq!(metrics.committer.unwrap(), mock_signature());
    //     assert!(metrics.author.is_none());
    // }

    // Test 3: Test GitDiffMetricsVector::from()
    // #[test]
    // fn test_git_diff_metrics_vector_from() {
    //     let mut change_map = HashMap::new();
    //     change_map.insert(BString::from("file1.txt"), (10u32, 5u32, String::new()));
    //
    //     let commit = mock_object_id();
    //     let metrics = GitDiffOutcome::new(change_map.clone(), commit, None, None, None).unwrap();
    //
    //     assert_eq!(metrics.change_map, change_map);
    // }

    // Test 5: Test Renderable values() for GitDiffMetricsVector with valid data
    // #[test]
    // fn test_git_diff_metrics_vector_values_non_empty() {
    //     let mut change_map = HashMap::new();
    //     change_map.insert(BString::from("file1.txt"), (10u32, 5u32, String::new()));
    //
    //     let commit = mock_object_id();
    //     let parent = mock_object_id();
    //     let metrics =
    //         GitDiffOutcome::new(change_map.clone(), commit, Some(parent), None, None).unwrap();
    //
    //     assert_eq!(metrics.commit.to_string(), commit.to_string());
    //     assert_eq!(
    //         metrics.parent.expect("expected").to_string(),
    //         parent.to_string()
    //     );
    //
    //     assert_eq!(metrics.change_map, change_map);
    // }
}
