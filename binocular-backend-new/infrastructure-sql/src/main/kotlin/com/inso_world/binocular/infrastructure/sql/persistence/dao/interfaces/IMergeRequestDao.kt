package com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces

import com.inso_world.binocular.infrastructure.sql.persistence.entity.MergeRequestEntity

internal interface IMergeRequestDao : IDao<MergeRequestEntity, Long> {
    fun findByIid(iid: com.inso_world.binocular.model.MergeRequest.Id): MergeRequestEntity?
}
