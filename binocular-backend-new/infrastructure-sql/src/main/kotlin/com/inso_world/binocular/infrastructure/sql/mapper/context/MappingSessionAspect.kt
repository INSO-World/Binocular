package com.inso_world.binocular.infrastructure.sql.mapper.context

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Aspect
@Component
internal class MappingSessionAspect {
    @Autowired
    private lateinit var mappingScope: MappingScope

//    @Autowired
//    private lateinit var mappingContext: MappingContext

    private val logger: Logger = LoggerFactory.getLogger(MappingSessionAspect::class.java)

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
