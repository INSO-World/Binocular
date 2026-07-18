package com.inso_world.binocular.web.usecases

import com.inso_world.binocular.core.persistence.mapper.context.MappingSession
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.model.metrics.ModuleSizeChangeFrequency
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class GetModuleSizeChangeFrequencyMetric(
    @Autowired private val repoPort: RepositoryInfrastructurePort,
) {
    @MappingSession
    fun execute(
        repoPath: String, since: Long, until: Long, neededModules: List<String>,
    ): List<ModuleSizeChangeFrequency> {
        val repo = repoPort.findByName(repoPath)

        var list = repoPort.findSizeAndChangeFrequencyByModule(repo, since, until, neededModules).toList()

        return list.map {
                ModuleSizeChangeFrequency(
                    module = it.module,
                    loc = it.loc,
                    changeFrequency = it.changeFrequency,
                )
            }.filter { mscf ->  mscf.loc > 0}
    }
}
