// package com.inso_world.binocular.web.graphql.mapper.impl
//
// import com.inso_world.binocular.model.FileState
// import com.inso_world.binocular.web.graphql.model.FileStateDto
// import org.springframework.stereotype.Component
//
// @Component
// class GraphQlFileStateMapper(
//    private val commitMapper: GraphQlCommitMapper,
//    private val fileMapper: GraphQlFileMapper
// ) {
//    fun toDto(fileState: FileState): FileStateDto {
//        return FileStateDto(
//            id = fileState.id,
//            content = fileState.content,
//            commit = commitMapper.toDto(fileState.commit),
//            file = fileMapper.toDto(fileState.file)
//        )
//    }
// }
