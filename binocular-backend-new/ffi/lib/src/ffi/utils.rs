/// Simple test function for FFI connectivity
#[uniffi::export]
pub fn hello() {
    println!("Hello, world!");
}