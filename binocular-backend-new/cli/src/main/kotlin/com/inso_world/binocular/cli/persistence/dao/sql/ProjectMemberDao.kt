package com.inso_world.binocular.cli.persistence.dao.sql

import com.inso_world.binocular.cli.entity.ProjectMember
import com.inso_world.binocular.cli.persistence.dao.sql.interfaces.IProjectMemberDao
import com.inso_world.binocular.cli.persistence.repository.sql.ProjectMemberRepository
import com.inso_world.binocular.infrastructure.sql.persistence.dao.SqlDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.util.function.Function
import java.util.function.Function.identity
import java.util.stream.Collectors

@Repository
class ProjectMemberDao(
    @Autowired private val repo: ProjectMemberRepository,
) : SqlDao<ProjectMember, Long>(),
    IProjectMemberDao {
    init {
        this.setClazz(ProjectMember::class.java)
        this.setRepository(repo)
    }

    override fun findAllAsMapUsingStream(keyMapper: Function<in ProjectMember, out String>): Map<String, ProjectMember> =
        this
            .findAllAsStream()
            .collect(Collectors.toMap(keyMapper, identity()))
}
