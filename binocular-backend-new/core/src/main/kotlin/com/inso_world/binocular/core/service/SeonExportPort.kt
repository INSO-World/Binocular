package com.inso_world.binocular.core.service
import com.inso_world.binocular.model.BranchExportData

interface SeonExportPort {
    fun map(exportData: BranchExportData): String
}
