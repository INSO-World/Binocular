#[cfg(not(test))]
fn available_threads() -> usize {
    std::thread::available_parallelism()
        .map(|p| p.get())
        .unwrap_or(1)
}

#[cfg(test)]
fn available_threads() -> usize {
    8
}

pub(crate) fn num_threads(max_threads: usize) -> usize {
    available_threads().min(max_threads).max(1)
}

#[cfg(test)]
macro_rules! num_threads_tests {
    ($($name:ident: $value:expr,)*) => {
    $(
        #[test]
        fn $name() {
            let (input, expected) = $value;
            assert_eq!(
                expected,
                num_threads(input),
                "When max_threads ({}) is, the function should return ({}).",
                input,
                expected
            );
        }
    )*
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    num_threads_tests! {
        num_threads_0: (0, 1),
        num_threads_1: (1, 1),
        num_threads_2: (2, 2),
        num_threads_3: (3, 3),
        num_threads_4: (4, 4),
        num_threads_5: (5, 5),
        num_threads_6: (6, 6),
        num_threads_7: (7,7),
        num_threads_8: (8,8),
        num_threads_9: (9,8),
        num_threads_10: (10,8),
    }
}
