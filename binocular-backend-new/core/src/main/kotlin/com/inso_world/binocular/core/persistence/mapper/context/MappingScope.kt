package com.inso_world.binocular.core.persistence.mapper.context

import com.inso_world.binocular.core.delegates.logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.config.Scope
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class MappingScope : Scope {
    companion object {
        private val logger by logger()
    }

    /** A single, application‑wide identity map. */
    private val context = ConcurrentHashMap<String, Any>()

    /** How many nested sessions are active *across all threads*. */
    private val sessionDepth = AtomicInteger(0)

    /** The current session’s UUID, or null if none active. */
    private val sessionId = AtomicReference<String?>(null)

    /**
     * Begins a (possibly nested) mapping session.
     *
     * - If this is the *first* session (depth goes 0→1), generate a new UUID
     *   and clear out any old context.
     * - Further nested calls just increment the depth.
     *
     * @return the active session ID
     */
    @Synchronized
    fun startSession(): String {
        val depth = sessionDepth.incrementAndGet()
        if (depth == 1) {
            // outermost session
            val newId = UUID.randomUUID().toString().also { sessionId.store(it) }
            context.clear()
            logger.debug("Started new outermost session: {}", newId)
        } else {
            logger.debug("Session {} nested depth → {}", sessionId.load(), depth)
        }
        return sessionId.load()!!
    }

    /**
     * Ends one level of session.
     *
     * - If depth goes 1→0, clears the shared context and nulls out the sessionId.
     * - Throws if no session is active.
     *
     * @return Pair(session ID, remaining depth)
     */
    @Synchronized
    fun endSession(): Pair<String, Int> {
        val before = sessionDepth.get()
        if (before <= 0) {
            throw IllegalStateException("No mapping session to end")
        }
        val id = sessionId.load()!!
        val after = sessionDepth.decrementAndGet()
        if (after == 0) {
            // all sessions ended
            context.clear()
            sessionId.store(null)
            logger.debug("Session {} ended; context cleared", id)
        } else {
            logger.debug("Session {} returned to depth {}", id, after)
        }
        return id to after
    }

    override fun get(
        name: String,
        objectFactory: ObjectFactory<*>,
    ): Any {
        if (sessionDepth.get() == 0) {
            throw IllegalStateException(
                "Tried to retrieve @Scope(\"mapping\") bean outside of a @MappingSession",
            )
        }
        // Atomic compute-if-absent on the shared ConcurrentHashMap
        return context.computeIfAbsent(name) {
            objectFactory.getObject()
        }
    }

    override fun remove(name: String): Any? = context.remove(name)

    override fun registerDestructionCallback(
        name: String,
        callback: Runnable,
    ) {
        // no‑op
    }

    override fun resolveContextualObject(key: String): Any? = null

    override fun getConversationId(): String = sessionId.load() ?: "no-session"

    /** For diagnostics: how many beans are in the shared map right now. */
    fun size(): Int = context.size
}
