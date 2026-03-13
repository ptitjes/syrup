package io.github.ptitjes.syrup.sample.app

import dev.whyoleg.sweetspi.ServiceProvider
import io.github.ptitjes.syrup.Plugin
import org.kodein.di.*

@ServiceProvider
object TestPluginA : Plugin {
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

@ServiceProvider
object TestPluginB : Plugin {
    override val dependencies: Set<Plugin> = setOf(TestPluginA)

    override fun DI.Builder.api() {
        inBindSet<Service> {
            add { singleton { new(::NamedService, "B") } }
        }
    }

    override fun DI.Builder.implementation() {
    }
}

@ServiceProvider
object TestPluginC : Plugin {
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

@ServiceProvider
object TestPluginD : Plugin {
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
