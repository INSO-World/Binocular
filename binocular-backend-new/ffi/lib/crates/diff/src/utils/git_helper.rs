use crate::objects::{ChangeType, Entry, FileDiff};
use gix::diff::blob::platform::resource::Data;
use gix::object::tree::diff::Change;


pub fn calculate_changes(
    previous: &gix::Tree,
    current: &gix::Tree,
    rewrite_cache: &mut gix::diff::blob::Platform,
    diff_cache: &mut gix::diff::blob::Platform,
) -> Vec<FileDiff> {
    let change_map = gitoxide_diff_calculation(previous, current, rewrite_cache, diff_cache);

    change_map
}

fn gitoxide_diff_calculation(
    previous: &gix::Tree<'_>,
    current: &gix::Tree<'_>,
    _rewrite_cache: &mut gix::diff::blob::Platform,
    diff_cache: &mut gix::diff::blob::Platform,
) -> Vec<FileDiff> {
    let mut platform = previous.changes().unwrap();
    

    // let source_tree = current.iter()
    //     .filter_map(|res| res.ok().map(|entry| entry.inner)) // dropping errors silently
    //     // .filter(|entry| !args.tree_only || (entry.mode.is_tree()))
    //     .map(|entry| Entry::new(entry.mode, entry.oid.to_owned(), entry.filename.to_owned()))
    //     .collect::<Vec<_>>();

    // println!("{:?}", current.id);
    // for t in tree {
    //     println!("{:?}", t);
    // }
    // println!("++++++++++++++++++++++++++++++++++++++++++++++++++");

    let opts_fn = |opts: &mut gix::diff::Options| {
        opts.track_path();
        // opts.track_filename();
    };

    let mut change_map = Vec::with_capacity(32);

    let _outcome = platform
        .options(opts_fn)
        .for_each_to_obtain_tree(
            &current,
            // rewrite_cache,
            |change| -> Result<_, gix::object::blob::diff::init::Error> {
                let change_type = match change {
                    Change::Addition { location, .. } => ChangeType::Addition {
                        location: location.to_owned(),
                    },
                    Change::Deletion { location, .. } => ChangeType::Deletion {
                        location: location.to_owned(),
                    },
                    Change::Modification { location, .. } => ChangeType::Modification {
                        location: location.to_owned(),
                    },
                    Change::Rewrite {
                        source_location,
                        location,
                        copy,
                        ..
                    } => ChangeType::Rewrite {
                        source_location: source_location.to_owned(),
                        location: location.to_owned(),
                        copy,
                    },
                };

                
                
                if let Ok(cache) = change.diff(diff_cache).map(|p| p.resource_cache) {
                    if let Ok(prep) = cache.prepare_diff() {
                        let old_file_content = match prep.old.data {
                            Data::Missing => None,
                            Data::Buffer { buf,  .. } => {
                                String::from_utf8(buf.to_vec()).ok()
                            }
                            Data::Binary { .. } => None
                        };
                        let new_file_content = match prep.new.data {
                            Data::Missing => None,
                            Data::Buffer { buf, is_derived, .. } => {
                                String::from_utf8(buf.to_vec()).ok()       
                            }
                            Data::Binary { .. } => None
                        };
                        
                        let tokens = prep.interned_input();
                        match prep.operation {
                            gix::diff::blob::platform::prepare_diff::Operation::InternalDiff {
                                algorithm,
                            } => {
                                let counts = gix::diff::blob::diff(
                                    algorithm,
                                    &tokens,
                                    gix::diff::blob::sink::Counter::default(),
                                );
                                // println!("change {:?}\t|\t{:?}|{:?}:\t{:?}", change.location, counts.insertions, counts.removals, counts.removals + counts.insertions);
                                // println!(" {:?}", change.location);
                                // *change_map.entry(change.location().into()).or_insert((
                                //     u32::MIN,
                                //     u32::MIN,
                                //     change_kind.clone(),
                                // )) = (counts.insertions, counts.removals, change_kind.clone());

                                change_map.push(FileDiff {
                                    insertions: counts.insertions,
                                    deletions: counts.removals,
                                    change: change_type,
                                    old_file_content,
                                    new_file_content,
                                })
                            }
                            _ => (),
                        }
                    }
                }

                Ok(gix::object::tree::diff::Action::Continue)
            },
        )
        .unwrap();
    // println!("outcome: {:?}", _outcome);
    change_map
}
