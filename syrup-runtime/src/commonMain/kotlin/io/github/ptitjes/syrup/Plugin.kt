package io.github.ptitjes.syrup

import org.kodein.di.DI

interface Plugin {
    val id: PluginId get() = PluginId(this::class.qualifiedName!!)

    val dependencies: Set<Plugin> get() = emptySet()

    fun DI.Builder.api()
    fun DI.Builder.implementation()
}
