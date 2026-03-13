package io.github.ptitjes.syrup.sample.app

import io.github.ptitjes.host.PluginManager
import io.github.ptitjes.syrup.PluginId
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

fun main() {
    val plugins = PluginManager(
        internalPluginBindings = { plugin ->
            DI.Module {
                bindSingleton<PluginId> { plugin.id }
            }
        },
    )

    val di = plugins.diFor(TestPluginA)

    val standaloneService by di.instance<StandaloneService>()
    println("Standalone service: ${standaloneService::class.simpleName}")
    standaloneService.indentifyAll()
}
