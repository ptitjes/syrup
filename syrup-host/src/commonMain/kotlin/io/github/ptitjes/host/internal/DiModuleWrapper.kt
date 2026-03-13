package io.github.ptitjes.host.internal

import org.kodein.di.DI

internal fun DI.Module.rename(name: String): DI.Module {
    return DI.Module(
        name = name,
        allowSilentOverride = this.allowSilentOverride,
        prefix = this.prefix,
        init = this.init,
    )
}

internal fun DI.Module.wrap(wrapper: (DI.Builder) -> DI.Builder): DI.Module {
    return DI.Module(
        name = this.name,
        allowSilentOverride = this.allowSilentOverride,
        prefix = this.prefix,
    ) {
        wrapper(this).apply { init() }
    }
}
