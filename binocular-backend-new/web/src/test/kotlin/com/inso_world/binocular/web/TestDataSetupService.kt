package com.inso_world.binocular.web

import com.inso_world.binocular.core.integration.base.InfrastructureDataSetup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Service for setting up test data in the database.
 *
 * Thin shim over the adapter-owned [InfrastructureDataSetup]: the infrastructure adapter
 * is the single source of truth for fixture ordering (projects → repositories → other
 * nodes → edges). This service exists so web tests can depend on a stable, profile-
 * independent API for seeding/tearing down fixtures without knowing which adapter is
 * active.
 *
 * ## Why delegation and not duplicate saves?
 * `CommitMapper.toEntity()` (and several siblings) require the owning `Repository`
 * to already be in the per-session `MappingContext`. The `@MappingSession` boundary
 * is one port call, so saving commits before repositories — even inside the same
 * test setup method — throws `IllegalStateException`. Owning that ordering inside
 * the adapter keeps the invariant in one place.
 *
 * @see com.inso_world.binocular.core.integration.base.InfrastructureDataSetup
 */
@Service
internal class TestDataSetupService(
    @Autowired private val infrastructureDataSetup: InfrastructureDataSetup,
) {
    /**
     * Clears all test data from the database by delegating to the active
     * [InfrastructureDataSetup] adapter implementation.
     */
    fun clearAllData() {
        infrastructureDataSetup.teardown()
    }

    /**
     * Sets up test data in the database by delegating to the active
     * [InfrastructureDataSetup] adapter implementation, which owns the correct
     * entity-creation ordering and relationship/edge wiring.
     */
    fun setupTestData() {
        infrastructureDataSetup.setup()
    }
}
