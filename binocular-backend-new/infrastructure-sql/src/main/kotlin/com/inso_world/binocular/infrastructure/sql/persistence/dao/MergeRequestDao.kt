package com.inso_world.binocular.infrastructure.sql.persistence.dao

import com.inso_world.binocular.model.MergeRequest
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
internal class MergeRequestDao {

    @PersistenceContext
    private lateinit var em: EntityManager

    fun findById(id: String): MergeRequest? =
        try {
            em.createNativeQuery("select id, title, state from merge_requests where id = ?1")
                .setParameter(1, id)
                .resultList
                .firstOrNull()
                ?.let { row ->
                    val arr = row as Array<*>
                    MergeRequest(
                        id = arr[0]?.toString(),
                        title = arr[1]?.toString(),
                        state = arr[2]?.toString()
                    )
                }
        } catch (ex: Exception) {
            null
        }

    fun findAll(): List<MergeRequest> =
        em.createNativeQuery("select id, title, state from merge_requests order by id")
            .resultList
            .map { row ->
                val arr = row as Array<*>
                MergeRequest(
                    id = arr[0]?.toString(),
                    title = arr[1]?.toString(),
                    state = arr[2]?.toString()
                )
            }

    fun count(): Long =
        (em.createNativeQuery("select count(*) from merge_requests").singleResult as Number).toLong()

    fun findAll(pageable: Pageable): List<MergeRequest> =
        em.createNativeQuery("select id, title, state from merge_requests order by id limit ?1 offset ?2")
            .setParameter(1, pageable.pageSize)
            .setParameter(2, pageable.offset.toInt())
            .resultList
            .map { row ->
                val arr = row as Array<*>
                MergeRequest(
                    id = arr[0]?.toString(),
                    title = arr[1]?.toString(),
                    state = arr[2]?.toString()
                )
            }

    fun create(mr: MergeRequest): MergeRequest {
        em.createNativeQuery("insert into merge_requests (id, title, state, created_at, updated_at) values (?1, ?2, ?3, ?4, ?5)")
            .setParameter(1, mr.id)
            .setParameter(2, mr.title)
            .setParameter(3, mr.state)
            .setParameter(4, mr.createdAt)
            .setParameter(5, mr.updatedAt)
            .executeUpdate()
        return mr
    }

    fun update(mr: MergeRequest): MergeRequest {
        em.createNativeQuery("update merge_requests set title = ?1, state = ?2 where id = ?3")
            .setParameter(1, mr.title)
            .setParameter(2, mr.state)
            .setParameter(3, mr.id)
            .executeUpdate()
        return mr
    }

    fun deleteById(id: String) {
        em.createNativeQuery("delete from merge_requests where id = ?1")
            .setParameter(1, id)
            .executeUpdate()
    }

    fun deleteAll() {
        em.createNativeQuery("delete from merge_requests").executeUpdate()
    }
}
