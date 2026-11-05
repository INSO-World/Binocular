package com.inso_world.binocular.jgit;

import org.eclipse.jgit.lib.PersonIdent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses and applies .mailmap transformations to PersonIdent objects.
 *
 * <h2>Mailmap Format</h2>
 * The .mailmap file supports the following formats:
 * <ul>
 *   <li>{@code Proper Name <proper@email.xx>} - Map any commit with proper@email.xx to Proper Name</li>
 *   <li>{@code <proper@email.xx> <commit@email.xx>} - Map commit@email.xx to proper@email.xx</li>
 *   <li>{@code Proper Name <proper@email.xx> <commit@email.xx>} - Map commit@email.xx to Proper Name + proper@email.xx</li>
 *   <li>{@code Proper Name <proper@email.xx> Commit Name <commit@email.xx>} - Map specific name+email combination</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * # Comments start with #
 * Alice Developer <alice@company.com> <alice@example.com>
 * Alice Developer <alice@company.com> Alice <alice@home.local>
 * Bob Builder <bob@company.com> <bob@example.com>
 * }</pre>
 *
 * @see <a href="https://git-scm.com/docs/gitmailmap">Git mailmap documentation</a>
 */
public class Mailmap {

    private static final Logger logger = LoggerFactory.getLogger(Mailmap.class);

    // Pattern to match email in angle brackets
    private static final Pattern EMAIL_PATTERN = Pattern.compile("<([^>]+)>");

    private final List<MailmapEntry> entries;

    private Mailmap(List<MailmapEntry> entries) {
        this.entries = entries;
    }

    /**
     * Reads and parses a .mailmap file from the repository.
     *
     * @param jgitRepo the JGit repository
     * @return the parsed Mailmap, or null if no .mailmap file exists
     */
    public static Mailmap read(org.eclipse.jgit.lib.Repository jgitRepo) {
        File workTree = jgitRepo.getWorkTree();
        if (workTree == null) {
            logger.debug("No work tree available (bare repository?)");
            return null;
        }

        File mailmapFile = new File(workTree, ".mailmap");
        if (!mailmapFile.exists() || !mailmapFile.isFile()) {
            logger.debug("No .mailmap file found at {}", mailmapFile);
            return null;
        }

        try {
            Mailmap result = parse(mailmapFile);
            logger.debug("Loaded {} mailmap entries from {}", result.entries.size(), mailmapFile);
            return result;
        } catch (IOException e) {
            logger.warn("Failed to read .mailmap file: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Parses a .mailmap file.
     *
     * @param file the mailmap file
     * @return the parsed Mailmap
     * @throws IOException if reading fails
     */
    public static Mailmap parse(File file) throws IOException {
        List<MailmapEntry> entries = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                MailmapEntry entry = parseLine(line);
                if (entry != null) {
                    entries.add(entry);
                }
            }
        }

        logger.debug("Loaded {} mailmap entries", entries.size());
        return new Mailmap(entries);
    }

    /**
     * Parses a single mailmap line.
     *
     * Supported formats:
     * 1. Proper Name &lt;proper@email.xx&gt;
     * 2. &lt;proper@email.xx&gt; &lt;commit@email.xx&gt;
     * 3. Proper Name &lt;proper@email.xx&gt; &lt;commit@email.xx&gt;
     * 4. Proper Name &lt;proper@email.xx&gt; Commit Name &lt;commit@email.xx&gt;
     */
    private static MailmapEntry parseLine(String line) {
        Matcher matcher = EMAIL_PATTERN.matcher(line);
        List<String> emails = new ArrayList<>();
        List<String> originalEmails = new ArrayList<>();
        List<Integer> emailStarts = new ArrayList<>();

        while (matcher.find()) {
            String originalEmail = matcher.group(1);
            emails.add(originalEmail.toLowerCase());
            originalEmails.add(originalEmail);
            emailStarts.add(matcher.start());
        }

        if (emails.isEmpty()) {
            return null;
        }

        String properName = null;
        String properEmail = null;
        String commitName = null;
        String commitEmail = null;

        if (emails.size() == 1) {
            // Format 1: Proper Name <proper@email.xx>
            // Preserve original case for proper email (canonical identity)
            properEmail = originalEmails.get(0);
            if (emailStarts.get(0) > 0) {
                properName = line.substring(0, emailStarts.get(0)).trim();
            }
        } else if (emails.size() >= 2) {
            // Preserve original case for proper email (canonical identity)
            properEmail = originalEmails.get(0);
            // Use lowercased email for matching commit emails
            commitEmail = emails.get(1);

            // Check for proper name before first email
            if (emailStarts.get(0) > 0) {
                properName = line.substring(0, emailStarts.get(0)).trim();
            }

            // Check for commit name between emails
            int endOfFirstEmail = emailStarts.get(0) + originalEmails.get(0).length() + 2; // +2 for < and >
            int startOfSecondEmail = emailStarts.get(1);
            if (startOfSecondEmail > endOfFirstEmail) {
                String between = line.substring(endOfFirstEmail, startOfSecondEmail).trim();
                if (!between.isEmpty()) {
                    commitName = between;
                }
            }
        }

        if (properEmail == null) {
            return null;
        }

        return new MailmapEntry(properName, properEmail, commitName, commitEmail);
    }

    /**
     * Maps a PersonIdent using the mailmap rules.
     *
     * @param ident the original PersonIdent
     * @return the mapped PersonIdent with canonical name/email
     */
    public PersonIdent map(PersonIdent ident) {
        if (ident == null) {
            return null;
        }

        String originalEmail = ident.getEmailAddress();
        String emailLower = originalEmail.toLowerCase();
        String name = ident.getName();

        for (MailmapEntry entry : entries) {
            if (entry.matches(name, emailLower)) {
                String newName = entry.getProperName() != null ? entry.getProperName() : name;
                String newEmail = entry.getProperEmail();

                // Compare against original email (case-sensitive) to preserve mailmap's specified case
                if (!newName.equals(name) || !newEmail.equals(originalEmail)) {
                    logger.trace("Mailmap: {} <{}> -> {} <{}>", name, originalEmail, newName, newEmail);
                    return new PersonIdent(newName, newEmail, ident.getWhenAsInstant(), ident.getZoneId());
                }
                return ident;
            }
        }

        return ident;
    }

    /**
     * A single mailmap entry.
     */
    private static class MailmapEntry {
        private final String properName;
        private final String properEmail;
        private final String commitName;
        private final String commitEmail;

        MailmapEntry(String properName, String properEmail, String commitName, String commitEmail) {
            this.properName = properName;
            this.properEmail = properEmail;
            this.commitName = commitName;
            this.commitEmail = commitEmail;
        }

        /**
         * Checks if this entry matches the given name and email.
         */
        boolean matches(String name, String email) {
            // If commitEmail is specified, it must match
            if (commitEmail != null) {
                if (!commitEmail.equalsIgnoreCase(email)) {
                    return false;
                }
                // If commitName is also specified, it must match too
                if (commitName != null && !commitName.equals(name)) {
                    return false;
                }
                return true;
            }

            // If only properEmail is specified, match by email
            return properEmail.equalsIgnoreCase(email);
        }

        String getProperName() {
            return properName;
        }

        String getProperEmail() {
            return properEmail;
        }
    }
}
