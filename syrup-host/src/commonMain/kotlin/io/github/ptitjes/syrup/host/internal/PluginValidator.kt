package io.github.ptitjes.syrup.host.internal

import io.github.ptitjes.syrup.Plugin
import io.github.ptitjes.syrup.host.PluginException
import io.github.ptitjes.syrup.specification.ExtensionPoint
import io.github.ptitjes.syrup.specification.PluginSpecification

internal class PluginValidator(
    private val plugins: Set<Plugin>,
    private val specifications: Map<Plugin, PluginSpecification>
) {
    fun validate() {
        validateNoCircularDependencies()
        validateNoShadowingExposedTypes()
        validateContributionsToDependencyExtensionPoints()
        validateSingularExtensionPoints()
        validatePluralExtensionPoints()
        validateNoConflictingSingularContributions()
    }

    private fun validateNoCircularDependencies() {
        val visited = mutableSetOf<Plugin>()
        val stack = mutableSetOf<Plugin>()

        fun check(plugin: Plugin) {
            if (plugin in stack) {
                throw PluginException("Circular dependency detected involving plugin ${plugin.id}")
            }
            if (plugin in visited) return

            visited += plugin
            stack += plugin
            plugin.dependencies.forEach { check(it) }
            stack -= plugin
        }

        plugins.forEach { check(it) }
    }

    private fun validateNoShadowingExposedTypes() {
        plugins.forEach { plugin ->
            val spec = specifications.getValue(plugin)
            val exposedTypes = spec.exposedTypes.map { it.type }.toSet()

            fun checkDependencies(current: Plugin, seenExposedTypes: Set<Any?>) {
                current.dependencies.forEach { dependency ->
                    val dependencySpec = specifications.getValue(dependency)
                    dependencySpec.exposedTypes.forEach { exposed ->
                        if (exposed.type in exposedTypes) {
                            throw PluginException("Plugin ${plugin.id} exposes type ${exposed.type} which is already exposed by its dependency ${dependency.id}")
                        }
                    }
                    checkDependencies(dependency, seenExposedTypes)
                }
            }
            checkDependencies(plugin, exposedTypes)
        }
    }

    private fun validateContributionsToDependencyExtensionPoints() {
        plugins.forEach { plugin ->
            val spec = specifications.getValue(plugin)
            spec.extensionContributions.forEach { contribution ->
                val point = contribution.point
                val owner = findExtensionPointOwner(point)
                    ?: throw PluginException("Extension point $point contributed by ${plugin.id} is not declared by any plugin")

                if (owner != plugin && !isDependency(plugin, owner)) {
                    throw PluginException("Plugin ${plugin.id} contributes to extension point $point owned by ${owner.id}, but does not depend on it")
                }
            }
        }
    }

    private fun validateSingularExtensionPoints() {
        plugins.forEach { plugin ->
            val spec = specifications.getValue(plugin)
            spec.extensionPoints.forEach { declaration ->
                val point = declaration.point
                if (point is ExtensionPoint.Singular<*>) {
                    val contributionsCount = countContributions(point)
                    if (contributionsCount == 0 && !declaration.optional) {
                        throw PluginException("Non-optional singular extension point ${point.type} owned by ${plugin.id} has no contributions")
                    }
                    if (contributionsCount > 1) {
                        throw PluginException("Conflicting contributions to singular extension point ${point.type} owned by ${plugin.id}")
                    }
                }
            }
        }
    }

    private fun validatePluralExtensionPoints() {
        plugins.forEach { plugin ->
            val spec = specifications.getValue(plugin)
            spec.extensionPoints.forEach { declaration ->
                val point = declaration.point
                if (point is ExtensionPoint.Plural<*> && !declaration.optional) {
                    val contributionsCount = countContributions(point)
                    if (contributionsCount == 0) {
                        throw PluginException("Non-optional plural extension point ${point.type} owned by ${plugin.id} has no contributions")
                    }
                }
            }
        }
    }

    private fun validateNoConflictingSingularContributions() {}

    private fun findExtensionPointOwner(point: ExtensionPoint): Plugin? {
        return plugins.find { plugin ->
            specifications.getValue(plugin).extensionPoints.any { it.point === point }
        }
    }

    private fun isDependency(plugin: Plugin, potentialDependency: Plugin): Boolean {
        val visited = mutableSetOf<Plugin>()
        fun check(current: Plugin): Boolean {
            if (current == potentialDependency) return true
            if (current in visited) return false
            visited += current
            return current.dependencies.any { check(it) }
        }
        return plugin.dependencies.any { check(it) }
    }

    private fun countContributions(point: ExtensionPoint): Int {
        return plugins.sumOf { plugin ->
            specifications.getValue(plugin).extensionContributions.count { it.point === point }
        }
    }
}
