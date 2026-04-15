package com.inso_world.binocular.core.delegates

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject

/**
 * Returns a thread-safe, lazily initialized SLF4J [Logger] bound to the receiver’s
 * declaring class.
 *
 * Works seamlessly from Kotlin *companion objects* by resolving the enclosing class,
 * so logs are emitted under the outer class (e.g., `Foo`) rather than `Foo$Companion`.
 *
 * ### Usage
 * ```
 * class Foo {
 *   companion object {
 *       private val log by logger()
 *   }
 *   fun run() { log.info("hello") }
 * }
 * ```
 *
 * ### Implementation notes
 * - Uses [lazy] with the default [LazyThreadSafetyMode.SYNCHRONIZED].
 * - Resolves the correct class via [unwrapCompanionClass].
 */
fun <R : Any> R.logger(): Lazy<Logger> {
    return lazy { LoggerFactory.getLogger(unwrapCompanionClass(this.javaClass).name) }
}

/**
 * Resolves the declaring [KClass] for a possibly companion-object class.
 *
 * @param ofClass Kotlin class that may represent a companion object.
 * @return the enclosing class if [ofClass] is a companion object; otherwise [ofClass] itself.
 * @see unwrapCompanionClass for the `java.lang.Class` variant.
 */
internal fun <T : Any> unwrapCompanionClass(ofClass: KClass<T>): KClass<*> {
    return unwrapCompanionClass(ofClass.java).kotlin
}

/**
 * Unwraps companion class to enclosing class given a Java Class.
 *
 * @param ofClass Java class that may represent a companion object.
 * @return the enclosing class if [ofClass] is a companion object; otherwise [ofClass] itself.
 */
fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
    return ofClass.enclosingClass?.takeIf {
        ofClass.enclosingClass.kotlin.companionObject?.java == ofClass
    } ?: ofClass
}
