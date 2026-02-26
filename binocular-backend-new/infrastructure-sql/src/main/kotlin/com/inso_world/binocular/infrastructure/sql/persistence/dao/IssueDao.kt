package com.inso_world.binocular.infrastructure.sql.persistence.dao

import com.inso_world.binocular.model.Issue
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
internal class IssueDao {

    @PersistenceContext
    private lateinit var em: EntityManager

    fun findById(id: String): Issue? =
        try {
            em.createNativeQuery("select id, title, state from issues where id = ?1")
                .setParameter(1, id)
                .resultList
                .firstOrNull()
                ?.let { row ->
                    val arr = row as Array<*>
                    Issue(
                        id = arr[0]?.toString(),
                        title = arr[1]?.toString(),
                        state = arr[2]?.toString()
                    )
                }
        } catch (ex: Exception) {
            null
        }

    fun findAll(): List<Issue> =
        em.createNativeQuery("select id, title, state from issues order by id")
            .resultList
            .map { row ->
                val arr = row as Array<*>
                Issue(
                    id = arr[0]?.toString(),
                    title = arr[1]?.toString(),
                    state = arr[2]?.toString()
                )
            }

    fun count(): Long =
        (em.createNativeQuery("select count(*) from issues").singleResult as Number).toLong()

    fun findAll(pageable: Pageable): List<Issue> =
        em.createNativeQuery("select id, title, state from issues order by id limit ?1 offset ?2")
            .setParameter(1, pageable.pageSize)
            .setParameter(2, pageable.offset.toInt())
            .resultList
            .map { row ->
                val arr = row as Array<*>
                Issue(
                    id = arr[0]?.toString(),
                    title = arr[1]?.toString(),
                    state = arr[2]?.toString()
                )
            }

    fun create(issue: Issue): Issue {
        em.createNativeQuery("insert into issues (id, title, state, created_at, updated_at) values (?1, ?2, ?3, ?4, ?5)")
            .setParameter(1, issue.id)
            .setParameter(2, issue.title)
            .setParameter(3, issue.state)
            .setParameter(4, issue.createdAt?.toString())
            .setParameter(5, issue.updatedAt?.toString())
            .executeUpdate()
        return issue
    }

    fun update(issue: Issue): Issue {
        em.createNativeQuery("update issues set title = ?1, state = ?2 where id = ?3")
            .setParameter(1, issue.title)
            .setParameter(2, issue.state)
            .setParameter(3, issue.id)
            .executeUpdate()
        return issue
    }

    fun deleteById(id: String) {
        em.createNativeQuery("delete from issues where id = ?1")
            .setParameter(1, id)
            .executeUpdate()
    }

    fun deleteAll() {
        em.createNativeQuery("delete from issues").executeUpdate()
    }
}
