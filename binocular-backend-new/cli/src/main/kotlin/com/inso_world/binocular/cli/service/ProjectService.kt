package com.inso_world.binocular.cli.service

import com.inso_world.binocular.cli.entity.Project
import com.inso_world.binocular.cli.persistence.dao.sql.ProjectDao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProjectService(
    @Autowired private val projectDao: ProjectDao,
) {
    private val logger: Logger = LoggerFactory.getLogger(ProjectService::class.java)

    fun findByName(name: String): Project? = projectDao.findByName(name)

    @Transactional
    fun getOrCreateProject(name: String): Project {
        val find = this.findByName(name)
        if (find == null) {
            logger.info("Project '$name' does not exists, creating new project")
            return this.projectDao.create(
                Project(
                    name = name,
                ),
            )
        } else {
            logger.info("Project '$name' already exists")
            return find
        }
    }

//    @Transactional
//    fun getOrCreate(
//        gitDir: String,
//        p: Project,
//    ): Repository {
//        val find = this.findRepo(gitDir)
//        if (find == null) {
//            logger.info("Repository does not exists, creating new repository")
//            return this.repositoryDao.create(
//                Repository(
//                    name = normalizePath(gitDir),
//                    project = p,
//                ),
//            )
//        } else {
//            logger.debug("Repository already exists, returning existing repository")
//            return find
//        }
//    }
}
