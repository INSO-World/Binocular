// package com.inso_world.binocular.cli.persistence.dao.sql
//
// import com.inso_world.binocular.cli.entity.MergeRequest
// import com.inso_world.binocular.cli.persistence.dao.sql.interfaces.IMergeRequestDao
// import com.inso_world.binocular.cli.persistence.repository.sql.MergeRequestRepository
// import com.inso_world.binocular.infrastructure.sql.persistence.dao.SqlDao
// import org.springframework.beans.factory.annotation.Autowired
// import org.springframework.stereotype.Repository
//
// @Repository
// @org.springframework.validation.annotation.Validated
// class MergeRequestDao(
//    @Autowired private val repo: MergeRequestRepository,
// ) : SqlDao<MergeRequest, Long>(),
//    IMergeRequestDao {
//    init {
//        this.setClazz(MergeRequest::class.java)
//        this.setRepository(repo)
//    }
// }
