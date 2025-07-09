// package com.inso_world.binocular.cli.persistence.dao.sql
//
// import com.inso_world.binocular.cli.entity.Project
// import com.inso_world.binocular.cli.persistence.dao.sql.interfaces.IProjectDao
// import com.inso_world.binocular.cli.persistence.repository.sql.ProjectRepository
// import com.inso_world.binocular.infrastructure.sql.persistence.dao.SqlDao
// import org.springframework.beans.factory.annotation.Autowired
// import org.springframework.stereotype.Repository
//
// @Repository
// @org.springframework.validation.annotation.Validated
// class ProjectDao(
//    @Autowired
//    private val repo: ProjectRepository,
// ) : SqlDao<Project, Long>(),
//    IProjectDao {
//    init {
//        this.setClazz(Project::class.java)
//        this.setRepository(repo)
//    }
//
//    override fun findByName(name: String): Project? = repo.findByName(name)
// }
