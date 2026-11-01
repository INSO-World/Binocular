package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.arangodb.ArangoCursor
import com.arangodb.springframework.annotation.Relations
import com.arangodb.springframework.core.ArangoOperations
import com.arangodb.springframework.core.convert.resolver.AbstractResolver
import com.arangodb.springframework.core.convert.resolver.RelationResolver
import org.springframework.data.util.TypeInformation
import java.util.function.Supplier
import kotlin.jvm.java

class QuotedRelationsResolver(
    private val template: ArangoOperations,
) : AbstractResolver(template.converter.conversionService),
    RelationResolver<Relations> {
    override fun resolveOne(
        id: String,
        type: TypeInformation<*>,
        traversedTypes: Collection<TypeInformation<*>>,
        annotation: Relations,
    ): Any? {
        val supplier = Supplier { resolveOneNow(id, type, traversedTypes, annotation) }
        return if (annotation.lazy) proxy(id, type, supplier) else supplier.get()
    }

    override fun resolveMultiple(
        id: String,
        type: TypeInformation<*>,
        traversedTypes: Collection<TypeInformation<*>>,
        annotation: Relations,
    ): Any? {
        val supplier = Supplier { resolveMultipleNow(id, type, traversedTypes, annotation) }
        return if (annotation.lazy) proxy(id, type, supplier) else supplier.get()
    }

    private fun resolveOneNow(
        id: String,
        type: TypeInformation<*>,
        traversedTypes: Collection<TypeInformation<*>>,
        annotation: Relations,
    ): Any? {
        val cursor = resolve(id, type.type, traversedTypes, annotation, limit = true)
        return if (cursor.hasNext()) cursor.next() else null
    }

    private fun resolveMultipleNow(
        id: String,
        type: TypeInformation<*>,
        traversedTypes: Collection<TypeInformation<*>>,
        annotation: Relations,
    ): Any =
        resolve(
            id,
            getNonNullComponentType(type).type,
            traversedTypes,
            annotation,
            limit = false,
        ).asListRemaining()

    private fun resolve(
        id: String,
        type: Class<*>,
        traversedTypes: Collection<TypeInformation<*>>,
        annotation: Relations,
        limit: Boolean,
    ): ArangoCursor<*> {
        val edges =
            annotation.edges.joinToString(", ") { edge ->
                "`" + template.collection(edge.java).name() + "`"
            }
        val allTraversedTypes = listOf(type) + traversedTypes.map { it.type }
        val bindVars = mutableMapOf<String, Any>("start" to id)
        val withClause =
            allTraversedTypes
                .mapIndexed { index, traversedType ->
                    bindVars["@with$index"] = traversedType
                    "@@with$index"
                }.joinToString(", ", prefix = "WITH ")
        val query =
            "$withClause FOR v IN ${annotation.minDepth} .. ${annotation.maxDepth} " +
                "${annotation.direction} @start $edges OPTIONS {bfs: true, uniqueVertices: \"global\"} " +
                (if (limit) "LIMIT 1 " else "") + "RETURN v"

        return template.query(query, bindVars, type)
    }
}
