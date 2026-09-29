package com.inso_world.binocular.infrastructure.arangodb.migration

import com.arangodb.springframework.core.template.ArangoTemplate
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.stereotype.Component

/**
 * Makes ArangoDB access wait until schema migrations have completed.
 *
 * Spring Data ArangoDB creates persistent indexes while repository beans are initialized. Legacy
 * data must therefore be migrated before [ArangoTemplate] becomes available to any repository.
 * This post-processor records that lifecycle dependency directly in Spring's bean graph while
 * preserving migration-disabled contexts, where no [MigrationRunner] bean exists.
 */
@Component
internal class ArangoMigrationDependencyConfigurer : BeanFactoryPostProcessor {
    /**
     * Adds every active migration runner as a prerequisite of each ArangoDB template bean.
     *
     * The dependency is configured before singleton creation, guaranteeing that migrations finish
     * before repository factories can use a template to create collections or indexes.
     */
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        val migrationRunnerNames =
            beanFactory.getBeanNamesForType(MigrationRunner::class.java, false, false)
        if (migrationRunnerNames.isEmpty()) return

        beanFactory
            .getBeanNamesForType(ArangoTemplate::class.java, false, false)
            .forEach { templateBeanName ->
                beanFactory
                    .getBeanDefinition(templateBeanName)
                    .addDependencies(migrationRunnerNames)
            }
    }

    /**
     * Merges [dependencies] into this bean definition without discarding existing prerequisites.
     */
    private fun BeanDefinition.addDependencies(dependencies: Array<String>) {
        val existingDependencies = getDependsOn()?.asList().orEmpty()
        setDependsOn(
            *(existingDependencies + dependencies.asList())
                .distinct()
                .toTypedArray(),
        )
    }
}
