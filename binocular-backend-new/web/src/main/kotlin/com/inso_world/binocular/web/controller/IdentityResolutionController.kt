package com.inso_world.binocular.web.controller

import com.inso_world.binocular.infrastructure.arangodb.service.IdentityResolutionArangoService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
internal class IdentityResolutionController(
    private val resolutionService: IdentityResolutionArangoService,
) {
    @PostMapping("/resolve-identities")
    fun resolve(): Map<String, Int> = resolutionService.resolveAll(clearExisting = true)
}
