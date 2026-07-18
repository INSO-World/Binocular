package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node

import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.IDao
import com.inso_world.binocular.model.Module
import com.inso_world.binocular.model.metrics.AuthorCountPerModule
import com.inso_world.binocular.model.metrics.CiRatePerModule
import com.inso_world.binocular.model.metrics.ModuleSizeCount

internal interface IModuleDao : IDao<Module, String> {

    /**
     * Counts commits per author for each module, over the WHOLE history (no time filter).
     *
     * The result includes every author, so callers can work out each author's share of a module.
     * This is the input for the per-module bus factor.
     *
     * @param neededModules module paths to include; an empty list means "all modules"
     * @return one entry per (module, author); never null, may be empty
     */
    fun countAuthorCommitsByModule(neededModules: List<String>): List<AuthorCountPerModule>

    /**
     * Aggregates CI build results (failed / completed) per module within a time window.
     *
     * A build counts here only if its status is "failed" or "success"; each build is counted once,
     * even if it is linked to several commits of the same module. Modules without builds in the
     * window are not returned.
     *
     * @param since         start of the time window (epoch millis, UTC)
     * @param until         end of the time window (epoch millis, UTC)
     * @param neededModules module paths to include; an empty list means "all modules"
     * @return one entry per module that has builds in the window; never null, may be empty
     */
    fun findCiErrorRateByModule(since: Long, until: Long, neededModules: List<String>): List<CiRatePerModule>

    /**
     * Returns the current size in lines of code and the number of commits that touched
     * each module inside the given time window.
     *
     * The LOC value is derived from the whole history (additions minus deletions), so it
     * reflects the current module size; only the change frequency is limited by the window.
     *
     * @param since         start of the time window (epoch millis, UTC)
     * @param until         end of the time window (epoch millis, UTC)
     * @param neededModules module paths to include; an empty list means "all modules"
     */
    fun findSizeAndChangeFrequencyByModule(since: Long, until: Long, neededModules: List<String>): List<ModuleSizeCount>
}
