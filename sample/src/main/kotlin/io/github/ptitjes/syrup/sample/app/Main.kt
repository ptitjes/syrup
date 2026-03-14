package io.github.ptitjes.syrup.sample.app

import io.github.ptitjes.host.assemblePlugins
import io.github.ptitjes.syrup.PluginId
import org.kodein.di.bindSingleton
import org.kodein.di.instance

fun main() {
    val plugins = assemblePlugins {
        loadPlugins()
        contributePluginBindings { plugin ->
            bindSingleton<PluginId> { plugin.id }
        }
    }

    val di = plugins.diFor(TestPluginA)

    val standaloneService by di.instance<StandaloneService>()
    println("Standalone service: ${standaloneService::class.simpleName}")
    standaloneService.indentifyAll()
}
