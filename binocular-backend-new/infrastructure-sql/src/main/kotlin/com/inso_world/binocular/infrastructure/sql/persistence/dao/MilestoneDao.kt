// package com.inso_world.binocular.infrastructure.sql.persistence.dao
//
// import com.inso_world.binocular.core.persistence.dao.interfaces.IMilestoneDao
// import com.inso_world.binocular.infrastructure.sql.persistence.entity.MilestoneEntity
// import com.inso_world.binocular.infrastructure.sql.persistence.mapper.MilestoneMapper
// import com.inso_world.binocular.infrastructure.sql.persistence.repository.MilestoneRepository
// import com.inso_world.binocular.model.Milestone
// import org.springframework.beans.factory.annotation.Autowired
// import org.springframework.stereotype.Repository
//
// @Repository
// class MilestoneDao(
//    @Autowired override val mapper: MilestoneMapper,
//    @Autowired override val repository: MilestoneRepository,
// ) : MappedSqlDbDao<Milestone, MilestoneEntity, String>(repository, mapper),
//    IMilestoneDao {
//    init {
//        this.setClazz(MilestoneEntity::class.java)
//    }
// }
