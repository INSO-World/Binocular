package com.inso_world.binocular.infrastructure.sql.mapper.context

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.config.Scope
import java.util.UUID

internal class MappingScope : Scope {
    private val logger: Logger = LoggerFactory.getLogger(MappingScope::class.java)

    // your identity‑map storage
    private val context = ThreadLocal.withInitial<MutableMap<String, Any>> { mutableMapOf() }

    // nesting counter
    private val sessionDepth = ThreadLocal.withInitial { 0 }

    // the UUID of the current (outermost) session
    private val sessionId = ThreadLocal<String?>()

    /**
     * Begins a (possibly nested) mapping session.
     * - if depth==0 we generate a new ID & clear the old context
     * @return the session ID
     */
    fun startSession(): String {
        val depth = sessionDepth.get()
        if (depth == 0) {
            val newId = UUID.randomUUID().toString()
            sessionId.set(newId)
            context.get().clear()
            logger.debug("Starting new outermost session: {}", newId)
        }
        sessionDepth.set(depth + 1)
        logger.debug("Session {} depth -> {}", sessionId.get(), sessionDepth.get())
        return sessionId.get()!!
    }

    /**
     * Ends one level of session.
     * - if depth hits 0 we clear the context & remove the ID
     * @return Pair(session ID, remaining depth)
     */
    fun endSession(): Pair<String, Int> {
        val depthBefore = sessionDepth.get()
        if (depthBefore <= 0) {
            throw IllegalStateException("No mapping session to end")
        }
        val id = sessionId.get()!!
        val depthAfter = depthBefore - 1
        sessionDepth.set(depthAfter)

        if (depthAfter == 0) {
            // outermost just finished
            context.get().clear()
            sessionId.remove()
            logger.debug("Outermost session {} ended and context cleared", id)
        } else {
            logger.debug("Session {} returned to depth {}", id, depthAfter)
        }
        return id to depthAfter
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
        return context.get().computeIfAbsent(name) {
            objectFactory.getObject()
        }
    }

    override fun remove(name: String): Any? = context.get().remove(name)

    override fun registerDestructionCallback(
        name: String,
        callback: Runnable,
    ) { /* no‑op */ }

    override fun resolveContextualObject(key: String): Any? = null

    override fun getConversationId(): String = sessionId.get() ?: "no-session"

    /** for logging/debugging */
    fun size(): Int = context.get().size
}
