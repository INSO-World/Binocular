package com.inso_world.binocular.web.usecases

import com.inso_world.binocular.core.persistence.mapper.context.MappingSession
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.model.enums.Granularity
import com.inso_world.binocular.model.metrics.BusFactorCIErrorRate
import com.inso_world.binocular.model.metrics.FileComplexityMinorContributors
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneOffset

@Service
class FileComplexityMinorContributorsMetric (
    @Autowired private val repoPort: RepositoryInfrastructurePort,
) {
    @MappingSession
    fun execute(
        repoPath: String
    ): List<FileComplexityMinorContributors> {

        val repo = repoPort.findByName(repoPath)

        val files = repoPort.findFileComplexityForAllFiles(repo).toList()

        return files.filter {
            file -> file.loc > 0
        }
    }
}
