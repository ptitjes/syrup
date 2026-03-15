package io.github.ptitjes.syrup

import dev.whyoleg.sweetspi.Service
import io.github.ptitjes.syrup.specification.PluginSpecificationBuilder
import org.kodein.di.DI

@Service
interface Plugin {
    val id: PluginId get() = PluginId(this::class.simpleName!!)

    val dependencies: Set<Plugin> get() = emptySet()

    fun PluginSpecificationBuilder.specification() {}
    fun DI.Builder.implementation() {}
}
