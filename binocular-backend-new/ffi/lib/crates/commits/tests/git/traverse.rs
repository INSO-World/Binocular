mod util {
    use std::path::Path;

    pub fn get_demo_repo() -> gix::Repository {
        // let repo_workdir_pathbuf = repo_workdir("make_diff_for_rewrites_repo.sh").unwrap();

        gix::discover(Path::new("./")).unwrap()
    }
}

#[cfg(test)]
mod no_mailmap {
    use crate::git::traverse::util;
    use gix::bstr::{ByteSlice, ByteVec};
    use parameterized::parameterized;
    use std::process::Command;

    const USE_MAILMAP: bool = false;

    #[parameterized(branch_name = {
        "origin/main",
        "origin/develop"
    })]
    fn traverse_branch_check_author_values(branch_name: &str) {
        use gix::bstr::BString;
        use std::collections::HashMap;

        let repo = util::get_demo_repo();
        let main_branch = repo.find_reference(branch_name).unwrap().detach();

        let main_info =
            commits::traversal::main(repo, vec![main_branch.clone()], false, USE_MAILMAP)
                .unwrap()
                .get_key_value(&main_branch)
                .unwrap()
                .1
                .clone();

        // Check the actual length before grouping
        let original_count = main_info.len();
        println!(
            "Original commit count from branch '{}': {}",
            branch_name, original_count
        );

        // Group commits by committer identity (name + email only, ignoring time)
        // Using a tuple of (name, email) as key instead of Sig (which includes time)
        let mut author_commits_map: HashMap<(BString, BString), Vec<_>> = HashMap::new();

        for gcm in main_info {
            let author = gcm.author.clone();
            let key = (author.name.clone(), author.email.clone());
            author_commits_map
                .entry(key)
                .or_insert_with(Vec::new)
                .push(gcm);
        }

        // Count total commits after grouping
        let total_commits: usize = author_commits_map.values().map(|v| v.len()).sum();
        println!("Total commits after grouping: {}", total_commits);
        println!(
            "Number of unique author identities: {}",
            author_commits_map.len()
        );

        // Verify we didn't lose any commits during grouping
        pretty_assertions::assert_eq!(total_commits, original_count);

        // Show top committers
        let mut author_stats: Vec<_> = author_commits_map
            .iter()
            .map(|((name, email), commits)| (name, email, commits.len()))
            .collect();
        author_stats.sort_by(|a, b| b.2.cmp(&a.2));

        let mut git_log_cmd = Command::new("sh");
        git_log_cmd.arg("-c").arg(
            format!(
                "git log --no-mailmap --format='%an <%ae>' {} | sort -u | wc -l",
                branch_name
            )
            .to_string(),
        );
        let git_log_output = git_log_cmd.output().expect("failed to execute process");
        let value = git_log_output.stdout.to_str().unwrap().trim();
        println!("git log: {}", value);
        pretty_assertions::assert_eq!(author_stats.len().to_string().as_str(), value);

        // println!("\nTop 10 committers by identity (name + email):");
        for (name, email, count) in author_stats.iter() {
            println!("  {} <{}> - {} commits", name, email, count);
            let args = format!(
                "git log --no-mailmap --pretty=format:'%an <%ae>' {} | grep '{} <{}>' | wc -l",
                branch_name,
                name.replace("[", "\\[")
                    .replace("]", "\\]")
                    .into_string()
                    .unwrap(),
                email
                    .replace("[", "\\[")
                    .replace("]", "\\]")
                    .into_string()
                    .unwrap()
            );
            let mut git_log_cmd = Command::new("sh");
            git_log_cmd.arg("-c").arg(args);
            let git_log_output = git_log_cmd.output().expect("failed to execute process");
            let value = git_log_output.stdout.to_str().unwrap().trim();
            println!("git log: {}", value);
            pretty_assertions::assert_eq!(count.to_string().as_str(), value);
        }
    }

    #[parameterized(branch_name = {
        "origin/main",
        "origin/develop"
    })]
    fn traverse_branch_check_committer_values(branch_name: &str) {
        use gix::bstr::BString;
        use std::collections::HashMap;

        let repo = util::get_demo_repo();
        let main_branch = repo.find_reference(branch_name).unwrap().detach();

        let main_info =
            commits::traversal::main(repo, vec![main_branch.clone()], false, USE_MAILMAP)
                .unwrap()
                .get_key_value(&main_branch)
                .unwrap()
                .1
                .clone();

        // Check the actual length before grouping
        let original_count = main_info.len();
        println!(
            "Original commit count from branch '{}': {}",
            branch_name, original_count
        );

        // Group commits by committer identity (name + email only, ignoring time)
        // Using a tuple of (name, email) as key instead of Sig (which includes time)
        let mut committer_commits_map: HashMap<(BString, BString), Vec<_>> = HashMap::new();

        for gcm in main_info {
            let committer = gcm.committer.clone();
            let key = (committer.name.clone(), committer.email.clone());
            committer_commits_map
                .entry(key)
                .or_insert_with(Vec::new)
                .push(gcm);
        }

        // Count total commits after grouping
        let total_commits: usize = committer_commits_map.values().map(|v| v.len()).sum();
        println!("Total commits after grouping: {}", total_commits);
        println!(
            "Number of unique committer identities: {}",
            committer_commits_map.len()
        );

        // Verify we didn't lose any commits during grouping
        pretty_assertions::assert_eq!(total_commits, original_count);

        // Show top committers
        let mut committer_stats: Vec<_> = committer_commits_map
            .iter()
            .map(|((name, email), commits)| (name, email, commits.len()))
            .collect();
        committer_stats.sort_by(|a, b| b.2.cmp(&a.2));

        let mut git_log_cmd = Command::new("sh");
        git_log_cmd.arg("-c").arg(
            format!(
                "git log --no-mailmap --format='%cn <%ce>' {} | sort -u | wc -l",
                branch_name
            )
            .to_string(),
        );
        let git_log_output = git_log_cmd.output().expect("failed to execute process");
        let value = git_log_output.stdout.to_str().unwrap().trim();
        println!("git log: {}", value);
        pretty_assertions::assert_eq!(committer_stats.len().to_string().as_str(), value);

        // println!("\nTop 10 committers by identity (name + email):");
        for (name, email, count) in committer_stats.iter() {
            println!("  {} <{}> - {} commits", name, email, count);
            let args = format!(
                "git log --no-mailmap --pretty=format:'%cn <%ce>' {} | grep '{} <{}>' | wc -l",
                branch_name,
                name.replace("[", "\\[")
                    .replace("]", "\\]")
                    .into_string()
                    .unwrap(),
                email
                    .replace("[", "\\[")
                    .replace("]", "\\]")
                    .into_string()
                    .unwrap()
            );
            let mut git_log_cmd = Command::new("sh");
            git_log_cmd.arg("-c").arg(args);
            let git_log_output = git_log_cmd.output().expect("failed to execute process");
            let value = git_log_output.stdout.to_str().unwrap().trim();
            println!("git log: {}", value);
            pretty_assertions::assert_eq!(count.to_string().as_str(), value);
        }
    }
}

#[cfg(test)]
mod with_mailmap {
    use crate::git::traverse::util;
    use gix::bstr::ByteSlice;
    use parameterized::parameterized;
    use std::process::Command;

    const USE_MAILMAP: bool = true;

    #[parameterized(branch_name = {
        "origin/main",
        "origin/develop"
    })]
    fn traverse_branch_check_committer_values(branch_name: &str) {
        use gix::bstr::BString;
        use std::collections::HashMap;

        let repo = util::get_demo_repo();
        let main_branch = repo.find_reference(branch_name).unwrap().detach();

        let main_info =
            commits::traversal::main(repo, vec![main_branch.clone()], false, USE_MAILMAP)
                .unwrap()
                .get_key_value(&main_branch)
                .unwrap()
                .1
                .clone();

        // Check the actual length before grouping
        let original_count = main_info.len();
        println!(
            "Original commit count from branch '{}': {}",
            branch_name, original_count
        );

        // Group commits by committer identity (name + email only, ignoring time)
        // Using a tuple of (name, email) as key instead of Sig (which includes time)
        let mut committer_commits_map: HashMap<(BString, BString), Vec<_>> = HashMap::new();

        for gcm in main_info {
            let committer = gcm.committer.clone();
            let key = (committer.name.clone(), committer.email.clone());
            committer_commits_map
                .entry(key)
                .or_insert_with(Vec::new)
                .push(gcm);
        }

        // Count total commits after grouping
        let total_commits: usize = committer_commits_map.values().map(|v| v.len()).sum();
        println!("Total commits after grouping: {}", total_commits);
        println!(
            "Number of unique committer identities: {}",
            committer_commits_map.len()
        );

        // Verify we didn't lose any commits during grouping
        pretty_assertions::assert_eq!(total_commits, original_count);

        // Show top committers
        let mut committer_stats: Vec<_> = committer_commits_map
            .iter()
            .map(|((name, email), commits)| (name, email, commits.len()))
            .collect();
        committer_stats.sort_by(|a, b| b.2.cmp(&a.2));

        let mut git_log_cmd = Command::new("sh");
        git_log_cmd.arg("-c").arg(format!(
            "git log --use-mailmap --format='%cN <%cE>' {} | sort -u | wc -l",
            branch_name
        ));
        let git_log_output = git_log_cmd.output().expect("failed to execute process");
        let value = git_log_output.stdout.to_str().unwrap().trim();
        println!("git log: {}", value);
        pretty_assertions::assert_eq!(committer_stats.len().to_string().as_str(), value);

        // println!("\nTop 10 committers by identity (name + email):");
        for (name, email, count) in committer_stats.iter() {
            println!("  {} <{}> - {} commits", name, email, count);
            let mut git_log_cmd = Command::new("sh");
            git_log_cmd.arg("-c").arg(format!(
                "git log --use-mailmap --pretty=format:'%cN <%cE>' {} | grep '{}' | wc -l",
                branch_name, email
            ));
            let git_log_output = git_log_cmd.output().expect("failed to execute process");
            let value = git_log_output.stdout.to_str().unwrap().trim();
            println!("git log: {}", value);
            pretty_assertions::assert_eq!(count.to_string().as_str(), value);
        }
    }

    #[parameterized(branch_name = {
        "origin/main",
        "origin/develop"
    })]
    fn traverse_branch_check_author_values(branch_name: &str) {
        use gix::bstr::BString;
        use std::collections::HashMap;

        let repo = util::get_demo_repo();
        let main_branch = repo.find_reference(branch_name).unwrap().detach();

        let main_info =
            commits::traversal::main(repo, vec![main_branch.clone()], false, USE_MAILMAP)
                .unwrap()
                .get_key_value(&main_branch)
                .unwrap()
                .1
                .clone();

        // Check the actual length before grouping
        let original_count = main_info.len();
        println!(
            "Original commit count from branch '{}': {}",
            branch_name, original_count
        );

        // Group commits by committer identity (name + email only, ignoring time)
        // Using a tuple of (name, email) as key instead of Sig (which includes time)
        let mut author_commits_map: HashMap<(BString, BString), Vec<_>> = HashMap::new();

        for gcm in main_info {
            let author = gcm.author.clone();
            let key = (author.name.clone(), author.email.clone());
            author_commits_map
                .entry(key)
                .or_insert_with(Vec::new)
                .push(gcm);
        }

        // Count total commits after grouping
        let total_commits: usize = author_commits_map.values().map(|v| v.len()).sum();
        println!("Total commits after grouping: {}", total_commits);
        println!(
            "Number of unique author identities: {}",
            author_commits_map.len()
        );

        // Verify we didn't lose any commits during grouping
        pretty_assertions::assert_eq!(total_commits, original_count);

        // Show top committers
        let mut author_stats: Vec<_> = author_commits_map
            .iter()
            .map(|((name, email), commits)| (name.to_string(), email.to_string(), commits))
            .collect();
        author_stats.sort_by(|a, b| b.2.len().cmp(&a.2.len()));

        let mut git_log_cmd = Command::new("sh");
        git_log_cmd.arg("-c").arg(format!(
            "git log --use-mailmap --format='%aN <%aE>' {} | sort -u | wc -l",
            branch_name
        ));
        let git_log_output = git_log_cmd.output().expect("failed to execute process");
        let value = git_log_output.stdout.to_str().unwrap().trim();
        println!("git log (count all): {}", value);
        pretty_assertions::assert_eq!(author_stats.len().to_string().as_str(), value);

        // println!("\nTop 10 committers by identity (name + email):");
        for (name, email, count) in author_stats.iter() {
            println!("  {} <{}> - {} commits", name, email, count.len());
            let mut git_log_cmd = Command::new("sh");
            git_log_cmd.arg("-c").arg(format!(
                "git log --use-mailmap --pretty=format:'%aN <%aE>' {} | grep '{}' | wc -l",
                branch_name,
                // name.replace("[", "\\[")
                //     .replace("]", "\\]")
                //     .into_string()
                //     .unwrap(),
                email
                    .replace("[", "\\[")
                    .replace("]", "\\]")
            ));
            let git_log_output = git_log_cmd.output().expect("failed to execute process");
            let value = git_log_output.stdout.to_str().unwrap().trim();
            println!("git log: {}", value);
            pretty_assertions::assert_eq!(count.len().to_string().as_str(), value);
        }
    }
}

#[cfg(test)]
mod traversals {
    use std::process::Command;
    use gix::bstr::ByteSlice;
    use crate::git::traverse::util::get_demo_repo;
    use parameterized::parameterized;

    #[parameterized(branch_name = {
        "origin/main",
        "origin/develop",
    })]
    fn traverse_main_branch(branch_name: &str) {
        let repo = get_demo_repo();
        let main_branch = repo.find_reference(branch_name).unwrap().detach();

        let result =
            commits::traversal::main(repo, vec![main_branch.clone()], false, true).unwrap();

        let mut git_revlist_cmd = Command::new("sh");
        git_revlist_cmd.arg("-c").arg(format!(
            "git rev-list --count {}",
            branch_name
        ));
        let git_log_output = git_revlist_cmd.output().expect("failed to execute process");
        let value = git_log_output.stdout.to_str().unwrap().trim();
        println!("git rev-list: {}", value);

        pretty_assertions::assert_eq!(result.len(), 1);
        let main_info = result.get_key_value(&main_branch).unwrap().1.clone();
        pretty_assertions::assert_eq!(main_info.len().to_string(), value);
    }
}
