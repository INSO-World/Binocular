package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb


import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IModuleDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ModuleEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.AuthorCountMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.CIRateMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.ModuleMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.ModuleSizeCountMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.ModuleRepository
import com.inso_world.binocular.model.metrics.AuthorCountPerModule
import com.inso_world.binocular.model.metrics.CiRatePerModule
import com.inso_world.binocular.model.metrics.ModuleSizeCount
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
internal class ModuleDao
(
    @Autowired private val moduleRepository: ModuleRepository,
    @Autowired private val moduleMapper: ModuleMapper,
    @Autowired private val authorCountMapper: AuthorCountMapper,
    @Autowired private val ciRateMapper: CIRateMapper,
    @Autowired private val moduleSizeCountMapper: ModuleSizeCountMapper
) : MappedArangoDbDao<com.inso_world.binocular.model.Module, ModuleEntity, String>(moduleRepository, moduleMapper),
    IModuleDao {

    override fun countAuthorCommitsByModule(
        neededModules: List<String>,
    ): List<AuthorCountPerModule> {
        return authorCountMapper.toDomainList(moduleRepository.countAuthorCommitsByModule(neededModules))
    }

    override fun findCiErrorRateByModule(
        since: Long,
        until: Long,
        neededModules: List<String>,
    ): List<CiRatePerModule> {
        return ciRateMapper.toDomainList(moduleRepository.ciErrorRateByModule(since, until, neededModules))
    }

    override fun findSizeAndChangeFrequencyByModule(
        since: Long,
        until: Long,
        neededModules: List<String>
    ): List<ModuleSizeCount> {
        return moduleSizeCountMapper.toDomainList(
            moduleRepository.sizeAndChangeFrequencyByModule(since, until, neededModules)
        )
    }
}
