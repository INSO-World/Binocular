package export.validation

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import validation.ShaclValidator

class ShaclValidatorTest {

    private val validator = ShaclValidator()

    @Test
    fun `should return malformed json error for invalid jsonld input`() {
        val malformedJsonLd = """
            {
              "@context": "https://schemas.inso-world.com/binocular/v1/contextTestForGit.jsonld",
              "@id": "https://data.inso-world.com/id/binocular/branch/test/export/123",
              "@type": "BranchExport",
              "branchName": "develop"
        """.trimIndent()

        val report = validator.validate(malformedJsonLd)

        println("----- VALIDATION REPORT -----")
        println("Conforms: ${report.conforms}")
        println("Critical errors: ${report.criticalErrors}")
        println("Warnings: ${report.warnings}")
        println("Raw RDF: ${report.rawRdf}")
        println("-----------------------------")

        assertFalse(report.conforms)
        assertTrue(report.criticalErrors.isNotEmpty())
        assertTrue(report.criticalErrors.first().contains("Malformed JSON"))
    }

    @Test
    fun `should return error when context is missing`() {
        val jsonLdWithoutContext = """
        {
          "@id": "https://data.inso-world.com/id/binocular/branch/test/export/123",
          "@type": "BranchExport",
          "branchName": "develop"
        }
    """.trimIndent()

        val report = validator.validate(jsonLdWithoutContext)

        println("----- VALIDATION REPORT -----")
        println("Conforms: ${report.conforms}")
        println("Critical errors: ${report.criticalErrors}")
        println("Warnings: ${report.warnings}")
        println("Raw RDF: ${report.rawRdf}")
        println("-----------------------------")

        assertFalse(report.conforms)
        assertTrue(report.criticalErrors.isNotEmpty())
        assertTrue(report.criticalErrors.first().contains("Missing @context"))
    }

    @Test
    fun `should return error when context is invalid`() {
        val jsonLdContextInvalid = """
            {
              "@context": "https://schemas.inso-world.com/binocular/123",
              "@id": "https://data.inso-world.com/id/binocular/branch/test/export/123",
              "@type": "BranchExport",
              "branchName": "develop"
            }
        """.trimIndent()

        val report = validator.validate(jsonLdContextInvalid)

        println("----- VALIDATION REPORT -----")
        println("Conforms: ${report.conforms}")
        println("Critical errors: ${report.criticalErrors}")
        println("Warnings: ${report.warnings}")
        println("Raw RDF: ${report.rawRdf}")
        println("-----------------------------")

        assertFalse(report.conforms)
        assertTrue(report.criticalErrors.isNotEmpty())
        assertTrue(report.criticalErrors.first().contains("Invalid @context URL"))
    }

    @Test
    fun `should return error when sha value is invalid`() {
        val jsonLdShaMissing = """
            {
              "@context": "https://schemas.inso-world.com/binocular/v1/contextTestForGit.jsonld",
              "@id": "https://data.inso-world.com/id/binocular/branch/branches%2F15385/export/7cc284f66d37daeb1d6d011137ecd87ba9b317f6",
              "@type": "BranchExport",
              "branchName": "develop",
              "branchId": "branches/15385",
              "commitSha": "abc",
              "commitId": "418617",
              "committerId": "https://data.inso-world.com/id/binocular/programmer/204550"
            }
        """.trimIndent()

        val report = validator.validate(jsonLdShaMissing)

        println("----- VALIDATION REPORT -----")
        println("Conforms: ${report.conforms}")
        println("Critical errors: ${report.criticalErrors}")
        println("Warnings: ${report.warnings}")
        println("Raw RDF: ${report.rawRdf}")
        println("-----------------------------")

        assertFalse(report.conforms)
        assertTrue(report.criticalErrors.isNotEmpty())
        assertTrue(report.criticalErrors.any { it.contains("The export contains an invalid commit SHA. Expected a 40-character hexadecimal Git commit ID.") })
    }

    @Test
    fun `should return error when commit message is missing`() {
        val jsonLdCommitMessageMissing = """
            {
              "@context": "https://schemas.inso-world.com/binocular/v1/contextTestForGit.jsonld",
              "@id": "https://data.inso-world.com/id/binocular/branch/branches%2F15385/export/7cc284f66d37daeb1d6d011137ecd87ba9b317f6",
              "@type": "BranchExport",
              "branchName": "develop",
              "branchId": "branches/15385",
              "commitSha": "7cc284f66d37daeb1d6d011137ecd87ba9b317f6",
              "commitId": "418617",
              "committerId": "https://data.inso-world.com/id/binocular/programmer/204550"
            }
        """.trimIndent()

        val report = validator.validate(jsonLdCommitMessageMissing)

        println("----- VALIDATION REPORT -----")
        println("Conforms: ${report.conforms}")
        println("Critical errors: ${report.criticalErrors}")
        println("Warnings: ${report.warnings}")
        println("Raw RDF: ${report.rawRdf}")
        println("-----------------------------")

        assertFalse(report.conforms)
        assertTrue(report.criticalErrors.isNotEmpty())
        assertTrue(report.criticalErrors.any { it.contains("The export is missing the commit message for the selected branch snapshot.") })
    }

    @Test
    fun `should return warning when commit message is short`() {
        val jsonLdCommitMessageShort = """
            {
              "@context": "https://schemas.inso-world.com/binocular/v1/contextTestForGit.jsonld",
              "@id": "https://data.inso-world.com/id/binocular/branch/branches%2F15385/export/7cc284f66d37daeb1d6d011137ecd87ba9b317f6",
              "@type": "BranchExport",
              "branchName": "develop",
              "branchId": "branches/15385",
              "commitSha": "7cc284f66d37daeb1d6d011137ecd87ba9b317f6",
              "commitId": "418617",
              "committerId": "https://data.inso-world.com/id/binocular/programmer/204550",
              "message": "short"
            }
        """.trimIndent()

        val report = validator.validate(jsonLdCommitMessageShort)

        println("----- VALIDATION REPORT -----")
        println("Conforms: ${report.conforms}")
        println("Critical errors: ${report.criticalErrors}")
        println("Warnings: ${report.warnings}")
        println("Raw RDF: ${report.rawRdf}")
        println("-----------------------------")

        assertTrue(report.warnings.isNotEmpty())
        assertTrue(report.warnings.any { it.contains("The commit message is very short. A more descriptive message would make the export easier to understand.") })
        assertFalse(report.criticalErrors.any { it.contains("The export is missing the commit message for the selected branch snapshot.") })
    }

    @Test
    fun `should return error when programmer reference is a literal`() {
        val jsonLdProgrammerReferenceLiteral = """
            {
              "@context": "https://schemas.inso-world.com/binocular/v1/contextTestForGit.jsonld",
              "@id": "https://data.inso-world.com/id/binocular/branch/branches%2F15385/export/7cc284f66d37daeb1d6d011137ecd87ba9b317f6",
              "@type": "BranchExport",
              "branchName": "develop",
              "branchId": "branches/15385",
              "commitSha": "7cc284f66d37daeb1d6d011137ecd87ba9b317f6",
              "commitId": "418617",
              "committerId": {
                "@value": "not-an-iri"
              },
              "message": "Merge pull request #321 from INSO-World/feature/320\n\n#320 Configure eslint pipeline to check new frontend\n"
            }
        """.trimIndent()

        val report = validator.validate(jsonLdProgrammerReferenceLiteral)

        println("----- VALIDATION REPORT -----")
        println("Conforms: ${report.conforms}")
        println("Critical errors: ${report.criticalErrors}")
        println("Warnings: ${report.warnings}")
        println("Raw RDF: ${report.rawRdf}")
        println("-----------------------------")

        assertFalse(report.conforms)
        assertTrue(report.criticalErrors.isNotEmpty())
        assertTrue(report.criticalErrors.any { it.contains("The export is missing a valid reference to the committer. A linked programmer entry is required here.") })
    }

    @Test
    fun `should return error when version link is missing from an artifact node`() {
        val jsonLdMissingArtifactVersionLink = """
            {
              "@context": "https://schemas.inso-world.com/binocular/v1/contextTestForGit.jsonld",
              "@graph": [
                {
                  "branchName": "develop",
                  "branchId": "branches/15385",
                  "commitSha": "7cc284f66d37daeb1d6d011137ecd87ba9b317f6",
                  "commitId": "418617",
                  "committerId": "https://data.inso-world.com/id/binocular/programmer/204550",
                  "message": "Merge pull request #321 from INSO-World/feature/320",
                  "fileContents": [
                    "https://data.inso-world.com/id/binocular/version/6a67169cf30ae01f5f16bd713add58a83478f32b"
                  ],
                  "_section": "root",
                  "@id": "https://data.inso-world.com/id/binocular/branch/branches%2F15385/export/7cc284f66d37daeb1d6d011137ecd87ba9b317f6",
                  "@type": "BranchExport"
                },
                {
                  "@id": "https://data.inso-world.com/id/binocular/programmer/204550",
                  "@type": "Programmer",
                  "programmerId": "204550"
                },
                {
                  "@id": "https://data.inso-world.com/id/binocular/artifact/README.md",
                  "@type": "FileContent",
                  "_section": "files",
                  "filePath": "README.md"
                },
                {
                  "@id": "https://data.inso-world.com/id/binocular/version/6a67169cf30ae01f5f16bd713add58a83478f32b",
                  "@type": "Content",
                  "_section": "versions",
                  "id": "6a67169cf30ae01f5f16bd713add58a83478f32b",
                  "contentText": "Content omitted for export size"
                }
              ]
            }
        """.trimIndent()

        val report = validator.validate(jsonLdMissingArtifactVersionLink)

        println("----- VALIDATION REPORT -----")
        println("Conforms: ${report.conforms}")
        println("Critical errors: ${report.criticalErrors}")
        println("Warnings: ${report.warnings}")
        println("Raw RDF: ${report.rawRdf}")
        println("-----------------------------")

        assertFalse(report.conforms)
        assertTrue(report.criticalErrors.isNotEmpty())
        assertTrue(report.criticalErrors.any { it.contains("A file entry is missing its link to the exported file content or version.") })
    }

    @Test
    fun `should return error when version control value is missing`() {
        val jsonLdMissingVersionContentValue = """
            {
              "@context": "https://schemas.inso-world.com/binocular/v1/contextTestForGit.jsonld",
              "@graph": [
                {
                  "branchName": "develop",
                  "branchId": "branches/15385",
                  "commitSha": "7cc284f66d37daeb1d6d011137ecd87ba9b317f6",
                  "commitId": "418617",
                  "committerId": "https://data.inso-world.com/id/binocular/programmer/204550",
                  "message": "Merge pull request #321 from INSO-World/feature/320",
                  "fileContents": [
                    "https://data.inso-world.com/id/binocular/version/6a67169cf30ae01f5f16bd713add58a83478f32b"
                  ],
                  "_section": "root",
                  "@id": "https://data.inso-world.com/id/binocular/branch/branches%2F15385/export/7cc284f66d37daeb1d6d011137ecd87ba9b317f6",
                  "@type": "BranchExport"
                },
                {
                  "@id": "https://data.inso-world.com/id/binocular/programmer/204550",
                  "@type": "Programmer",
                  "programmerId": "204550"
                },
                {
                  "@id": "https://data.inso-world.com/id/binocular/artifact/README.md",
                  "@type": "FileContent",
                  "_section": "files",
                  "filePath": "README.md",
                  "content": [
                    "https://data.inso-world.com/id/binocular/version/6a67169cf30ae01f5f16bd713add58a83478f32b"
                  ]
                },
                {
                  "@id": "https://data.inso-world.com/id/binocular/version/6a67169cf30ae01f5f16bd713add58a83478f32b",
                  "@type": "Content",
                  "_section": "versions",
                  "id": "6a67169cf30ae01f5f16bd713add58a83478f32b"
                }
              ]
            }
        """.trimIndent()

        val report = validator.validate(jsonLdMissingVersionContentValue)

        println("----- VALIDATION REPORT -----")
        println("Conforms: ${report.conforms}")
        println("Critical errors: ${report.criticalErrors}")
        println("Warnings: ${report.warnings}")
        println("Raw RDF: ${report.rawRdf}")
        println("-----------------------------")

        assertFalse(report.conforms)
        assertTrue(report.criticalErrors.isNotEmpty())
        assertTrue(report.criticalErrors.any { it.contains("A file content entry is missing its content value. If content is intentionally omitted, an omission marker must still be present.") })
    }
}
