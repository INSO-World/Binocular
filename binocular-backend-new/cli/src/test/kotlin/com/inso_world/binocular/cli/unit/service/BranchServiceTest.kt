package com.inso_world.binocular.cli.unit.service

import com.inso_world.binocular.cli.service.BranchService
import com.inso_world.binocular.cli.service.CommitService
import com.inso_world.binocular.cli.service.UserService
import com.inso_world.binocular.infrastructure.arangodb.service.BranchInfrastructurePortImpl
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@DisplayName("BranchService")
internal class BranchServiceTest {

    private val branchPort = mock(BranchInfrastructurePortImpl::class.java)
    private val commitService = mock(CommitService::class.java)
    private val userService = mock(UserService::class.java)

    private val branchService = BranchService(branchPort, commitService, userService)

    @Nested
    @DisplayName("Given a branch whose head SHA is missing")
    inner class MissingHeadSha {

        @Test
        @DisplayName("When export data is requested, then a predictable fallback DTO should be returned")
        fun `should return fallback dto when branch head sha is missing`() {
            val branch = mock(Branch::class.java)
            val headCommit = mock(Commit::class.java)

            `when`(branch.name).thenReturn("develop")
            `when`(branch.head).thenReturn(headCommit)
            `when`(headCommit.sha).thenReturn(null)

            `when`(branchPort.findById("branches/missing-sha")).thenReturn(branch)

            val dto = branchService.getBranchExportData(
                "branches/missing-sha",
                "D:/Binocular",
                false,
                false
            )

            println("----- DTO OUTPUT -----")
            println(dto)
            println("----------------------")

            assertAll(
                { assertThat(dto.branchName).isEqualTo("develop") },
                { assertThat(dto.branchId).isEqualTo("branches/missing-sha") },
                { assertThat(dto.commitSha).isEqualTo("No SHA") },
                { assertThat(dto.commitId).isEqualTo("N/A") },
                { assertThat(dto.committerId).isEqualTo("N/A") },
                { assertThat(dto.message).isEqualTo("N/A") },
                { assertThat(dto.fileContents).isEmpty() },
                { assertThat(dto.childrenCommits).isEmpty() }
            )
        }
    }
}
