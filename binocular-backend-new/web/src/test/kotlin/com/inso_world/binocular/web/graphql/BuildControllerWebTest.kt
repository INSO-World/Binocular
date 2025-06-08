package com.inso_world.binocular.web.graphql

import com.inso_world.binocular.web.BaseDbTest
import com.inso_world.binocular.web.entity.Build
import com.inso_world.binocular.web.exception.NotFoundException
import com.inso_world.binocular.web.exception.ValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BuildControllerWebTest : BaseDbTest() {

    @Test
    fun `should return all builds`() {
        // Test data is set up in BaseDbTest
        val result = graphQlTester.document("""
            query {
                builds(page: 1, perPage: 100) {
                    arangoId
                    id
                    sha
                    ref
                    status
                    tag
                    user
                    userFullName
                    duration
                    webUrl
                    jobs {
                        id
                        name
                        status
                        stage
                        webUrl
                    }
                }
            }
        """)
        .execute()
        .path("builds")
        .entityList(Build::class.java)

        // Check size
        result.hasSize(2)

        // Get the builds from the result
        val builds = result.get()

        // Check that the builds match the test data
        testBuilds.forEachIndexed { index, expectedBuild ->
            val actualBuild = builds[index]
            assert(actualBuild.arangoId == expectedBuild.arangoId) { "Build arangoId mismatch: expected ${expectedBuild.arangoId}, got ${actualBuild.arangoId}" }
            assert(actualBuild.id == expectedBuild.id) { "Build id mismatch: expected ${expectedBuild.id}, got ${actualBuild.id}" }
            assert(actualBuild.sha == expectedBuild.sha) { "Build sha mismatch: expected ${expectedBuild.sha}, got ${actualBuild.sha}" }
            assert(actualBuild.ref == expectedBuild.ref) { "Build ref mismatch: expected ${expectedBuild.ref}, got ${actualBuild.ref}" }
            assert(actualBuild.status == expectedBuild.status) { "Build status mismatch: expected ${expectedBuild.status}, got ${actualBuild.status}" }
            assert(actualBuild.tag == expectedBuild.tag) { "Build tag mismatch: expected ${expectedBuild.tag}, got ${actualBuild.tag}" }
            assert(actualBuild.user == expectedBuild.user) { "Build user mismatch: expected ${expectedBuild.user}, got ${actualBuild.user}" }
            assert(actualBuild.userFullName == expectedBuild.userFullName) { "Build userFullName mismatch: expected ${expectedBuild.userFullName}, got ${actualBuild.userFullName}" }
            assert(actualBuild.duration == expectedBuild.duration) { "Build duration mismatch: expected ${expectedBuild.duration}, got ${actualBuild.duration}" }
            assert(actualBuild.webUrl == expectedBuild.webUrl) { "Build webUrl mismatch: expected ${expectedBuild.webUrl}, got ${actualBuild.webUrl}" }
            
            // Check jobs
            assert(actualBuild.jobs?.size == expectedBuild.jobs?.size) { "Build jobs size mismatch: expected ${expectedBuild.jobs?.size}, got ${actualBuild.jobs?.size}" }
            actualBuild.jobs?.forEachIndexed { jobIndex, actualJob ->
                val expectedJob = expectedBuild.jobs!![jobIndex]
                assert(actualJob.id == expectedJob.id) { "Job id mismatch: expected ${expectedJob.id}, got ${actualJob.id}" }
                assert(actualJob.name == expectedJob.name) { "Job name mismatch: expected ${expectedJob.name}, got ${actualJob.name}" }
                assert(actualJob.status == expectedJob.status) { "Job status mismatch: expected ${expectedJob.status}, got ${actualJob.status}" }
                assert(actualJob.stage == expectedJob.stage) { "Job stage mismatch: expected ${expectedJob.stage}, got ${actualJob.stage}" }
                assert(actualJob.webUrl == expectedJob.webUrl) { "Job webUrl mismatch: expected ${expectedJob.webUrl}, got ${actualJob.webUrl}" }
            }
        }
    }

    @Test
    fun `should return build by id`() {
        // Test data is set up in BaseDbTest
        val expectedBuild = testBuilds.first { it.arangoId == "1" }

        val result = graphQlTester.document("""
            query {
                build(id: "1") {
                    arangoId
                    id
                    sha
                    ref
                    status
                    tag
                    user
                    userFullName
                    duration
                    webUrl
                    jobs {
                        id
                        name
                        status
                        stage
                        webUrl
                    }
                }
            }
        """)
        .execute()
        .path("build")

        // Check that the build exists
        result.hasValue()

        // Get the build from the result
        val actualBuild = result.entity(Build::class.java).get()

        // Check that the build matches the test data
        assert(actualBuild.arangoId == expectedBuild.arangoId) { "Build arangoId mismatch: expected ${expectedBuild.arangoId}, got ${actualBuild.arangoId}" }
        assert(actualBuild.id == expectedBuild.id) { "Build id mismatch: expected ${expectedBuild.id}, got ${actualBuild.id}" }
        assert(actualBuild.sha == expectedBuild.sha) { "Build sha mismatch: expected ${expectedBuild.sha}, got ${actualBuild.sha}" }
        assert(actualBuild.ref == expectedBuild.ref) { "Build ref mismatch: expected ${expectedBuild.ref}, got ${actualBuild.ref}" }
        assert(actualBuild.status == expectedBuild.status) { "Build status mismatch: expected ${expectedBuild.status}, got ${actualBuild.status}" }
        assert(actualBuild.tag == expectedBuild.tag) { "Build tag mismatch: expected ${expectedBuild.tag}, got ${actualBuild.tag}" }
        assert(actualBuild.user == expectedBuild.user) { "Build user mismatch: expected ${expectedBuild.user}, got ${actualBuild.user}" }
        assert(actualBuild.userFullName == expectedBuild.userFullName) { "Build userFullName mismatch: expected ${expectedBuild.userFullName}, got ${actualBuild.userFullName}" }
        assert(actualBuild.duration == expectedBuild.duration) { "Build duration mismatch: expected ${expectedBuild.duration}, got ${actualBuild.duration}" }
        assert(actualBuild.webUrl == expectedBuild.webUrl) { "Build webUrl mismatch: expected ${expectedBuild.webUrl}, got ${actualBuild.webUrl}" }
        
        // Check jobs
        assert(actualBuild.jobs?.size == expectedBuild.jobs?.size) { "Build jobs size mismatch: expected ${expectedBuild.jobs?.size}, got ${actualBuild.jobs?.size}" }
        actualBuild.jobs?.forEachIndexed { jobIndex, actualJob ->
            val expectedJob = expectedBuild.jobs!![jobIndex]
            assert(actualJob.id == expectedJob.id) { "Job id mismatch: expected ${expectedJob.id}, got ${actualJob.id}" }
            assert(actualJob.name == expectedJob.name) { "Job name mismatch: expected ${expectedJob.name}, got ${actualJob.name}" }
            assert(actualJob.status == expectedJob.status) { "Job status mismatch: expected ${expectedJob.status}, got ${actualJob.status}" }
            assert(actualJob.stage == expectedJob.stage) { "Job stage mismatch: expected ${expectedJob.stage}, got ${actualJob.stage}" }
            assert(actualJob.webUrl == expectedJob.webUrl) { "Job webUrl mismatch: expected ${expectedJob.webUrl}, got ${actualJob.webUrl}" }
        }
    }

    @Test
    fun `should return builds with pagination`() {
        // Test with page=1, perPage=1 (should return only the first build)
        val result = graphQlTester.document("""
            query {
                builds(page: 1, perPage: 1) {
                    arangoId
                    id
                    sha
                    ref
                    status
                }
            }
        """)
        .execute()
        .path("builds")
        .entityList(Build::class.java)

        // Check size
        result.hasSize(1)

        // Get the builds from the result
        val builds = result.get()

        // Check that the build matches the first test build
        val expectedBuild = testBuilds.first()
        val actualBuild = builds.first()
        assert(actualBuild.arangoId == expectedBuild.arangoId) { "Build arangoId mismatch: expected ${expectedBuild.arangoId}, got ${actualBuild.arangoId}" }
        assert(actualBuild.id == expectedBuild.id) { "Build id mismatch: expected ${expectedBuild.id}, got ${actualBuild.id}" }
        assert(actualBuild.sha == expectedBuild.sha) { "Build sha mismatch: expected ${expectedBuild.sha}, got ${actualBuild.sha}" }
        assert(actualBuild.ref == expectedBuild.ref) { "Build ref mismatch: expected ${expectedBuild.ref}, got ${actualBuild.ref}" }
        assert(actualBuild.status == expectedBuild.status) { "Build status mismatch: expected ${expectedBuild.status}, got ${actualBuild.status}" }
    }

    @Test
    fun `should throw exception for non-existent build id`() {
        // Test with a non-existent build ID
        val nonExistentId = "999"

        graphQlTester.document("""
            query {
                build(id: "$nonExistentId") {
                    arangoId
                    id
                    sha
                }
            }
        """)
        .execute()
        .errors()
        .expect { error ->
            error.message?.contains("Build not found with id: $nonExistentId") ?: false
        }
        .verify()
    }

    @Test
    fun `should throw exception for invalid pagination parameters`() {
        // Test with invalid page parameter
        graphQlTester.document("""
            query {
                builds(page: 0, perPage: 10) {
                    arangoId
                    id
                    sha
                }
            }
        """)
        .execute()
        .errors()
        .expect { error ->
            error.message?.contains("Page must be greater than or equal to 1") ?: false
        }
        .verify()

        // Test with invalid perPage parameter
        graphQlTester.document("""
            query {
                builds(page: 1, perPage: 0) {
                    arangoId
                    id
                    sha
                }
            }
        """)
        .execute()
        .errors()
        .expect { error ->
            error.message?.contains("PerPage must be greater than or equal to 1") ?: false
        }
        .verify()
    }

    @Test
    fun `should handle null pagination parameters`() {
        // Test with null page and perPage parameters (should use defaults)
        val result = graphQlTester.document("""
            query {
                builds {
                    arangoId
                    id
                    sha
                }
            }
        """)
        .execute()
        .path("builds")
        .entityList(Build::class.java)

        // Check size (should return all builds with default pagination)
        result.hasSize(2)
    }

    @Test
    fun `should return second page of builds`() {
        // Test with page=2, perPage=1 (should return only the second build)
        val result = graphQlTester.document("""
            query {
                builds(page: 2, perPage: 1) {
                    arangoId
                    id
                    sha
                    ref
                    status
                }
            }
        """)
        .execute()
        .path("builds")
        .entityList(Build::class.java)

        // Check size
        result.hasSize(1)

        // Get the builds from the result
        val builds = result.get()

        // Check that the build matches the second test build
        val expectedBuild = testBuilds[1]
        val actualBuild = builds.first()
        assert(actualBuild.arangoId == expectedBuild.arangoId) { "Build arangoId mismatch: expected ${expectedBuild.arangoId}, got ${actualBuild.arangoId}" }
        assert(actualBuild.id == expectedBuild.id) { "Build id mismatch: expected ${expectedBuild.id}, got ${actualBuild.id}" }
        assert(actualBuild.sha == expectedBuild.sha) { "Build sha mismatch: expected ${expectedBuild.sha}, got ${actualBuild.sha}" }
        assert(actualBuild.ref == expectedBuild.ref) { "Build ref mismatch: expected ${expectedBuild.ref}, got ${actualBuild.ref}" }
        assert(actualBuild.status == expectedBuild.status) { "Build status mismatch: expected ${expectedBuild.status}, got ${actualBuild.status}" }
    }
}
