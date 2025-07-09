// package com.inso_world.binocular.infrastructure.sql.persistence.dao
//
// import com.inso_world.binocular.core.persistence.dao.interfaces.INoteDao
// import com.inso_world.binocular.infrastructure.sql.persistence.entity.NoteEntity
// import com.inso_world.binocular.infrastructure.sql.persistence.mapper.NoteMapper
// import com.inso_world.binocular.infrastructure.sql.persistence.repository.NoteRepository
// import com.inso_world.binocular.model.Note
// import org.springframework.beans.factory.annotation.Autowired
// import org.springframework.stereotype.Repository
//
// /**
// * SQL implementation of INoteDao.
// */
// @Repository
// class NoteDao(
//    @Autowired override val mapper: NoteMapper,
//    @Autowired override val repository: NoteRepository,
// ) : MappedSqlDbDao<Note, NoteEntity, String>(repository, mapper),
//    INoteDao {
//    init {
//        this.setClazz(NoteEntity::class.java)
//    }
// }
