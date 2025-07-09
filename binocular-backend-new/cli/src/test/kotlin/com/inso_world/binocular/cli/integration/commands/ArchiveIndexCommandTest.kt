package com.inso_world.binocular.cli.integration.commands

import com.inso_world.binocular.cli.BinocularCommandLineApplication
import com.inso_world.binocular.cli.commands.Archive
import com.inso_world.binocular.core.integration.base.BaseIntegrationTest
import com.inso_world.binocular.core.service.IssueInfrastructurePort
import com.inso_world.binocular.core.service.MergeRequestInfrastructurePort
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.shell.test.autoconfigure.AutoConfigureShell
import org.springframework.shell.test.autoconfigure.AutoConfigureShellTestClient
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.support.TransactionTemplate
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureShell
@AutoConfigureShellTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(
    classes = [BinocularCommandLineApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
)
@ActiveProfiles("archive", "test")
internal class ArchiveIndexCommandTest(
    @Autowired val transactionTemplate: TransactionTemplate,
    @Autowired val archive: Archive,
    @Autowired val issueDao: IssueInfrastructurePort,
    @Autowired val projectDao: ProjectInfrastructurePort,
    @Autowired val mergeRequestDao: MergeRequestInfrastructurePort,
) : BaseIntegrationTest() {
//    @Autowired
//    private lateinit var projectRepository: ProjectRepository

//    @Autowired
//    private lateinit var pmRepository: ProjectMemberRepository

    @AfterEach
    fun tearDown() {
//        projectRepository.deleteAll()
    }

    @ParameterizedTest
    @MethodSource("getSingleTerm")
    fun `single term`(
        path: String,
        noOfProjects: Int,
        noOfIssues: Int,
        noOfMergeRequests: Int,
        noOfProjectMembers: Int,
        noOfIssueTimelogs: Int,
        noOfMrTimelogs: Int,
    ) {
        archive.archive(
            projectName = path,
            rootPath = "/Volumes/External-Backup/Masterarbeit_Data/extract/$path/",
        )

        transactionTemplate.execute {
            val i = 0
            TODO()
            assertAll(
                { assertThat(projectDao.findAll()).hasSize(noOfProjects) },
                { assertThat(issueDao.findAll()).hasSize(noOfIssues) },
//                { assertThat(issueDao.findAll().flatMap { it.timelogs }).hasSize(noOfIssueTimelogs) },
                { assertThat(mergeRequestDao.findAll()).hasSize(noOfMergeRequests) },
//                { assertThat(pmRepository.findAll()).hasSize(noOfProjectMembers) },
//                { assertThat(pmRepository.findByUsername("root")).isNotNull() },
//                { assertThat(pmRepository.findByUsername("root")?.projects).hasSize(noOfProjects) },
//                { assertThat(mergeRequestDao.findAll().flatMap { it.timelogs }).hasSize(noOfMrTimelogs) },
            )
        }
    }

    @ParameterizedTest
    @MethodSource("getAllProjects")
    fun `combine terms`(
        paths: List<String>,
        noOfProjects: Int,
        noOfIssues: Int,
        noOfMergeRequests: Int,
        noOfProjectMembers: Int,
        noOfIssueTimelogs: Int,
        noOfMrTimelogs: Int,
    ) {
        for (p in paths) {
            archive.archive(
                rootPath = "/Volumes/External-Backup/Masterarbeit_Data/extract/$p/",
                projectName = p,
            )
        }

        transactionTemplate.execute {
            assertAll(
                { assertThat(projectDao.findAll()).hasSize(noOfProjects) },
                { assertThat(issueDao.findAll()).hasSize(noOfIssues) },
//                { assertThat(issueDao.findAll().flatMap { it.timelogs }).hasSize(noOfIssueTimelogs) },
                { assertThat(mergeRequestDao.findAll()).hasSize(noOfMergeRequests) },
//                { assertThat(pmRepository.findAll()).hasSize(noOfProjectMembers) },
//                { assertThat(pmRepository.findByUsername("root")).isNotNull() },
//                { assertThat(pmRepository.findByUsername("root")?.projects).hasSize(noOfProjects) },
//                { assertThat(mergeRequestDao.findAll().flatMap { it.timelogs }).hasSize(noOfMrTimelogs) },
            )
        }
    }

    companion object {
        private val singleTermData =
            listOf(
                listOf("2020ss-sepm-pr-group", 41, 5317, 2977, 292, 21250, 896),
                listOf("2020ws-sepm-pr-group", 12, 1338, 560, 100, 4640, 36),
                listOf("2021ss-sepm-pr-group", 34, 4641, 2331, 237, 16623, 850),
                listOf("2021ws-sepm-pr-group", 13, 1126, 786, 104, 5586, 121),
                listOf("2022ss-sepm-pr-group", 29, 3923, 2030, 213, 14165, 695),
                listOf("2022ws-sepm-pr-group", 21, 2564, 1892, 167, 9188, 218),
                listOf("2023ss-sepm-pr-group", 40, 4674, 2986, 286, 15839, 933),
                listOf("2023ws-se-pr-group", 27, 3278, 1554, 197, 10455, 203),
                listOf("2024ws-se-pr-group", 20, 2155, 1719, 148, 7606, 593),
                listOf("2024ss-se-pr-group", 39, 5047, 3789, 281, 15224, 1614),
            )

        private val multipleTermData =
            listOf(
                listOf(listOf("2020ss-sepm-pr-group", "2020ws-sepm-pr-group"), 53, 6655, 3537, 372, 25890, 932),
                listOf(listOf("2020ss-sepm-pr-group", "2021ss-sepm-pr-group"), 75, 9958, 5308, 507, 37873, 1746),
                listOf(listOf("2020ss-sepm-pr-group", "2021ws-sepm-pr-group"), 54, 6443, 3763, 385, 26836, 1017),
                listOf(listOf("2020ss-sepm-pr-group", "2022ss-sepm-pr-group"), 70, 9240, 5007, 493, 35415, 1591),
                listOf(listOf("2020ss-sepm-pr-group", "2022ws-sepm-pr-group"), 62, 7881, 4869, 444, 30438, 1114),
                listOf(listOf("2020ss-sepm-pr-group", "2023ss-sepm-pr-group"), 81, 9991, 5963, 563, 37089, 1829),
                listOf(listOf("2020ss-sepm-pr-group", "2023ws-se-pr-group"), 68, 8595, 4531, 481, 31705, 1099),
                listOf(listOf("2020ss-sepm-pr-group", "2024ws-se-pr-group"), 61, 7472, 4696, 435, 28856, 1489),
                listOf(listOf("2020ss-sepm-pr-group", "2024ss-se-pr-group"), 80, 10364, 6766, 561, 36474, 2510),
                listOf(listOf("2020ws-sepm-pr-group", "2021ss-sepm-pr-group"), 46, 5979, 2891, 312, 21263, 886),
                listOf(listOf("2020ws-sepm-pr-group", "2021ws-sepm-pr-group"), 25, 2464, 1346, 189, 10226, 157),
                listOf(listOf("2020ws-sepm-pr-group", "2022ss-sepm-pr-group"), 41, 5261, 2590, 301, 18805, 731),
                listOf(listOf("2020ws-sepm-pr-group", "2022ws-sepm-pr-group"), 33, 3902, 2452, 253, 13828, 254),
                listOf(listOf("2020ws-sepm-pr-group", "2023ss-sepm-pr-group"), 52, 6012, 3546, 370, 20479, 969),
                listOf(listOf("2020ws-sepm-pr-group", "2023ws-se-pr-group"), 39, 4616, 2114, 285, 15095, 239),
                listOf(listOf("2020ws-sepm-pr-group", "2024ws-se-pr-group"), 32, 3493, 2279, 243, 12246, 629),
                listOf(listOf("2020ws-sepm-pr-group", "2024ss-se-pr-group"), 51, 6385, 4349, 372, 19864, 1650),
                listOf(listOf("2021ss-sepm-pr-group", "2021ws-sepm-pr-group"), 47, 5767, 3117, 323, 22209, 971),
                listOf(listOf("2021ss-sepm-pr-group", "2022ss-sepm-pr-group"), 63, 8564, 4361, 435, 30788, 1545),
                listOf(listOf("2021ss-sepm-pr-group", "2022ws-sepm-pr-group"), 55, 7205, 4223, 384, 25811, 1068),
                listOf(listOf("2021ss-sepm-pr-group", "2023ss-sepm-pr-group"), 74, 9315, 5317, 502, 32462, 1783),
                listOf(listOf("2021ss-sepm-pr-group", "2023ws-se-pr-group"), 61, 7919, 3885, 422, 27078, 1053),
                listOf(listOf("2021ss-sepm-pr-group", "2024ws-se-pr-group"), 54, 6796, 4050, 379, 24229, 1443),
                listOf(listOf("2021ss-sepm-pr-group", "2024ss-se-pr-group"), 73, 9688, 6120, 505, 31847, 2464),
                listOf(listOf("2021ws-sepm-pr-group", "2022ss-sepm-pr-group"), 42, 5049, 2816, 299, 19751, 816),
                listOf(listOf("2021ws-sepm-pr-group", "2022ws-sepm-pr-group"), 34, 3690, 2678, 249, 14774, 339),
                listOf(listOf("2021ws-sepm-pr-group", "2023ss-sepm-pr-group"), 53, 5800, 3772, 368, 21425, 1054),
                listOf(listOf("2021ws-sepm-pr-group", "2023ws-se-pr-group"), 40, 4404, 2340, 287, 16041, 324),
                listOf(listOf("2021ws-sepm-pr-group", "2024ws-se-pr-group"), 33, 3281, 2505, 249, 13192, 714),
                listOf(listOf("2021ws-sepm-pr-group", "2024ss-se-pr-group"), 52, 6173, 4575, 373, 20810, 1735),
                listOf(listOf("2022ss-sepm-pr-group", "2022ws-sepm-pr-group"), 50, 6487, 3922, 349, 23353, 913),
                listOf(listOf("2022ss-sepm-pr-group", "2023ss-sepm-pr-group"), 69, 8597, 5016, 468, 30004, 1628),
                listOf(listOf("2022ss-sepm-pr-group", "2023ws-se-pr-group"), 56, 7201, 3584, 391, 24620, 898),
                listOf(listOf("2022ss-sepm-pr-group", "2024ws-se-pr-group"), 49, 6078, 3749, 354, 21771, 1288),
                listOf(listOf("2022ss-sepm-pr-group", "2024ss-se-pr-group"), 68, 8970, 5819, 476, 29389, 2309),
                listOf(listOf("2022ws-sepm-pr-group", "2023ss-sepm-pr-group"), 61, 7238, 4878, 415, 25027, 1151),
                listOf(listOf("2022ws-sepm-pr-group", "2023ws-se-pr-group"), 48, 5842, 3446, 341, 19643, 421),
                listOf(listOf("2022ws-sepm-pr-group", "2024ws-se-pr-group"), 41, 4719, 3611, 307, 16794, 811),
                listOf(listOf("2022ws-sepm-pr-group", "2024ss-se-pr-group"), 60, 7611, 5681, 426, 24412, 1832),
                listOf(listOf("2023ss-sepm-pr-group", "2023ws-se-pr-group"), 67, 7952, 4540, 453, 26294, 1136),
                listOf(listOf("2023ss-sepm-pr-group", "2024ws-se-pr-group"), 60, 6829, 4705, 425, 23445, 1526),
                listOf(listOf("2023ss-sepm-pr-group", "2024ss-se-pr-group"), 79, 9721, 6775, 543, 31063, 2547),
                listOf(listOf("2023ws-se-pr-group", "2024ws-se-pr-group"), 47, 5433, 3273, 330, 18061, 796),
                listOf(listOf("2023ws-se-pr-group", "2024ss-se-pr-group"), 66, 8325, 5343, 446, 25679, 1817),
                listOf(listOf("2024ws-se-pr-group", "2024ss-se-pr-group"), 59, 7202, 5508, 407, 22830, 2207),
            )

        @JvmStatic
        fun getSingleTerm(): Stream<Arguments> =
            singleTermData
                .map {
                    Arguments.of(
                        it[0] as String,
                        it[1] as Int,
                        it[2] as Int,
                        it[3] as Int,
                        it[4] as Int,
                        it[5] as Int,
                        it[6] as Int,
                    )
                }.stream()

        @JvmStatic
        fun getAllProjects(): Stream<Arguments> {
            return multipleTermData
                .map {
                    Arguments.of(
                        it[0] as List<String>,
                        it[1] as Int,
                        it[2] as Int,
                        it[3] as Int,
                        it[4] as Int,
                        it[5] as Int,
                        it[6] as Int,
                    )
                }.stream()
//            val args = mutableListOf<Arguments>()
//            val data = singleTermData
            // Two-combinations
//            for (i in 0 until data.size) {
//                for (j in i + 1 until data.size) {
//                    val paths = listOf(data[i][0] as String, data[j][0] as String)
//                    val sums = (1..6).map { k -> (data[i][k] as Int) + (data[j][k] as Int) }
//                    args.add(
//                        Arguments.of(
//                            paths,
//                            sums[0],
//                            sums[1],
//                            sums[2],
//                            sums[3],
//                            sums[4],
//                            sums[5],
//                        ),
//                    )
//                }
//            }
            // Three-combinations
//            for (i in 0 until data.size) {
//                for (j in i + 1 until data.size) {
//                    for (k in j + 1 until data.size) {
//                        val paths = listOf(data[i][0] as String, data[j][0] as String, data[k][0] as String)
//                        val sums = (1..6).map { idx -> (data[i][idx] as Int) + (data[j][idx] as Int) + (data[k][idx] as Int) }
//                        args.add(
//                            Arguments.of(
//                                paths,
//                                sums[0],
//                                sums[1],
//                                sums[2],
//                                sums[3],
//                                sums[4],
//                                sums[5],
//                            ),
//                        )
//                    }
//                }
//            }
//            return args.stream()
        }
    }
}
