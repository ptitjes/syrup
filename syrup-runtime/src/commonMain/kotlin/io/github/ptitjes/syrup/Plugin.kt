package io.github.ptitjes.syrup

import dev.whyoleg.sweetspi.Service
import org.kodein.di.DI

@Service
interface Plugin {
    val id: PluginId get() = PluginId(this::class.qualifiedName!!)

    val dependencies: Set<Plugin> get() = emptySet()

    fun DI.Builder.api()
    fun DI.Builder.implementation()
}
