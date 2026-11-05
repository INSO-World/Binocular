use gix::actor::SignatureRef;
use gix::mailmap::Snapshot;
use shared::signature::Sig;

pub(crate) fn apply_mailmap(
    gix_sig: Result<SignatureRef<'_>, gix_object::decode::Error>,
    mailmap: &Snapshot,
) -> Option<Sig> {
    match gix_sig {
        Ok(sig) => {
            let mailmapped = mailmap.resolve(sig);
            Some(Sig::from(mailmapped))
        }
        Err(_) => None
    }
    // gix_sig.ok().map(|sig| {
    //     // println!("sig = {:?}", sig);
    //     let mailmapped = mailmap.resolve(sig);
    //     // println!("mailmapped = {:?}", mailmapped);
    //     Sig::from(mailmapped)
    // })
    // gix_sig.ok().map(|sig| Sig::from(sig))
}
