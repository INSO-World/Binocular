package com.inso_world.binocular.core.service

import com.inso_world.binocular.model.ShaclReport

interface ShaclValidationPort {
    fun validate(jsonLdString: String): ShaclReport
}
