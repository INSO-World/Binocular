package com.inso_world.binocular.ffi.extensions

import com.inso_world.binocular.ffi.internal.BinocularCommitVec
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import java.time.LocalDateTime

/** Map a whole batch in two passes to preserve identity for commits, parents and users. */
internal fun Collection<BinocularCommitVec>.toDomain(repository: Repository): List<Commit> {
//    val usersByKey = LinkedHashMap<String, User>()
    val usersByKey = repository.user.associateBy(User::uniqueKey).toMutableMap()
//    val commitsBySha = LinkedHashMap<String, Commit>()
    val commitsBySha = repository.commits.associateBy(Commit::uniqueKey).toMutableMap()

    // 1) create Users & Commits (scalar fields only; no relationships yet)
    for (bin in this) {
        bin.author?.let { author ->
            val key = author.userKey(repository)
            val user = usersByKey.computeIfAbsent(key) { author.toDomain(repository) }
            repository.user.add(user)
        }
        bin.committer?.let { committer ->
            val key = committer.userKey(repository)
            val user = usersByKey.computeIfAbsent(key) { committer.toDomain(repository) }
            repository.user.add(user)
        }

        // commitDateTime = committer time (fallback to author time; final fallback: now)
        val commitDt = (bin.committer?.time ?: bin.author?.time)?.toLocalDateTime() ?: LocalDateTime.now()
        val authorDt = bin.author?.time?.toLocalDateTime()

        commitsBySha.computeIfAbsent(bin.commit) {
            val cmt =
                Commit(
                    sha = bin.commit,
                    authorDateTime = authorDt,
                    commitDateTime = commitDt,
                    message = bin.message,
                    repository = repository,
                )
//            cmt.committer = committerUser
//            cmt.author = authorUser
            repository.commits.add(cmt)
            cmt
        }
//        authorUser?.let { it.repository = repository }
//        committerUser?.let { it.repository = repository }
    }

    // 2) wire relationships: committer, author, parents
    for (bin in this) {
        val commit = commitsBySha.getValue(bin.commit)

        // users (linking will also populate the inverse sets via domain logic)
        bin.committer?.let { commit.committer = usersByKey.getValue(it.userKey(repository)) }
        bin.author?.let { commit.author = usersByKey.getValue(it.userKey(repository)) }

        // parents (and implicit children back-links via domain)
        for (pSha in bin.parents) {
            val parent =
                commitsBySha.computeIfAbsent(pSha) {
                    // unseen parent – create lightweight placeholder (vals require something);
                    // will still preserve identity and relationships.
                    Commit(
                        sha = pSha,
                        authorDateTime = null,
                        commitDateTime = commit.commitDateTime, // safe placeholder; can be enriched elsewhere
                        message = null,
                        repository = repository,
                    )
                }
            commit.parents.add(parent)
        }
    }
    val cmts = commitsBySha.values.toList()
    repository.commits.addAll(cmts)

    return cmts
}
