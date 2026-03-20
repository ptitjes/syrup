package io.github.ptitjes.syrup.sample.app

import io.github.ptitjes.syrup.PluginContext
import io.github.ptitjes.syrup.PluginId

class StandaloneService(
    context: PluginContext,
) {
    private val services by context.contributions(ExtensionPoints.Services)
    private val sourcedServices by context.sourcedContributions(ExtensionPoints.Services)

    fun indentifyAll() {
        println("StandaloneService initialized with ${services.size} services")
        services.forEach { it.indentify() }

        println()
        println("With sources:")
        sourcedServices.forEach {
            println("Source: ${it.source}")
            it.contribution.indentify()
        }
    }
}

interface Service {
    fun indentify()
}

class NamedService(val name: String, val pluginId: PluginId) : Service {
    override fun indentify() {
        println("I am $name (pluginId=${pluginId})")
    }
}

class OtherServiceBasedService(pluginContext: PluginContext) : Service {
    val otherServices by pluginContext.contributions(ExtensionPoints.OtherServices)

    override fun indentify() {
        otherServices.forEach { it.indentify() }
    }
}

interface OtherService {
    fun indentify()
}

class NamedOtherService(val name: String, val pluginId: PluginId) : OtherService {
    override fun indentify() {
        println("I am $name (pluginId=${pluginId})")
    }
}
