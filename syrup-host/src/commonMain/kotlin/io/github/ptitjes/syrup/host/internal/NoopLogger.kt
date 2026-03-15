package io.github.ptitjes.syrup.host.internal

import io.github.ptitjes.syrup.host.Logger

internal object NoopLogger : Logger {
    override fun trace(messageBuilder: () -> String) = Unit
    override fun debug(messageBuilder: () -> String) = Unit
    override fun info(messageBuilder: () -> String) = Unit
    override fun warn(messageBuilder: () -> String) = Unit
    override fun error(messageBuilder: () -> String) = Unit
    override fun error(throwable: Throwable, messageBuilder: () -> String) = Unit
}
