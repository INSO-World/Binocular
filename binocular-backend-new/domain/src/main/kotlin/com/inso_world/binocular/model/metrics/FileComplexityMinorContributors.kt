package com.inso_world.binocular.model.metrics

import com.inso_world.binocular.model.AbstractDomainObject
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Commit.Id
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.metrics.BusFactorCIErrorRate.Key
import org.hibernate.validator.constraints.Range
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class FileComplexityMinorContributors(
    val filePath: String,
    @field:Range(min = 0) val loc: Int,
    @field:Range(min = 0) val minorContributors: Int,
    @field:Range(min = 0) val nfix: Int,
) : AbstractDomainObject<FileComplexityMinorContributors.Id, FileComplexityMinorContributors.Key>(
    Id(Uuid.random())
) {
    @JvmInline
    value class Id(val value: Uuid)

    data class Key(val id: String)

    override val uniqueKey: FileComplexityMinorContributors.Key
        get() = Key(filePath)
}
