use crate::git::traverse::util::{get_demo_repo, get_demo_repo_merges};
use cartography_diff::traversal::main;
use gix::date::time::Sign;
use gix_testtools::bstr::BString;
use pretty_assertions::assert_eq;
use assertables::assert_none;
#[test]
fn check_correct_number_of_results_unlimited() {
    let local_repo = get_demo_repo();
    let result = main(
        &local_repo,              // repo
        vec!["HEAD".to_string()], // commitlist
        1,                        // max_threads
        false,                    //no_merges
        None,                     //diff_algo
        true,                     // breadth_first
        true,                     // follow
        None,                     // limit
    )
    .unwrap();
    assert_eq!(result.iter().clone().count(), 21);
}

#[test]
fn check_correct_number_of_results_20_committish() {
    let local_repo = get_demo_repo();
    let result = main(
        &local_repo,
        vec!["HEAD".to_string()],
        1,
        false,
        None,
        true,
        true,
        Some(20),
    )
    .unwrap();
    assert_eq!(result.iter().clone().count(), 20);
}

#[test]
fn check_correct_number_of_results_3_commitlist_with_limit_1() {
    let local_repo = get_demo_repo();
    let commitlist = vec![
        String::from("0cf7a4fe3ad6c49ae7beb394a1c1df7cc5173ce4"),
        String::from("a9f4112b75ecad0cb07a45e20e2a363f29729157"),
        String::from("d78c63c5ea3149040767e4387e7fc743cda118fd"),
    ];
    let result = main(
        &local_repo,
        commitlist,
        1,
        false,
        None,
        true,
        false,
        Some(1),
    )
    .unwrap();
    assert_eq!(result.iter().clone().count(), 3);
}

#[test]
fn check_correct_number_of_results_3_commitlist_unlimited() {
    let local_repo = get_demo_repo();
    let commitlist = vec![
        String::from("0cf7a4fe3ad6c49ae7beb394a1c1df7cc5173ce4"),
        String::from("a9f4112b75ecad0cb07a45e20e2a363f29729157"),
        String::from("d78c63c5ea3149040767e4387e7fc743cda118fd"),
    ];
    let result =
        main(&local_repo, commitlist,1, false, None, true, false, None).unwrap();
    assert_eq!(result.iter().clone().count(), 3);
}

#[test]
#[should_panic]
fn check_commitlist_fail_on_non_existent_sha() {
    let local_repo = get_demo_repo();
    let commitlist = vec![String::from("0cf7a4fe3ad6c49ae7beb394a1c1df7cc5173cad")];
    main(&local_repo, commitlist,1, false, None, true, true , None).unwrap();
}

#[test]
fn check_correct_number_of_results_commitlist_empty_input() {
    let local_repo = get_demo_repo();
    let result =
        main(&local_repo, vec![],1, false, None, true, true, None).unwrap();
    assert_eq!(result.iter().clone().count(), 0);
}

#[test]
fn check_correct_number_of_results_19_committish() {
    let local_repo = get_demo_repo();
    let result = main(
        &local_repo,
        vec!["HEAD".to_string()],
        1,
        false,
        None,
        true,
        true,
        Some(19),
    )
    .unwrap();
    assert_eq!(result.iter().clone().count(), 19);
}

#[test]
fn check_correct_number_of_results_21_committish() {
    let local_repo = get_demo_repo();
    let result = main(
        &local_repo,
        vec!["HEAD".to_string()],
        1,
        false,
        None,
        true,
        true,
        Some(21),
    )
    .unwrap();
    assert_eq!(result.iter().clone().count(), 21);
}

#[test]
fn check_correct_number_of_results_22_committish() {
    let local_repo = get_demo_repo();
    let result = main(
        &local_repo,
        vec!["HEAD".to_string()],
        1,
        false,
        None,
        true,
        true,
        Some(22),
    )
    .unwrap();
    // git rev-list --count --no-merges HEAD returns 21
    assert_eq!(result.iter().clone().count(), 21);
}

#[test]
fn check_correct_number_of_results_0_committish() {
    let local_repo = get_demo_repo();
    let result = main(
        &local_repo,
        vec!["HEAD".to_string()],
        1,
        false,
        None,
        true,
        true,
        Some(0),
    )
    .unwrap();
    assert!(result.is_empty());
}
#[test]
fn check_correct_number_of_results_0_commitlist() {
    let local_repo = get_demo_repo();
    let result = main(
        &local_repo,
        vec![String::from("922051b304015810e6056a72d9ef61d55e7763ed")],
        1,
        false,
        None,
        true,
        true,
        Some(0),
    )
    .unwrap();
    assert!(result.is_empty());
}

#[test]
fn check_correct_number_of_results_start_hash_922051b304015810e6056a72d9ef61d55e7763ed() {
    // first commit, initial
    let start_hash = String::from("922051b304015810e6056a72d9ef61d55e7763ed");
    let local_repo = get_demo_repo();
    let result_vec = main(
        &local_repo,
        vec![start_hash.clone()],
        1,
        false,
        None,
        true,
        true,
        None,
    )
    .unwrap();

    assert_eq!(result_vec.iter().clone().count(), 1);
}

#[test]
#[should_panic]
fn check_correct_number_of_results_start_hash_ed292b87739f56b1179f64aa813dc96fb6128555_should_fail_committish(
) {
    // first commit, initial
    let start_hash = String::from("ed292b87739f56b1179f64aa813dc96fb6128555");
    let local_repo = get_demo_repo();
    let result_vec = main(
        &local_repo,
        vec![start_hash.clone()],
        1,
        false,
        None,
        true,
        true,
        None,
    )
    .unwrap();

    assert_eq!(result_vec.iter().clone().count(), 1);
}

#[test]
#[should_panic]
fn check_correct_number_of_results_start_hash_ed292b87739f56b1179f64aa813dc96fb6128555_should_fail_commitlist(
) {
    // first commit, initial
    let start_hash = String::from("ed292b87739f56b1179f64aa813dc96fb6128555");
    let local_repo = get_demo_repo();
    let result_vec = main(
        &local_repo,
        vec![start_hash],
        1,
        false,
        None,
        true,
        true,
        None,
    )
    .unwrap();

    assert_eq!(result_vec.iter().clone().count(), 1);
}

#[test]
fn check_correct_result_start_hash_922051b304015810e6056a72d9ef61d55e7763ed() {
    // first commit, initial
    let start_hash = String::from("922051b304015810e6056a72d9ef61d55e7763ed");
    let local_repo = get_demo_repo();
    let result_vec = main(
        &local_repo,
        vec![start_hash.clone()],                   // commitlist
        1,                        // max_threads
        false,                    // no_merges
        None,                     // diff_algo
        true,                     // breadth_first
        true, //follow
        None,                     // limit
    )
    .unwrap();

    let result = result_vec
        .get(0)
        .expect("Failed to get one and only element");
    assert_eq!(result.commit.to_string(), start_hash.clone());
    assert_eq!(result.parent, None);

    assert_ne!(result.committer, None);
    assert_ne!(result.author, None);
    let author = &<Option<shared::signature::Sig> as Clone>::clone(&result.author).unwrap();
    let committer = &<Option<shared::signature::Sig> as Clone>::clone(&result.committer).unwrap();

    assert_ne!(author, committer);
    assert_eq!(author.name, "author");
    assert_eq!(author.email, "author@example.com");
    assert_eq!(
        author.time,
        gix::date::Time {
            seconds: 946684800, // 1.1.2000 00:00:00
            offset: 0,
            sign: Sign::Plus
        }
    );

    assert_eq!(committer.name, "committer");
    assert_eq!(committer.email, "committer@example.com");
    assert_eq!(
        committer.time,
        gix::date::Time {
            seconds: 946771200, // 2.1.2000 00:00:00
            offset: 0,
            sign: Sign::Plus
        }
    );

    assert_ne!(result.change_map.get(&BString::from("a")), None);
    assert_eq!(result.change_map.get(&BString::from("a")).unwrap(), &(0, 0));

    assert_ne!(result.change_map.get(&BString::from("b")), None);
    assert_eq!(result.change_map.get(&BString::from("b")).unwrap(), &(0, 0));

    assert_ne!(result.change_map.get(&BString::from("dir/c")), None);
    assert_eq!(
        result.change_map.get(&BString::from("dir/c")).unwrap(),
        &(0, 0)
    );

    assert_ne!(result.change_map.get(&BString::from("d")), None);
    assert_eq!(result.change_map.get(&BString::from("d")).unwrap(), &(0, 0));
}

#[test]
fn check_correct_number_of_result_start_hash_11899e89f0d6c9d7fd68aa79f356c9a49a9f319a() {
    // first commit, initial
    let start_hash = String::from("11899e89f0d6c9d7fd68aa79f356c9a49a9f319a");
    let local_repo = get_demo_repo();
    let result_vec = main(
        &local_repo,
        vec![start_hash.clone()],
        1,
        false,
        None,
        true,
        true,
        None,
    )
    .unwrap();

    assert_eq!(result_vec.iter().count(), 2);

    let result_0 = result_vec.get(0).expect("Failed to get first");
    assert_eq!(
        result_0.commit.to_string(),
        "11899e89f0d6c9d7fd68aa79f356c9a49a9f319a"
    );
    assert_ne!(result_0.parent, None);
    assert_eq!(
        result_0.parent.unwrap().to_string(),
        "922051b304015810e6056a72d9ef61d55e7763ed"
    );

    let result_1 = result_vec.get(1).expect("Failed to get second");
    assert_eq!(
        result_1.commit.to_string(),
        "922051b304015810e6056a72d9ef61d55e7763ed"
    );
    assert_none!(
        result_1.parent
    )
}

#[test]
fn check_correct_number_of_result_start_hash_2a8baaceb3d79f157aaf6a7967278eb65288e073() {
    // first commit, initial
    let start_hash = String::from("2a8baaceb3d79f157aaf6a7967278eb65288e073");
    let local_repo = get_demo_repo();
    let result_vec = main(
        &local_repo,
        vec![start_hash.clone()],
        1,
        false,
        None,
        true,
        true,
        Some(2),
    )
    .unwrap();

    assert_eq!(result_vec.iter().count(), 2);

    let result_0 = result_vec.get(0).expect("Failed to get first");
    assert_eq!(
        result_0.commit.to_string(),
        "2a8baaceb3d79f157aaf6a7967278eb65288e073"
    );
    assert_eq!(result_0.change_map.clone().iter().count(), 2);

    let result_1 = result_vec.get(1).expect("Failed to get second");
    assert_eq!(
        result_1.commit.to_string(),
        "11899e89f0d6c9d7fd68aa79f356c9a49a9f319a"
    );
    assert_eq!(result_1.change_map.clone().iter().count(), 4);
}

#[test]
fn check_correct_number_of_result_start_hash_b6c93f947ec4c96039bac4971c681d7a18bc436d() {
    // first commit, initial
    let start_hash = String::from("b6c93f947ec4c96039bac4971c681d7a18bc436d");
    let local_repo = get_demo_repo();
    let result_vec = main(
        &local_repo,
        vec![start_hash.clone()],
        1,
        false,
        None,
        true,
        false,
        None,
    )
    .unwrap();

    assert_eq!(result_vec.iter().count(), 1);

    let result_0 = result_vec.get(0).expect("Failed to get first");
    assert_eq!(
        result_0.commit.to_string(),
        "b6c93f947ec4c96039bac4971c681d7a18bc436d"
    );
    assert_eq!(
        result_0.parent.unwrap().to_string(),
        "7fdf7c8b6607b31f5400418e3732d50091265ac5"
    );
    println!("{:?}", result_0.change_map);
    assert_eq!(result_0.change_map.clone().iter().count(), 2);

    assert_ne!(result_0.change_map.get(&BString::from("b")), None);
    assert_eq!(
        result_0.change_map.get(&BString::from("b")).unwrap(),
        &(1, 0)
    );
    assert_ne!(result_0.change_map.get(&BString::from("dir/c-moved")), None);
    assert_eq!(
        result_0
            .change_map
            .get(&BString::from("dir/c-moved"))
            .unwrap(),
        &(1, 0)
    );
}

#[test]
fn check_correct_number_of_result_start_hash_f3b695021ac313bd223396abb70e2c472106220a() {
    // first commit, initial
    let start_hash = String::from("f3b695021ac313bd223396abb70e2c472106220a");
    let local_repo = get_demo_repo();
    let result_vec = main(
        &local_repo,
        vec![start_hash.clone()],
        1,
        false,
        None,
        true,
        false,
        None,
    )
    .unwrap();

    assert_eq!(result_vec.iter().count(), 1);

    let result_0 = result_vec.get(0).expect("Failed to get first");
    assert_eq!(
        result_0.commit.to_string(),
        "f3b695021ac313bd223396abb70e2c472106220a"
    );
    assert_eq!(
        result_0.parent.unwrap().to_string(),
        "de5eea3539a859a57509d986593375ddfa932116"
    );
    println!("{:?}", result_0.change_map);
    assert_eq!(result_0.change_map.clone().iter().count(), 4);
    assert_ne!(result_0.change_map.get(&BString::from("dir/link-2")), None);
    assert_ne!(result_0.change_map.get(&BString::from("no-link")), None);
    assert_ne!(
        result_0.change_map.get(&BString::from("renamed-link-1")),
        None
    );
    assert_ne!(result_0.change_map.get(&BString::from("z-link-2")), None);

    assert_eq!(
        result_0
            .change_map
            .get(&BString::from("dir/link-2"))
            .unwrap(),
        &(0, 1)
    );

    assert_eq!(
        result_0.change_map.get(&BString::from("no-link")).unwrap(),
        &(0, 1)
    );

    assert_eq!(
        result_0
            .change_map
            .get(&BString::from("renamed-link-1"))
            .unwrap(),
        &(0, 0)
    );

    assert_eq!(
        result_0.change_map.get(&BString::from("z-link-2")).unwrap(),
        &(1, 0)
    );
}

#[test]
fn check_correct_number_of_results_skip_merges_false() {
    let local_repo = get_demo_repo_merges();
    let result_vec = main(
        &local_repo,
        vec!["HEAD".to_string()],
        1,
        false,
        None,
        true,
        true,
        None,
    )
    .unwrap();

    assert_eq!(result_vec.iter().count(), 38);
}

#[test]
fn check_correct_history_of_merges() {
    let start_hash = String::from("1823ac918111531ef2984bc3b667f5c199a584b9");
    let local_repo = get_demo_repo_merges();
    let result_vec = main(
        &local_repo,
        vec![start_hash.clone()],
        1,
        false,
        None,
        true,
        false,
        None,
    )
    .unwrap();

    assert_eq!(result_vec.iter().count(), 2);

    let result_0 = result_vec.get(0).expect("Failed to get first");
    assert_eq!(
        result_0.commit.to_string(),
        "1823ac918111531ef2984bc3b667f5c199a584b9"
    );
    assert_eq!(
        result_0.parent.unwrap().to_string(),
        "ed93e447508cdff606d90e9d7ebdaa152833086c"
    );

    let result_1 = result_vec.get(1).expect("Failed to get second");
    assert_eq!(
        result_1.commit.to_string(),
        "1823ac918111531ef2984bc3b667f5c199a584b9"
    );
    assert_eq!(
        result_1.parent.unwrap().to_string(),
        "49cb9aa3b1fcbb4588e73774e52c24ebb70f65d0"
    );
}

#[test]
fn check_correct_number_of_results_skip_merges_true() {
    let local_repo = get_demo_repo_merges();
    let result_vec = main(
        &local_repo,
        vec!["HEAD".to_string()],
        1,
        true,
        None,
        true,
        true,
        None,
    )
    .unwrap();

    assert_eq!(result_vec.iter().count(), 32);
}

mod util {
    use std::path::PathBuf;

    pub fn get_demo_repo() -> gix::Repository {
        let repo_workdir_pathbuf = repo_workdir("make_diff_for_rewrites_repo.sh").unwrap();

        gix::discover(repo_workdir_pathbuf.as_path()).unwrap()
    }

    pub fn get_demo_repo_merges() -> gix::Repository {
        let repo_workdir_pathbuf = repo_workdir("make_blame_repo.sh").unwrap();

        gix::discover(repo_workdir_pathbuf.as_path()).unwrap()
    }

    fn repo_workdir(script_name: &str) -> gix_testtools::Result<PathBuf> {
        gix_testtools::scripted_fixture_read_only_standalone(script_name)
    }
}
