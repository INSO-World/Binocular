package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.service.MilestoneInfrastructurePort
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import com.inso_world.binocular.model.Milestone
import com.inso_world.binocular.model.Project
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired

/**
 * Integration tests for Milestone persistence via MilestoneInfrastructurePort.
 *
 * Since #454 a milestone always belongs to a project, expressed by the mandatory
 * [Milestone.project] id, so a project has to exist before a milestone can be created.
 *
 * Note: `MilestoneMapper.toEntity` does not write `project` yet, so the project reference does
 * not survive a round trip. That open drift is reported by `DomainModelAlignmentTest` and is
 * therefore not asserted here.
 */
internal class MilestoneTest : BaseInfrastructureSpringTest() {
    @Autowired
    lateinit var milestonePort: MilestoneInfrastructurePort

    @Autowired
    lateinit var projectPort: ProjectInfrastructurePort

    private lateinit var project: Project

    @BeforeEach
    fun setup() {
        project = projectPort.create(Project(name = "proj-milestone-001"))
    }

    @Test
    fun `create milestone and load it by iid`() {
        val ms = Milestone(title = "1.0 Release", state = "active", project = project.iid)
        val created = milestonePort.create(ms)
        assertNotNull(created.id)

        val loaded = milestonePort.findByIid(created.iid)
        assertNotNull(loaded)
        loaded!!
        assertAll(
            { assertEquals(created.id, loaded.id, "id") },
            { assertEquals(ms.title, loaded.title, "title") },
            { assertEquals(ms.state, loaded.state, "state") },
            { assertEquals(ms.iid, loaded.iid, "iid") },
        )
    }

    @Test
    fun `findAll returns created milestones`() {
        val first = milestonePort.create(Milestone(title = "M1", platformIid = 1, project = project.iid))
        val second = milestonePort.create(Milestone(title = "M2", platformIid = 2, project = project.iid))

        val iids = milestonePort.findAll().map { it.iid }.toSet()
        assertThat(iids).contains(first.iid, second.iid)
    }
}
