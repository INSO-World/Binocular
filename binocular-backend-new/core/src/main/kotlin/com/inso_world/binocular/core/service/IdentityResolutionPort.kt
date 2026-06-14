package com.inso_world.binocular.core.service

/**
 * Port for the identity resolution pipeline.
 * Matches Git developer identities to GitHub accounts.
 */
interface IdentityResolutionPort {
    /**
     * Resolves identities for all developers in the given repository.
     * @param repositoryId The database ID of the repository to process.
     */
    fun resolveIdentities(repositoryId: Long)
}
