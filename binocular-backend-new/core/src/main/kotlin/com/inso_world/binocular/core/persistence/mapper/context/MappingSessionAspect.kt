package com.inso_world.binocular.core.persistence.mapper.context

import com.inso_world.binocular.core.delegates.logger
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Aspect
@Component
internal class MappingSessionAspect {
    @Autowired
    private lateinit var mappingScope: MappingScope

    companion object {
        private val logger by logger()
    }

    @Around("@within(mappingSession) || @annotation(mappingSession)", argNames = "pjp,mappingSession")
    fun around(
        pjp: ProceedingJoinPoint,
        mappingSession: MappingSession?,
    ): Any? {
        val sessionId = mappingScope.startSession()
        logger.debug("[{}] ▶ Enter {}", sessionId, pjp.signature)
        return try {
            pjp.proceed()
        } finally {
            val (endedId, remainingDepth) = mappingScope.endSession()
            if (endedId != sessionId) {
                throw IllegalStateException(
                    "MappingSession ID mismatch: started $sessionId but ended $endedId",
                )
            }
            logger.debug("[{}] ◀ Exit {} (depth now {})", endedId, pjp.signature, remainingDepth)
        }
    }
}
