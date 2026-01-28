package com.inso_world.binocular.jgit;

import org.eclipse.jgit.lib.PersonIdent;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Mailmap}.
 * Tests parsing of .mailmap files and identity mapping.
 */
@Tag("unit")
class MailmapTest {

    @TempDir
    Path tempDir;

    @Nested
    class ParseOperation {

        @Test
        void parse_emptyFile_returnsEmptyMailmap() throws IOException {
            Path mailmapFile = tempDir.resolve(".mailmap");
            Files.writeString(mailmapFile, "", StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            assertNotNull(mailmap, "Should return non-null Mailmap for empty file");
        }

        @Test
        void parse_commentOnlyFile_returnsEmptyMailmap() throws IOException {
            Path mailmapFile = tempDir.resolve(".mailmap");
            Files.writeString(mailmapFile, "# This is a comment\n# Another comment\n", StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            // Verify by mapping - should return unchanged identity
            PersonIdent ident = createIdent("Test User", "test@example.com");
            PersonIdent mapped = mailmap.map(ident);
            assertEquals(ident, mapped, "Identity should be unchanged with comment-only mailmap");
        }

        @Test
        void parse_blankLines_areIgnored() throws IOException {
            Path mailmapFile = tempDir.resolve(".mailmap");
            Files.writeString(mailmapFile, "\n\n   \n\n", StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            assertNotNull(mailmap);
        }

        @Test
        void parse_format1_properNameAndEmail() throws IOException {
            // Format: Proper Name <proper@email.xx>
            Path mailmapFile = tempDir.resolve(".mailmap");
            Files.writeString(mailmapFile, "Alice Developer <alice@company.com>\n", StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            PersonIdent original = createIdent("alice", "alice@company.com");
            PersonIdent mapped = mailmap.map(original);

            assertEquals("Alice Developer", mapped.getName(), "Name should be mapped to proper name");
            assertEquals("alice@company.com", mapped.getEmailAddress(), "Email should be preserved");
        }

        @Test
        void parse_format2_emailOnlyMapping() throws IOException {
            // Format: <proper@email.xx> <commit@email.xx>
            Path mailmapFile = tempDir.resolve(".mailmap");
            Files.writeString(mailmapFile, "<alice@company.com> <alice@home.local>\n", StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            PersonIdent original = createIdent("Alice", "alice@home.local");
            PersonIdent mapped = mailmap.map(original);

            assertEquals("Alice", mapped.getName(), "Name should be unchanged");
            assertEquals("alice@company.com", mapped.getEmailAddress(), "Email should be mapped");
        }

        @Test
        void parse_format3_nameAndEmailToEmail() throws IOException {
            // Format: Proper Name <proper@email.xx> <commit@email.xx>
            Path mailmapFile = tempDir.resolve(".mailmap");
            Files.writeString(mailmapFile, "Alice Developer <alice@company.com> <alice@home.local>\n", StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            PersonIdent original = createIdent("Alice Home", "alice@home.local");
            PersonIdent mapped = mailmap.map(original);

            assertEquals("Alice Developer", mapped.getName(), "Name should be mapped");
            assertEquals("alice@company.com", mapped.getEmailAddress(), "Email should be mapped");
        }

        @Test
        void parse_format4_fullMapping() throws IOException {
            // Format: Proper Name <proper@email.xx> Commit Name <commit@email.xx>
            Path mailmapFile = tempDir.resolve(".mailmap");
            Files.writeString(mailmapFile, "Alice Developer <alice@company.com> Alice Home <alice@home.local>\n", StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            // Should match specific name+email combo
            PersonIdent original = createIdent("Alice Home", "alice@home.local");
            PersonIdent mapped = mailmap.map(original);

            assertEquals("Alice Developer", mapped.getName(), "Name should be mapped");
            assertEquals("alice@company.com", mapped.getEmailAddress(), "Email should be mapped");
        }

        @Test
        void parse_format4_doesNotMatchDifferentName() throws IOException {
            // Format 4 requires both name AND email to match
            Path mailmapFile = tempDir.resolve(".mailmap");
            Files.writeString(mailmapFile, "Alice Developer <alice@company.com> Alice Home <alice@home.local>\n", StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            // Different name, same email - should NOT match format 4
            PersonIdent original = createIdent("Alice Work", "alice@home.local");
            PersonIdent mapped = mailmap.map(original);

            assertEquals("Alice Work", mapped.getName(), "Name should NOT be mapped when commit name doesn't match");
        }

        @Test
        void parse_multipleEntries() throws IOException {
            Path mailmapFile = tempDir.resolve(".mailmap");
            String content = """
                    Alice Developer <alice@company.com> <alice@old.com>
                    Bob Builder <bob@company.com> <bob@old.com>
                    """;
            Files.writeString(mailmapFile, content, StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            PersonIdent alice = createIdent("alice", "alice@old.com");
            PersonIdent bob = createIdent("bob", "bob@old.com");

            assertEquals("Alice Developer", mailmap.map(alice).getName());
            assertEquals("Bob Builder", mailmap.map(bob).getName());
        }

        @Test
        void parse_lineWithNoEmail_isSkipped() throws IOException {
            Path mailmapFile = tempDir.resolve(".mailmap");
            Files.writeString(mailmapFile, "Just a name without email\n", StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            // Should not crash, should return functional mailmap
            assertNotNull(mailmap);
        }

        @Test
        void parse_mixedContent() throws IOException {
            Path mailmapFile = tempDir.resolve(".mailmap");
            String content = """
                    # Comment line
                    Alice <alice@company.com> <alice@old.com>

                    # Another comment
                    Bob <bob@company.com>
                    Invalid line without email
                    """;
            Files.writeString(mailmapFile, content, StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            assertNotNull(mailmap, "Should parse mixed content without errors");
        }
    }

    @Nested
    class MapOperation {

        @Test
        void map_nullIdent_returnsNull() throws IOException {
            Path mailmapFile = tempDir.resolve(".mailmap");
            Files.writeString(mailmapFile, "Test <test@test.com>\n", StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            assertNull(mailmap.map(null), "Mapping null should return null");
        }

        @Test
        void map_noMatchingEntry_returnsOriginal() throws IOException {
            Path mailmapFile = tempDir.resolve(".mailmap");
            Files.writeString(mailmapFile, "Alice <alice@company.com> <alice@old.com>\n", StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            PersonIdent original = createIdent("Bob", "bob@example.com");
            PersonIdent mapped = mailmap.map(original);

            assertSame(original, mapped, "Non-matching identity should return same instance");
        }

        @Test
        void map_caseInsensitiveEmailMatch() throws IOException {
            Path mailmapFile = tempDir.resolve(".mailmap");
            Files.writeString(mailmapFile, "Alice <alice@company.com> <ALICE@OLD.COM>\n", StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            PersonIdent original = createIdent("alice", "alice@old.com");
            PersonIdent mapped = mailmap.map(original);

            assertEquals("Alice", mapped.getName(), "Email matching should be case-insensitive");
        }

        @Test
        void map_preservesTimestamp() throws IOException {
            Path mailmapFile = tempDir.resolve(".mailmap");
            Files.writeString(mailmapFile, "Alice Developer <alice@company.com> <alice@old.com>\n", StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            Instant timestamp = Instant.parse("2024-01-15T10:30:00Z");
            ZoneId zone = ZoneId.of("Europe/Vienna");
            PersonIdent original = new PersonIdent("alice", "alice@old.com", timestamp, zone);
            PersonIdent mapped = mailmap.map(original);

            assertEquals(timestamp, mapped.getWhenAsInstant(), "Timestamp should be preserved");
            assertEquals(zone, mapped.getZoneId(), "Timezone should be preserved");
        }

        @Test
        void map_identityAlreadyMatchesProper_returnsSameIdent() throws IOException {
            Path mailmapFile = tempDir.resolve(".mailmap");
            Files.writeString(mailmapFile, "Alice Developer <alice@company.com>\n", StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            // Identity already matches the proper name/email
            PersonIdent original = createIdent("Alice Developer", "alice@company.com");
            PersonIdent mapped = mailmap.map(original);

            assertSame(original, mapped, "Already-matching identity should return same instance");
        }

        @Test
        void map_onlyEmailChanges_returnsNewIdent() throws IOException {
            Path mailmapFile = tempDir.resolve(".mailmap");
            Files.writeString(mailmapFile, "<alice@company.com> <alice@old.com>\n", StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            PersonIdent original = createIdent("Alice", "alice@old.com");
            PersonIdent mapped = mailmap.map(original);

            assertNotSame(original, mapped, "Should return new instance when email changes");
            assertEquals("Alice", mapped.getName(), "Name should be preserved");
            assertEquals("alice@company.com", mapped.getEmailAddress(), "Email should change");
        }

        @Test
        void map_onlyNameChanges_returnsNewIdent() throws IOException {
            Path mailmapFile = tempDir.resolve(".mailmap");
            Files.writeString(mailmapFile, "Alice Developer <alice@company.com>\n", StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            PersonIdent original = createIdent("alice", "alice@company.com");
            PersonIdent mapped = mailmap.map(original);

            assertNotSame(original, mapped, "Should return new instance when name changes");
            assertEquals("Alice Developer", mapped.getName(), "Name should change");
            assertEquals("alice@company.com", mapped.getEmailAddress(), "Email should be preserved");
        }

        @Test
        void map_firstMatchingEntryWins() throws IOException {
            Path mailmapFile = tempDir.resolve(".mailmap");
            String content = """
                    First Match <first@company.com> <alice@old.com>
                    Second Match <second@company.com> <alice@old.com>
                    """;
            Files.writeString(mailmapFile, content, StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            PersonIdent original = createIdent("alice", "alice@old.com");
            PersonIdent mapped = mailmap.map(original);

            assertEquals("First Match", mapped.getName(), "First matching entry should win");
        }
    }

    @Nested
    class BoundaryConditions {

        @Test
        void parse_singleEmail_format1() throws IOException {
            // Tests emails.size() == 1 boundary
            Path mailmapFile = tempDir.resolve(".mailmap");
            Files.writeString(mailmapFile, "Alice <alice@company.com>\n", StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            PersonIdent original = createIdent("alice", "alice@company.com");
            PersonIdent mapped = mailmap.map(original);

            assertEquals("Alice", mapped.getName());
        }

        @Test
        void parse_twoEmails_boundary() throws IOException {
            // Tests emails.size() >= 2 boundary (exactly 2 emails)
            Path mailmapFile = tempDir.resolve(".mailmap");
            Files.writeString(mailmapFile, "<proper@company.com> <commit@old.com>\n", StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            PersonIdent original = createIdent("anyone", "commit@old.com");
            PersonIdent mapped = mailmap.map(original);

            assertEquals("proper@company.com", mapped.getEmailAddress());
        }

        @Test
        void parse_threeEmails_boundary() throws IOException {
            // Tests emails.size() >= 2 with 3 emails (second email used as commit email)
            Path mailmapFile = tempDir.resolve(".mailmap");
            Files.writeString(mailmapFile, "<proper@company.com> <commit@old.com> <ignored@third.com>\n", StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            PersonIdent original = createIdent("anyone", "commit@old.com");
            PersonIdent mapped = mailmap.map(original);

            // Should match second email
            assertEquals("proper@company.com", mapped.getEmailAddress());
        }

        @Test
        void parse_emailAtLineStart_noPoperName() throws IOException {
            // Tests emailStarts.get(0) > 0 boundary (email at position 0)
            Path mailmapFile = tempDir.resolve(".mailmap");
            Files.writeString(mailmapFile, "<alice@company.com>\n", StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            PersonIdent original = createIdent("Alice", "alice@company.com");
            PersonIdent mapped = mailmap.map(original);

            // No proper name was specified, so name should remain unchanged
            assertEquals("Alice", mapped.getName());
        }

        @Test
        void parse_emailsAdjacent_noCommitName() throws IOException {
            // Tests startOfSecondEmail > endOfFirstEmail boundary
            // When emails are adjacent with no space between
            Path mailmapFile = tempDir.resolve(".mailmap");
            Files.writeString(mailmapFile, "<proper@company.com><commit@old.com>\n", StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            PersonIdent original = createIdent("anyone", "commit@old.com");
            PersonIdent mapped = mailmap.map(original);

            assertEquals("proper@company.com", mapped.getEmailAddress());
        }
    }

    @Nested
    class MatchesBehavior {

        @Test
        void map_format1_doesNotMatchDifferentEmail() throws IOException {
            // Tests matches() method - when only properEmail is specified and emails don't match
            Path mailmapFile = tempDir.resolve(".mailmap");
            Files.writeString(mailmapFile, "Alice <alice@company.com>\n", StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            // Email doesn't match the proper email in the mailmap
            PersonIdent original = createIdent("Bob", "bob@example.com");
            PersonIdent mapped = mailmap.map(original);

            // Should NOT be mapped since emails don't match
            assertSame(original, mapped, "Should not map when email doesn't match");
        }

        @Test
        void map_format3_matchesOnlyWithCorrectEmail() throws IOException {
            // Format 3 with commit email - should only match when commit email matches
            Path mailmapFile = tempDir.resolve(".mailmap");
            Files.writeString(mailmapFile, "Alice Dev <alice@company.com> <alice@old.com>\n", StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            // Different email - should NOT match
            PersonIdent original = createIdent("Alice", "different@email.com");
            PersonIdent mapped = mailmap.map(original);

            assertSame(original, mapped, "Should not map when commit email doesn't match");
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void parse_emailWithSpecialCharacters() throws IOException {
            Path mailmapFile = tempDir.resolve(".mailmap");
            Files.writeString(mailmapFile, "Test User <test+special@example.com>\n", StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            PersonIdent original = createIdent("test", "test+special@example.com");
            PersonIdent mapped = mailmap.map(original);

            assertEquals("Test User", mapped.getName());
        }

        @Test
        void parse_preservesEmailCase_inProperEmail() throws IOException {
            Path mailmapFile = tempDir.resolve(".mailmap");
            Files.writeString(mailmapFile, "Alice <Alice.Developer@Company.COM> <alice@old.com>\n", StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            PersonIdent original = createIdent("alice", "alice@old.com");
            PersonIdent mapped = mailmap.map(original);

            // The proper email case should be preserved from the mailmap file
            assertEquals("Alice.Developer@Company.COM", mapped.getEmailAddress(),
                    "Proper email case from mailmap should be preserved");
        }

        @Test
        void parse_handlesWhitespace() throws IOException {
            Path mailmapFile = tempDir.resolve(".mailmap");
            Files.writeString(mailmapFile, "  Alice Developer  <alice@company.com>  <alice@old.com>  \n", StandardCharsets.UTF_8);

            Mailmap mailmap = Mailmap.parse(mailmapFile.toFile());

            PersonIdent original = createIdent("alice", "alice@old.com");
            PersonIdent mapped = mailmap.map(original);

            assertEquals("Alice Developer", mapped.getName(), "Should handle surrounding whitespace");
        }
    }

    // Helper method to create PersonIdent with current time
    private PersonIdent createIdent(String name, String email) {
        return new PersonIdent(name, email, Instant.now(), ZoneId.systemDefault());
    }
}