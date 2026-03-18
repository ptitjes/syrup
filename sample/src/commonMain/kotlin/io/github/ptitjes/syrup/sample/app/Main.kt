package io.github.ptitjes.syrup.sample.app

import io.github.ptitjes.syrup.PluginId
import io.github.ptitjes.syrup.host.assemblePlugins
import org.kodein.di.bindSingleton
import org.kodein.di.instance

fun main() {
    val pluginManager = assemblePlugins {
        loadPlugins()
        contributePluginBindings { plugin ->
            bindSingleton<PluginId> { plugin.id }
        }
    }

    val di = pluginManager.mainDi

    val standaloneService by di.instance<StandaloneService>()
    standaloneService.indentifyAll()
}
