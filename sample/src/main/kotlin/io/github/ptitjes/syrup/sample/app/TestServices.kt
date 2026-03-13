package io.github.ptitjes.syrup.sample.app

import io.github.ptitjes.syrup.PluginId

class StandaloneService(
    private val services: Set<Service> = emptySet(),
) {
    init {
        println("StandaloneService initialized with ${services.size} services")
    }

    fun indentifyAll() {
        services.forEach { it.indentify() }
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

class OtherServiceBasedService(val otherServices: Set<OtherService>) : Service {
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
