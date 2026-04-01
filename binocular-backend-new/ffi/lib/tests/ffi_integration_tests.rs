//! Integration tests for the FFI layer of gix-binocular.
//!
//! These tests verify that the FFI functions work correctly with real Git repositories
//! and handle errors appropriately.

use gix_binocular::*;

mod util {
    use std::path::Path;
    use gix_binocular::types::GixRepository;

    /// Gets the path to the current test repository.
    pub fn get_test_repo_path() -> String {
        Path::new("./").canonicalize().unwrap().display().to_string()
    }

    /// Gets a repository through the FFI layer.
    pub fn get_test_repo() -> GixRepository {
        gix_binocular::find_repo(get_test_repo_path()).expect("Should find test repo")
    }
}

// ============================================================================
// Repository Discovery Tests
// ============================================================================

mod find_repo_tests {
    use super::*;

    #[test]
    fn test_find_repo_with_valid_path() {
        let path = util::get_test_repo_path();

        let result = find_repo(path);

        assert!(result.is_ok(), "Should find repository at valid path");
        let repo = result.unwrap();
        assert!(!repo.git_dir.is_empty(), "git_dir should not be empty");
    }

    #[test]
    fn test_find_repo_with_invalid_path() {
        let invalid_path = "/nonexistent/path/to/repository".to_string();

        let result = find_repo(invalid_path);

        assert!(result.is_err(), "Should fail for invalid path");
        let err = result.unwrap_err();
        // Should be a GixDiscoverError
        let err_string = err.to_string();
        assert!(
            err_string.contains("discovery") || err_string.contains("Git") || err_string.contains("repository"),
            "Error should mention discovery issue: {}",
            err_string
        );
    }

    #[test]
    fn test_find_repo_with_empty_path() {
        let empty_path = "".to_string();

        let result = find_repo(empty_path);

        // Empty path might succeed (discovers from current dir) or fail depending on cwd
        // The important thing is it doesn't panic
        match result {
            Ok(repo) => assert!(!repo.git_dir.is_empty()),
            Err(e) => assert!(!e.to_string().is_empty()),
        }
    }
}

// ============================================================================
// Find Commit Tests
// ============================================================================

mod find_commit_tests {
    use super::*;

    const KNOWN_COMMIT: &str = "9853fe8e0e05871b5757c21a23015f3dd169c568";

    #[test]
    fn test_find_commit_with_valid_hash() {
        let repo = util::get_test_repo();

        let result = find_commit(repo, KNOWN_COMMIT.to_string(), false);

        assert!(result.is_ok(), "Should find known commit");
        let commit = result.unwrap();
        assert!(commit.oid.to_string().starts_with("9853fe8"));
    }

    #[test]
    fn test_find_commit_with_head() {
        let repo = util::get_test_repo();

        let result = find_commit(repo, "HEAD".to_string(), false);

        assert!(result.is_ok(), "HEAD should be a valid revision spec");
    }

    #[test]
    fn test_find_commit_with_abbreviated_hash() {
        let repo = util::get_test_repo();

        let result = find_commit(repo, "9853fe8".to_string(), false);

        assert!(result.is_ok(), "Should find commit with abbreviated hash");
    }

    #[test]
    fn test_find_commit_with_invalid_hash() {
        let repo = util::get_test_repo();
        let invalid_hash = "not-a-valid-hash!@#$".to_string();

        let result = find_commit(repo, invalid_hash, false);

        assert!(result.is_err(), "Should fail for invalid hash");
    }

    #[test]
    fn test_find_commit_with_nonexistent_hash() {
        let repo = util::get_test_repo();
        let nonexistent = "0000000000000000000000000000000000000000".to_string();

        let result = find_commit(repo, nonexistent, false);

        assert!(result.is_err(), "Should fail for non-existent commit");
    }

    #[test]
    fn test_find_commit_with_mailmap() {
        let repo = util::get_test_repo();

        let result = find_commit(repo, KNOWN_COMMIT.to_string(), true);

        assert!(result.is_ok(), "Should work with mailmap enabled");
    }
}

// ============================================================================
// Find All Branches Tests
// ============================================================================

mod find_all_branches_tests {
    use gix_binocular::types::GixRepository;
    use super::*;

    #[test]
    fn test_find_all_branches_returns_branches() {
        let repo = util::get_test_repo();

        let result = find_all_branches(repo);

        assert!(result.is_ok(), "Should find branches in repository");
        let branches = result.unwrap();
        // Most repos have at least one branch
        assert!(!branches.is_empty(), "Should have at least one branch");
    }

    #[test]
    fn test_find_all_branches_with_invalid_repo() {
        // Create an invalid repo reference
        let invalid_repo = GixRepository {
            git_dir: "/nonexistent/path/.git".to_string(),
            work_tree: None,
            remotes: vec![],
        };

        let result = find_all_branches(invalid_repo);

        assert!(result.is_err(), "Should fail with invalid repository");
    }
}

// ============================================================================
// Traverse Branch Tests
// ============================================================================

mod traverse_branch_tests {
    use gix_binocular::types::GixRepository;
    use super::*;

    /// Helper to find a valid branch name to test with
    fn find_test_branch(repo: &GixRepository) -> Option<String> {
        // Try common branch names (must use full ref path, not HEAD)
        let branches = find_all_branches(repo.clone()).ok()?;
        branches.first().map(|b| b.full_name.to_string())
    }

    #[test]
    fn test_traverse_branch_with_real_branch() {
        let repo = util::get_test_repo();

        // Find an actual branch to test with
        let branch_name = find_test_branch(&repo);
        assert!(branch_name.is_some(), "Should have at least one branch");

        let result = traverse_branch(repo, branch_name.unwrap(), false, false);

        assert!(result.is_ok(), "Should traverse branch: {:?}", result.err());
        let traversal = result.unwrap();
        assert!(!traversal.commits.is_empty(), "Branch should have commits");
    }

    #[test]
    fn test_traverse_branch_with_invalid_ref() {
        let repo = util::get_test_repo();
        let invalid_branch = "refs/heads/nonexistent-branch-12345".to_string();

        let result = traverse_branch(repo, invalid_branch, false, false);

        assert!(result.is_err(), "Should fail for non-existent branch");
        let err = result.unwrap_err();
        let err_string = err.to_string();
        assert!(
            err_string.contains("reference") || err_string.contains("Reference"),
            "Error should mention reference issue: {}",
            err_string
        );
    }

    #[test]
    fn test_traverse_branch_skip_merges() {
        let repo_without = util::get_test_repo();
        let repo_with = util::get_test_repo();

        // Find an actual branch to test with
        let branch_name = find_test_branch(&repo_without);
        assert!(branch_name.is_some(), "Should have at least one branch");
        let branch = branch_name.unwrap();

        // Get results with and without skip_merges
        let result_without_skip = traverse_branch(repo_without, branch.clone(), false, false);
        let result_with_skip = traverse_branch(repo_with, branch, true, false);

        // Both should succeed
        assert!(result_without_skip.is_ok(), "Without skip should succeed");
        assert!(result_with_skip.is_ok(), "With skip should succeed");

        // With skip_merges, we should have <= commits (assuming there might be merges)
        let count_without = result_without_skip.unwrap().commits.len();
        let count_with = result_with_skip.unwrap().commits.len();
        assert!(
            count_with <= count_without,
            "skip_merges should result in <= commits"
        );
    }

    #[test]
    fn test_traverse_branch_with_mailmap() {
        let repo = util::get_test_repo();

        // Find an actual branch to test with
        let branch_name = find_test_branch(&repo);
        assert!(branch_name.is_some(), "Should have at least one branch");

        let result = traverse_branch(repo, branch_name.unwrap(), false, true);

        assert!(result.is_ok(), "Should work with mailmap enabled");
    }
}

// ============================================================================
// Error Type Tests
// ============================================================================

mod error_type_tests {
    use super::*;

    #[test]
    fn test_error_messages_are_descriptive() {
        let repo = util::get_test_repo();

        let result = find_commit(repo, "invalid".to_string(), false);

        assert!(result.is_err());
        let err = result.unwrap_err();
        let msg = err.to_string();

        // Error message should be non-empty and descriptive
        assert!(!msg.is_empty(), "Error message should not be empty");
        assert!(
            msg.len() > 10,
            "Error message should be descriptive: {}",
            msg
        );
    }

    #[test]
    fn test_different_error_types_have_different_messages() {
        let repo_commit = util::get_test_repo();
        let repo_branch = util::get_test_repo();

        // Invalid commit hash
        let commit_err = find_commit(repo_commit, "not-valid".to_string(), false).unwrap_err();

        // Invalid branch
        let branch_err = traverse_branch(repo_branch, "refs/heads/nonexistent".to_string(), false, false)
            .unwrap_err();

        // Error messages should be different
        assert_ne!(
            commit_err.to_string(),
            branch_err.to_string(),
            "Different errors should have different messages"
        );
    }
}
