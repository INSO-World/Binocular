package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Build
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.DomainId
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Job
import com.inso_world.binocular.model.Mention
import com.inso_world.binocular.model.MergeRequest
import com.inso_world.binocular.model.Milestone
import com.inso_world.binocular.model.Module
import com.inso_world.binocular.model.Note
import com.inso_world.binocular.model.Platform
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Stats
import com.inso_world.binocular.model.User
import com.inso_world.binocular.model.vcs.ReferenceCategory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.test.util.AssertionErrors.fail
import java.time.LocalDateTime
import java.util.Date
import kotlin.jvm.java
import kotlin.jvm.javaObjectType
import kotlin.jvm.kotlin
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.uuid.Uuid

// Test to ensure that the persistence entity classes align with the domain model classes
// Tests should fail if there are mismatches in property types or relations
// and warn if there are extra properties or relations
class DomainModelAlignmentTest {
    // Mapped pairs are based of off mappers
    private val mappedClasses =
        mapOf(
            Account::class.java to AccountEntity::class.java,
            Branch::class.java to BranchEntity::class.java,
            Build::class.java to BuildEntity::class.java,
            Commit::class.java to CommitEntity::class.java,
            File::class.java to FileEntity::class.java,
            Issue::class.java to IssueEntity::class.java,
            Job::class.java to JobEntity::class.java,
            Mention::class.java to MentionEntity::class.java,
            MergeRequest::class.java to MergeRequestEntity::class.java,
            Milestone::class.java to MilestoneEntity::class.java,
            Module::class.java to ModuleEntity::class.java,
            Note::class.java to NoteEntity::class.java,
            Platform::class.java to PlatformEntity::class.java,
            Stats::class.java to StatsEntity::class.java,
            User::class.java to UserEntity::class.java,
            Developer::class.java to DeveloperEntity::class.java,
            Repository::class.java to RepositoryEntity::class.java,
            ReferenceCategory::class.java to String::class.java,
            Project::class.java to ProjectEntity::class.java,
        )

    companion object {
        @JvmStatic
        fun entityModelPairs() =
            listOf(
                Arguments.of(Account::class.java, AccountEntity::class.java),
                Arguments.of(Branch::class.java, BranchEntity::class.java),
                Arguments.of(Build::class.java, BuildEntity::class.java),
                Arguments.of(Commit::class.java, CommitEntity::class.java),
                Arguments.of(File::class.java, FileEntity::class.java),
                Arguments.of(Issue::class.java, IssueEntity::class.java),
                Arguments.of(Job::class.java, JobEntity::class.java),
                Arguments.of(Mention::class.java, MentionEntity::class.java),
                Arguments.of(MergeRequest::class.java, MergeRequestEntity::class.java),
                Arguments.of(Milestone::class.java, MilestoneEntity::class.java),
                Arguments.of(Module::class.java, ModuleEntity::class.java),
                Arguments.of(Note::class.java, NoteEntity::class.java),
                Arguments.of(Stats::class.java, StatsEntity::class.java),
                Arguments.of(User::class.java, UserEntity::class.java),
                Arguments.of(Developer::class.java, DeveloperEntity::class.java),
                Arguments.of(Repository::class.java, RepositoryEntity::class.java),
                Arguments.of(Project::class.java, ProjectEntity::class.java),
            )
    }

    @ParameterizedTest
    @MethodSource("entityModelPairs")
    fun `compare entity and model alignment`(
        model: Class<*>,
        entity: Class<*>
    ) {
        val mappedProperties =
            when (model) {
                Commit::class.java -> mapOf("commitDateTime" to "date")
                Branch::class.java -> mapOf("name" to "branch")
                else -> emptyMap()
            }
        val ignoredModelProperties =
            when (model) {
                Branch::class.java, Commit::class.java -> setOf("branch")
                Issue::class.java -> setOf("userIds")
                else -> emptySet()
            }
        val modelProperties =
            model.kotlin.memberProperties
                .filterNot { isIgnoredProperty(it.name) }
                .associateBy { it.name }
        val entityProperties =
            entity.kotlin.memberProperties
                .filterNot { isIgnoredProperty(it.name) }
                .associateBy { it.name }
        val matchedEntityProperties = mutableSetOf<String>()

        for ((name, modelProperty) in modelProperties) {
            val entityPropertyName = findEntityPropertyName(name, entityProperties.keys, mappedProperties)
            if (name in ignoredModelProperties) {
                entityPropertyName?.let(matchedEntityProperties::add)
                continue
            }

            val shape = propertyShape(modelProperty.returnType)
            if (shape.kind == PropertyKind.LEGACY_RELATION) {
                println("⚠️ ${model.simpleName}.$name is a legacy object relation; consider an ID reference.")
                entityPropertyName?.let(matchedEntityProperties::add)
                continue
            }

            val entityProperty = entityPropertyName?.let(entityProperties::get)
            if (entityProperty == null) {
                println("⚠️ ${model.simpleName} has no entity property for '$name' (${modelProperty.returnType}).")
                continue
            }
            matchedEntityProperties.add(entityProperty.name)

            val matches =
                if (shape.kind == PropertyKind.ID_REFERENCE) {
                    idReferenceMatches(modelProperty.returnType, entityProperty.returnType, shape.target)
                } else {
                    typesMatch(modelProperty.returnType, entityProperty.returnType)
                }
            if (!matches) {
                fail(
                    "❌ Property '$name' type mismatch between ${model.simpleName} and ${entity.simpleName}: " +
                        "expected ${modelProperty.returnType} but got ${entityProperty.returnType}; " +
                        "shape=$shape"
                )
            }
        }

        for ((name, entityProperty) in entityProperties) {
            if (name !in matchedEntityProperties) {
                println("⚠️ ${entity.simpleName} has extra property '$name' (${entityProperty.returnType}).")
            }
        }
    }

    private enum class PropertyKind {
        VALUE,
        ID_REFERENCE,
        LEGACY_RELATION,
        UNKNOWN
    }

    private data class PropertyShape(
        val kind: PropertyKind,
        val target: Class<*>? = null
    )

    private fun propertyShape(type: KType): PropertyShape {
        val valueType = collectionElementType(type) ?: type
        val idTarget = domainClassForId(valueType)
        val classifier = typeClassifier(valueType)
        if (classifier?.isSubclassOf(DomainId::class) == true) {
            return PropertyShape(PropertyKind.ID_REFERENCE, idTarget)
        }
        if (classifier != null && mappedClasses[classifier.java] == String::class.java) {
            return PropertyShape(PropertyKind.VALUE)
        }
        if (domainClassFor(valueType) != null || isDomainObject(classifier)) {
            return PropertyShape(PropertyKind.LEGACY_RELATION)
        }
        return if (typeClassifier(valueType) == null) {
            PropertyShape(PropertyKind.UNKNOWN)
        } else {
            PropertyShape(PropertyKind.VALUE)
        }
    }

    private fun domainClassForId(type: KType): Class<*>? {
        val classifier = typeClassifier(type) ?: return null
        if (!classifier.isSubclassOf(DomainId::class)) return null
        val idName = classifier.qualifiedName ?: return null
        return mappedClasses.keys.firstOrNull { modelClass -> idName == "${modelClass.name}.Id" }
    }

    private fun domainClassFor(type: KType): Class<*>? {
        val classifier = typeClassifier(type) ?: return null
        return mappedClasses.keys.firstOrNull { it == classifier.java }
    }

    private fun isDomainObject(classifier: KClass<*>?): Boolean {
        if (classifier == null || classifier.java.isEnum || classifier.isSubclassOf(DomainId::class)) return false
        return classifier.java.packageName.startsWith("com.inso_world.binocular.model.")
    }

    private fun collectionElementType(type: KType): KType? = if (isCollection(type)) type.arguments.firstOrNull()?.type else null

    private fun isCollection(type: KType): Boolean {
        val classifier = typeClassifier(type) ?: return false
        return Collection::class.java.isAssignableFrom(classifier.java)
    }

    private fun typeClassifier(type: KType) = type.classifier as? KClass<*>

    private fun idReferenceMatches(
        modelType: KType,
        entityType: KType,
        target: Class<*>?
    ): Boolean {
        if (isCollection(modelType) != isCollection(entityType)) return false
        val modelValueType = collectionElementType(modelType) ?: modelType
        val entityValueType = collectionElementType(entityType) ?: entityType
        val entityValueClass = typeClassifier(entityValueType)?.java ?: return false
        return entityValueClass == Uuid::class.java ||
            (target != null && mappedClasses[target] == entityValueClass) ||
            modelValueType == entityValueType
    }

    private fun typesMatch(
        modelType: KType,
        entityType: KType
    ): Boolean {
        val modelClassifier = typeClassifier(modelType) ?: return false
        val entityClassifier = typeClassifier(entityType) ?: return false
        val modelClass = modelClassifier.java
        val entityClass = entityClassifier.java

        if (isCollection(modelType) || isCollection(entityType)) {
            if (!isCollection(modelType) || !isCollection(entityType)) return false
            val sameCollectionType =
                modelClass == entityClass ||
                    (modelClass == Set::class.java && entityClass == List::class.java) ||
                    (modelClass == List::class.java && entityClass == Set::class.java)
            if (!sameCollectionType) return false
            val modelElementType = modelType.arguments.firstOrNull()?.type
            val entityElementType = entityType.arguments.firstOrNull()?.type
            return if (modelElementType == null || entityElementType == null) {
                modelElementType == entityElementType
            } else {
                typesMatch(modelElementType, entityElementType)
            }
        }

        if (mappedClasses[modelClass] == entityClass) return true
        if (modelClass == LocalDateTime::class.java && entityClass == Date::class.java) return true
        return modelClassifier.javaObjectType == entityClassifier.javaObjectType
    }

    private fun findEntityPropertyName(
        modelPropertyName: String,
        entityPropertyNames: Set<String>,
        mappedProperties: Map<String, String>
    ): String? {
        val mappedName = mappedProperties[modelPropertyName]
        if (mappedName != null && mappedName in entityPropertyNames) return mappedName
        return entityPropertyCandidates(modelPropertyName).firstOrNull { it in entityPropertyNames }
    }

    private fun entityPropertyCandidates(name: String): List<String> {
        val base =
            when {
                name.endsWith("Ids") -> name.removeSuffix("Ids")
                name.endsWith("Id") -> name.removeSuffix("Id")
                else -> return listOf(name)
            }
        return listOf(name, base, "${base}s", "${base}es").distinct()
    }

    private fun isIgnoredProperty(name: String): Boolean =
        name == "Companion" || name == "logger" || name.contains('$') || name.startsWith("_") || name == "uniqueKey"
}
