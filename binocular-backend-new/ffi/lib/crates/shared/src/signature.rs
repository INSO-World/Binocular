use gix::bstr::BString;
use serde::ser::SerializeStruct;

#[derive(Hash, PartialOrd, Ord, Eq, PartialEq, Debug, Clone)]
pub struct Sig {
    pub name: BString,
    pub email: BString,
    pub time: gix::date::Time,
}

impl serde::ser::Serialize for Sig {
    fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>
    where
        S: serde::ser::Serializer,
    {
        // 3 is the number of fields in the struct.
        let mut state = serializer.serialize_struct("Signature", 3)?;
        state.serialize_field("name", &self.name.to_string())?;
        state.serialize_field("email", &self.email.to_string())?;
        state.serialize_field("time", &crate::tz_utils::time_to_utc_with_offset(self.time))?;
        state.end()
    }
}

impl From<gix::actor::Signature> for Sig {
    fn from(gix::actor::Signature { name, email, time }: gix::actor::Signature) -> Self {
        Self { name, email, time }
    }
}

impl From<gix::actor::SignatureRef<'_>> for Sig {
    fn from(value: gix::actor::SignatureRef<'_>) -> Self {
        Self {
            name: BString::from(value.name),
            email: BString::from(value.email),
            time: value.time().expect("Datetime must be present for commit"),
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use gix::actor::{Signature, SignatureRef};
    use gix::bstr::BString;
    use gix::date::Time;
    use std::format;

    // Helper function to create a dummy Time struct
    fn dummy_time() -> Time {
        Time {
            seconds: 1609459200, // Jan 1, 2021, 00:00:00 UTC
            offset: 0,           // UTC offset
        }
    }

    #[test]
    fn test_sig_from_signature() {
        let name = BString::from("John Doe");
        let email = BString::from("john@example.com");
        let time = dummy_time();

        let signature = Signature {
            name: name.clone(),
            email: email.clone(),
            time,
        };

        let sig: Sig = signature.into();

        assert_eq!(sig.name, name);
        assert_eq!(sig.email, email);
        assert_eq!(sig.time, time);
    }

    #[test]
    fn test_sig_from_signature_ref() {
        let name = BString::from("Jane Doe");
        let email = BString::from("jane@example.com");
        let time = dummy_time();

        let signature_ref = SignatureRef {
            name: (&name).as_ref(),
            email: (&email).as_ref(),
            time: &*format!("{}", time.seconds + (time.offset as i64)),
        };

        let sig: Sig = signature_ref.into();

        assert_eq!(sig.name, name);
        assert_eq!(sig.email, email);
        assert_eq!(sig.time, time);
    }

    #[test]
    fn test_sig_equality() {
        let name = BString::from("Alice");
        let email = BString::from("alice@example.com");
        let time = dummy_time();

        let sig1 = Sig {
            name: name.clone(),
            email: email.clone(),
            time,
        };

        let sig2 = Sig { name, email, time };

        assert_eq!(sig1, sig2);
    }

    #[test]
    fn test_sig_ordering() {
        let name1 = BString::from("Charlie");
        let email1 = BString::from("charlie@example.com");
        let time1 = dummy_time();

        let name2 = BString::from("Bob");
        let email2 = BString::from("bob@example.com");
        let time2 = dummy_time();

        let sig1 = Sig {
            name: name1,
            email: email1,
            time: time1,
        };

        let sig2 = Sig {
            name: name2,
            email: email2,
            time: time2,
        };

        assert!(sig1 > sig2); // Ord implemented via name, email, and time
    }

    #[test]
    fn test_sig_debug_format() {
        let name = BString::from("Diane");
        let email = BString::from("diane@example.com");
        let time = dummy_time();

        let sig = Sig {
            name: name.clone(),
            email: email.clone(),
            time,
        };

        let debug_output = format!("{:?}", sig);
        assert!(debug_output.contains("Diane"));
        assert!(debug_output.contains("diane@example.com"));
    }

    #[test]
    fn test_ser_empty() {
        use serde_test::{assert_ser_tokens, Token};

        let name = BString::from("Diane");
        let email = BString::from("diane@example.com");
        let time = dummy_time();

        let sig = Sig {
            name: name.clone(),
            email: email.clone(),
            time,
        };

        assert_ser_tokens(
            &sig,
            &[
                Token::Struct {
                    len: 3,
                    name: "Signature",
                },
                Token::Str("name"),
                Token::String("Diane"),
                Token::Str("email"),
                Token::String("diane@example.com"),
                Token::Str("time"),
                Token::String("2021-01-01T00:00:00Z"),
                Token::StructEnd,
            ],
        );
    }
}
