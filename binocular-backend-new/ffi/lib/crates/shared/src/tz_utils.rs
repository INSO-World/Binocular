use gix::date::Time;

pub fn time_to_utc_with_offset(time: Time) -> chrono::DateTime<chrono::Utc> {
    // Adjust the offset based on the sign.
    // Assume that `time::Sign` has variants `Plus` and `Minus`.
    // let offset_seconds = match time.sign {
    //     gix::date::time::Sign::Plus => time.offset,
    //     gix::date::time::Sign::Minus => -time.offset,
    // } as i64;

    // The seconds field is defined as the number of seconds since UNIX epoch in UTC.
    // However, if you need to interpret it as a local time in the given offset,
    // you would normally have had a separate local timestamp.
    // Here, we assume that the `seconds` field is actually the UTC timestamp.
    // So if you already have UTC, you might not need this extra conversion.
    match chrono::DateTime::from_timestamp(time.seconds, 0) {
        None => panic!(
            "Cannot convert seconds '{}' to chrono::DateTime",
            time.seconds
        ),
        Some(dt_utc) => dt_utc,
    }
        // .add(chrono::Duration::seconds(time.offset as i64))
}