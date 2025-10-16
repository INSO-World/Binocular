package com.inso_world.binocular.core.delegates

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject

fun <R : Any> R.logger(): Lazy<Logger> {
    return lazy { LoggerFactory.getLogger(unwrapCompanionClass(this.javaClass).name) }
}

// unwrap companion class to enclosing class given a Kotlin Class
internal fun <T : Any> unwrapCompanionClass(ofClass: KClass<T>): KClass<*> {
    return unwrapCompanionClass(ofClass.java).kotlin
}

// unwrap companion class to enclosing class given a Java Class
fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
    return ofClass.enclosingClass?.takeIf {
        ofClass.enclosingClass.kotlin.companionObject?.java == ofClass
    } ?: ofClass
}
