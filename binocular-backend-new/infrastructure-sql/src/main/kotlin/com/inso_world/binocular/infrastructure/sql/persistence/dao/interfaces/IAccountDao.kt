package com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces

import com.inso_world.binocular.infrastructure.sql.persistence.entity.AccountEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.IssueEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.MergeRequestEntity
import java.util.stream.Stream

internal interface IAccountDao : IDao<AccountEntity, Long> {

    fun findExistingGid(gids: List<String>): Iterable<AccountEntity>
    fun findAllByIssue(issue: IssueEntity): Stream<AccountEntity>

    // TODO
    //fun findAllByMergeRequest(mergeRequest: MergeRequestEntity): Stream<AccountEntity>

    // TODO uncomment when NoteEntity is implemented
    //fun findAllByNote(note: NoteEntity): Stream<AccountEntity>
}
