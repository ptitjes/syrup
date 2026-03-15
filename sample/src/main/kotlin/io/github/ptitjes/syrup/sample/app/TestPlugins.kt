package io.github.ptitjes.syrup.sample.app

import dev.whyoleg.sweetspi.ServiceProvider
import io.github.ptitjes.syrup.Plugin
import io.github.ptitjes.syrup.specification.ExtensionPoint
import io.github.ptitjes.syrup.specification.PluginSpecificationBuilder
import org.kodein.di.*
import org.kodein.type.generic

object ExtensionPoints {
    object Services : ExtensionPoint.Plural<Service>(generic())
    object OtherServices : ExtensionPoint.Plural<OtherService>(generic())
}

@ServiceProvider
object TestPluginA : Plugin {
    override val dependencies: Set<Plugin> = emptySet()

    override fun PluginSpecificationBuilder.specification() {
        exposedType<StandaloneService> { instance() }

        extensionPoint(ExtensionPoints.Services)

        ExtensionPoints.Services {
            contribution { new(::NamedService, "A1") }
            contribution { new(::NamedService, "A2") }
        }
    }

    override fun DI.Builder.implementation() {
        bind<StandaloneService> { singleton { new(::StandaloneService) } }
    }
}

@ServiceProvider
object TestPluginB : Plugin {
    override val dependencies: Set<Plugin> = setOf(TestPluginA)

    override fun PluginSpecificationBuilder.specification() {
        ExtensionPoints.Services {
            contribution { new(::NamedService, "B") }
        }
    }
}

@ServiceProvider
object TestPluginC : Plugin {
    override val dependencies: Set<Plugin> = setOf(TestPluginA)

    override fun PluginSpecificationBuilder.specification() {
        ExtensionPoints.Services {
            contribution { new(::OtherServiceBasedService) }
        }

        extensionPoint(ExtensionPoints.OtherServices)

        ExtensionPoints.OtherServices {
            contribution { new(::NamedOtherService, "C1") }
        }
    }
}

@ServiceProvider
object TestPluginD : Plugin {
    override val dependencies: Set<Plugin> = setOf(TestPluginC)

    override fun PluginSpecificationBuilder.specification() {
        ExtensionPoints.OtherServices {
            contribution { new(::NamedOtherService, "D1") }
            contribution { new(::NamedOtherService, "D2") }
        }
    }
}
