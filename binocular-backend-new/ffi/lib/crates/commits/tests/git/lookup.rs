mod util {
    use std::path::Path;

    pub fn get_demo_repo() -> gix::Repository {
        // let repo_workdir_pathbuf = repo_workdir("make_diff_for_rewrites_repo.sh").unwrap();

        gix::discover(Path::new("./")).unwrap()
    }
}

#[cfg(test)]
mod no_mailmap {
    use parameterized::parameterized;
    use crate::git::lookup::util::get_demo_repo;
    const USE_MAILMAP: bool = false;

    #[parameterized(
    commit_sha = {
        "9853fe8e0e05871b5757c21a23015f3dd169c568",
        "e0c0fafcde92fc28e6945b741c3e500c03416af2"
    },
    author_email = {
        "tmoer93@gmail.com",
        "dave@example.com"
    },
    author_name = {
        "Thomas Moerbauer",
        "Dave"
    },
    committer_email = {
        "tmoer93@gmail.com",
        "dave@example.com"
    },
    committer_name = {
        "Thomas Moerbauer",
        "Dave"
    })]
    fn test_find_commit(commit_sha: &str, author_email: &str, committer_email: &str, committer_name: &str, author_name: &str) {
        let repo = get_demo_repo();

        let c = commits::find_commit(&repo, commit_sha.to_string(), USE_MAILMAP).unwrap();

        pretty_assertions::assert_eq!(c.commit.to_string(), commit_sha);
        // author
        pretty_assertions::assert_eq!(c.author.clone().email.to_string(), author_email);
        pretty_assertions::assert_eq!(c.author.name.to_string(), author_name);
        // committer
        pretty_assertions::assert_eq!(c.committer.clone().email.to_string(), committer_email);
        pretty_assertions::assert_eq!(c.committer.name.to_string(), committer_name);

        pretty_assertions::assert_eq!(c.committer.time, c.author.time);
    }

    #[parameterized(
    commit_sha = {
        "1627f2aec571240f2a77f8234613738f7653bf26"
    },
    author_email = {
        "se.watzinger@gmail.com"
    },
    author_name = {
        "Sebastian Watzinger"
    },
    committer_email = {
        "se.watzinger@gmail.com"
    },
    committer_name = {
        "Sebastian Watzinger"
    })]
    fn test_find_commit_different_author_committer(commit_sha: &str, author_email: &str, committer_email: &str, committer_name: &str, author_name: &str) {
        let repo = get_demo_repo();

        let c = commits::find_commit(&repo, commit_sha.to_string(), USE_MAILMAP).unwrap();

        pretty_assertions::assert_eq!(c.commit.to_string(), commit_sha);
        // author
        pretty_assertions::assert_eq!(c.author.clone().email.to_string(), author_email);
        pretty_assertions::assert_eq!(c.author.name.to_string(), author_name);
        // committer
        pretty_assertions::assert_eq!(c.committer.clone().email.to_string(), committer_email);
        pretty_assertions::assert_eq!(c.committer.name.to_string(), committer_name);

        pretty_assertions::assert_ne!(c.committer.time, c.author.time);
    }
}

#[cfg(test)]
mod error_handling {
    use commits::CommitLookupError;
    use crate::git::lookup::util::get_demo_repo;

    #[test]
    fn test_invalid_hash_returns_revision_parse_error() {
        let repo = get_demo_repo();
        let invalid_hash = "not-a-valid-hash-at-all!@#$".to_string();

        let result = commits::find_commit(&repo, invalid_hash.clone(), false);

        assert!(result.is_err());
        let err = result.unwrap_err();
        assert!(
            matches!(err, CommitLookupError::RevisionParseError { .. }),
            "Expected RevisionParseError, got: {:?}",
            err
        );
        assert_eq!(err.hash(), invalid_hash);
        assert!(err.is_not_found());
    }

    #[test]
    fn test_nonexistent_commit_returns_revision_parse_error() {
        let repo = get_demo_repo();
        // Valid hex format but non-existent commit
        let nonexistent_hash = "0000000000000000000000000000000000000000".to_string();

        let result = commits::find_commit(&repo, nonexistent_hash.clone(), false);

        assert!(result.is_err());
        let err = result.unwrap_err();
        // gix returns RevisionParseError for non-existent commits
        assert!(
            matches!(err, CommitLookupError::RevisionParseError { .. }),
            "Expected RevisionParseError for non-existent commit, got: {:?}",
            err
        );
        assert_eq!(err.hash(), nonexistent_hash);
        assert!(err.is_not_found());
    }

    #[test]
    fn test_abbreviated_nonexistent_hash_returns_error() {
        let repo = get_demo_repo();
        // Abbreviated hash that doesn't exist
        let short_hash = "deadbeef".to_string();

        let result = commits::find_commit(&repo, short_hash.clone(), false);

        assert!(result.is_err());
        let err = result.unwrap_err();
        assert!(err.is_not_found());
        assert_eq!(err.hash(), short_hash);
    }

    #[test]
    fn test_empty_hash_returns_error() {
        let repo = get_demo_repo();
        let empty_hash = "".to_string();

        let result = commits::find_commit(&repo, empty_hash.clone(), false);

        assert!(result.is_err());
        let err = result.unwrap_err();
        assert!(
            matches!(err, CommitLookupError::RevisionParseError { .. }),
            "Expected RevisionParseError for empty hash, got: {:?}",
            err
        );
    }

    #[test]
    fn test_error_message_contains_hash() {
        let repo = get_demo_repo();
        let invalid_hash = "invalid-test-hash".to_string();

        let result = commits::find_commit(&repo, invalid_hash.clone(), false);

        assert!(result.is_err());
        let err = result.unwrap_err();
        let error_message = err.to_string();
        assert!(
            error_message.contains(&invalid_hash),
            "Error message should contain the invalid hash. Message: {}",
            error_message
        );
    }

    #[test]
    fn test_valid_revision_spec_head_works() {
        let repo = get_demo_repo();

        // HEAD is a valid revision spec
        let result = commits::find_commit(&repo, "HEAD".to_string(), false);

        assert!(result.is_ok(), "HEAD should be a valid revision spec");
    }

    #[test]
    fn test_valid_abbreviated_hash_works() {
        let repo = get_demo_repo();
        // Use first 7 chars of a known commit
        let abbreviated = "9853fe8".to_string();

        let result = commits::find_commit(&repo, abbreviated, false);

        assert!(result.is_ok(), "Abbreviated hash should work");
        let commit = result.unwrap();
        assert!(commit.commit.to_string().starts_with("9853fe8"));
    }
}

#[cfg(test)]
mod with_mailmap {
    use crate::git::lookup::util::get_demo_repo;
    const USE_MAILMAP: bool = true;

    #[test]
    fn test_find_commit() {
        let repo = get_demo_repo();

        let c = commits::find_commit(&repo, "9853fe8e0e05871b5757c21a23015f3dd169c568".to_string(), USE_MAILMAP).unwrap();

        pretty_assertions::assert_eq!(c.commit.to_string(), "9853fe8e0e05871b5757c21a23015f3dd169c568".to_string());
        // committer
        pretty_assertions::assert_eq!(c.committer.clone().email.to_string(), "tmoer93@gmail.com");
        pretty_assertions::assert_eq!(c.committer.name.to_string(), "Thomas Mörbauer");
        // author
        pretty_assertions::assert_eq!(c.author.clone().email.to_string(), "tmoer93@gmail.com");
    }

    #[test]
    fn test_dave_example_com_mailmap() {
        let repo = get_demo_repo();

        let c = commits::find_commit(&repo, "e0c0fafcde92fc28e6945b741c3e500c03416af2".to_string(), USE_MAILMAP).unwrap();

        pretty_assertions::assert_eq!(c.commit.to_string(), "e0c0fafcde92fc28e6945b741c3e500c03416af2".to_string());
        // committer
        pretty_assertions::assert_eq!(c.committer.clone().email.to_string(), "manuel.stoeger@inso-world.com");
        pretty_assertions::assert_eq!(c.committer.name.to_string(), "Manuel Stöger");
        // author
        pretty_assertions::assert_eq!(c.author.clone().email.to_string(), "manuel.stoeger@inso-world.com");
        pretty_assertions::assert_eq!(c.author.name.to_string(), "Manuel Stöger");
    }
}