package com.inso_world.binocular.web.graphql.mapper

import com.inso_world.binocular.model.*
import com.inso_world.binocular.web.graphql.model.*
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.ReportingPolicy

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface GraphQlMapper {
    fun toDto(account: Account): AccountDto
    fun toDto(mergeRequest: MergeRequest): MergeRequestDto
    fun toDto(issue: Issue): IssueDto
    fun toDto(branch: Branch): BranchDto
    fun toDto(build: Build): BuildDto
    fun toDto(commit: Commit): CommitDto
    fun toDto(file: File): FileDto
    fun toDto(milestone: Milestone): MilestoneDto
    fun toDto(module: Module): ModuleDto
    fun toDto(note: Note): NoteDto
    fun toDto(user: User): UserDto
    fun toDto(mention: Mention): MentionDto
    fun toDto(job: Job): JobDto
    fun toDto(fileState: FileState): FileStateDto
    fun toDto(stats: Stats): StatsDto
}
