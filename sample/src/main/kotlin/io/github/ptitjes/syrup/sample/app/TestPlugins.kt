package io.github.ptitjes.syrup.sample.app

import io.github.ptitjes.syrup.Plugin
import io.github.ptitjes.syrup.PluginId
import org.kodein.di.*

data object TestPluginA : Plugin {
    override val dependencies: Set<Plugin> = emptySet()

    override fun DI.Builder.api() {
        bind<StandaloneService> { singleton { instance<StandaloneService>() } }

        bindSet<Service> {
            add { singleton { new(::NamedService, "A1") } }
            add { singleton { new(::NamedService, "A2") } }
        }
    }

    override fun DI.Builder.implementation() {
        bind<StandaloneService> { singleton { new(::StandaloneService) } }
    }
}

data object TestPluginB : Plugin {
    override val dependencies: Set<Plugin> = setOf(TestPluginA)

    override fun DI.Builder.api() {
        inBindSet<Service> {
            add { singleton { new(::NamedService, "B") } }
        }
    }

    override fun DI.Builder.implementation() {
    }
}

data object TestPluginC : Plugin {
    override val dependencies: Set<Plugin> = setOf(TestPluginA)

    override fun DI.Builder.api() {
        inBindSet<Service> {
            add { singleton { new(::OtherServiceBasedService) } }
        }

        bindSet<OtherService> {
            add { singleton { new(::NamedOtherService, "C1") } }
        }
    }

    override fun DI.Builder.implementation() {
    }
}

data object TestPluginD : Plugin {
    override val dependencies: Set<Plugin> = setOf(TestPluginC)

    override fun DI.Builder.api() {
        inBindSet<OtherService> {
            add { singleton { new(::NamedOtherService, "D1") } }
            add { singleton { new(::NamedOtherService, "D2") } }
        }
    }

    override fun DI.Builder.implementation() {
    }
}

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
