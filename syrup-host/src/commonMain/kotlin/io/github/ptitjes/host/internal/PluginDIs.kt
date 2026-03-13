package io.github.ptitjes.host.internal

import org.kodein.di.DI

class PluginDIs(
    name: String,
    api: DI.Builder.() -> Unit = {},
    implementation: DI.Builder.() -> Unit = {},
    bindingSourceProvider: (() -> BindingSource)? = null,
) {
    private val apiModule = DI.Module(name = "$name#api", init = api).rewriteWithCallback()
    private val implementationModule = DI.Module(name = "$name#implementation", init = implementation)

    val privateDI = ModularDI(
        debugName = "$name#internal",
        sourceDi = DI.Companion { import(implementationModule) },
        callbackDi = null,
        bindingSourceProvider = bindingSourceProvider,
    )

    val publicDi = ModularDI(
        debugName = "$name#public",
        sourceDi = DI.Companion { import(apiModule) },
        callbackDi = privateDI,
    )
}
