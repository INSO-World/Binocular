uniffi::setup_scaffolding!();
use std::ffi::CString;
use std::fmt::Formatter;

#[derive(Debug, Default)]
#[derive(uniffi::Object)]
pub struct Point {
    pub x: i32,
    pub y: i32,
}

// Rust function: Adds two numbers
#[no_mangle]
pub extern "C" fn add_numbers(x: i32, y: i32) -> i32 {
    x + y
}
pub mod ffi {
    use crate::Point;

    #[uniffi::export]
    fn add(a: u32, b: u32) -> u32 {
        a + b
    }

    type AnyhowError = anyhow::Error;
    // For interfaces, wrap a unit struct with `#[uniffi::remote]`.
    #[uniffi::remote(Object)]
    pub struct AnyhowError;
    #[uniffi::export]
    fn transform_string(s: String) -> anyhow::Result<String> {
        Ok(format!("hello, {}", s))
    }

    #[uniffi::export]
    fn find_repo(path: String) -> anyhow::Result<Point> {
        println!("Searching for git at '{}'", path);

        let repo = match gix::discover(path) {
            Ok(repo) => repo,
            Err(e) => panic!("{}", e),
        };

        println!("Repo found at {:?}", repo.git_dir());
        println!("Repo common_dir {:?}", repo.git_dir());

        let head = repo.head()?.peel_to_commit_in_place()?;

        println!("{:?}", head.id);

        let p = Point { x: 1, y: 2 };
        println!("{:?}", p);

        Ok(p)
    }
}

#[no_mangle]
pub extern "C" fn hello_world() -> *mut libc::c_char {
    let s = CString::new("hello").unwrap();
    s.into_raw()
}

#[no_mangle]
pub unsafe extern "C" fn find_repo(msg: *const libc::c_char) -> Point {
    let msg_str: &str = std::ffi::CStr::from_ptr(msg).to_str().unwrap_or_else(|e| {
        // crate::log_error("FFI string conversion failed");
        panic!("{:?}", e)
    });

    println!("Searching for git at '{}'", msg_str);

    let repo = match gix::discover(msg_str) {
        Ok(repo) => repo,
        Err(e) => panic!("{}", e),
    };

    println!("Repo found at {:?}", repo.git_dir());

    let head = repo.head().unwrap().peel_to_commit_in_place().unwrap();

    println!("{:?}", head.id);

    let p = Point { x: 1, y: 2 };
    println!("{:?}", p);

    p
}
