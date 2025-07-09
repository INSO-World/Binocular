// package com.inso_world.binocular.infrastructure.sql.persistence.dao
//
// import com.inso_world.binocular.core.persistence.dao.interfaces.IModuleDao
// import com.inso_world.binocular.infrastructure.sql.persistence.entity.ModuleEntity
// import com.inso_world.binocular.infrastructure.sql.persistence.mapper.ModuleMapper
// import com.inso_world.binocular.infrastructure.sql.persistence.repository.ModuleRepository
// import org.springframework.beans.factory.annotation.Autowired
// import org.springframework.stereotype.Repository
//
// @Repository
// class ModuleDao(
//    @Autowired override val mapper: ModuleMapper,
//    @Autowired override val repository: ModuleRepository,
// ) : MappedSqlDbDao<com.inso_world.binocular.model.Module, ModuleEntity, String>(repository, mapper),
//    IModuleDao {
//    init {
//        this.setClazz(ModuleEntity::class.java)
//    }
// }
