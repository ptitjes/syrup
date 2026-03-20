package io.github.ptitjes.syrup

import dev.whyoleg.sweetspi.Service
import io.github.ptitjes.syrup.specification.PluginSpecificationBuilder
import org.kodein.di.DI

@Service
public interface Plugin {
    public val id: PluginId get() = PluginId(this::class.simpleName!!)

    public val dependencies: Set<Plugin> get() = emptySet()

    public fun PluginSpecificationBuilder.specification() {}

    public fun DI.Builder.implementation() {}
}
