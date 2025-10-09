// A procmacro as an error
#[derive(Debug, uniffi::Object, thiserror::Error)]
#[uniffi::export(Debug, Display)]
pub struct ProcErrorInterface {
    pub e: String,
}

#[uniffi::export]
impl ProcErrorInterface {
    fn message(&self) -> String {
        self.e.clone()
    }
}

impl std::fmt::Display for ProcErrorInterface {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "ProcErrorInterface({})", self.e)
    }
}