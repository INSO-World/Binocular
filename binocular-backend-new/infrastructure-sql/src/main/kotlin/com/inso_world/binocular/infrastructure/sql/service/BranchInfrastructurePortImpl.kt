package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IBranchDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.mapper.BranchMapper
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.File
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
internal class BranchInfrastructurePortImpl(
    @Autowired private val branchMapper: BranchMapper,
) : AbstractInfrastructurePort<Branch, BranchEntity, Long>(Long::class),
    BranchInfrastructurePort {
    @Autowired
    private lateinit var branchDao: IBranchDao

    @PostConstruct
    fun init() {
        super.dao = branchDao
        super.mapper = branchMapper
    }

    override fun findFilesByBranchId(branchId: String): List<File> {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: String) {
        TODO("Not yet implemented")
    }
}
