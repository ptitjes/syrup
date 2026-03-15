package io.github.ptitjes.syrup.host

interface Logger {
    fun trace(messageBuilder: () -> String)
    fun debug(messageBuilder: () -> String)
    fun info(messageBuilder: () -> String)
    fun warn(messageBuilder: () -> String)
    fun error(messageBuilder: () -> String)
    fun error(throwable: Throwable, messageBuilder: () -> String)

    enum class Level(internal val level: Int) {
        Error(0),
        Warn(1),
        Info(2),
        Debug(3),
        Trace(4),
    }

    companion object {
        fun consoleLogger(
            minimumLevel: Level = Level.Info,
            tag: String = "Syrup",
        ): Logger = object : Logger {
            private fun log(level: Level, messageBuilder: () -> String) {
                if (level.level <= minimumLevel.level) println("[$tag] ${messageBuilder()}")
            }

            override fun trace(messageBuilder: () -> String) = log(Level.Trace, messageBuilder)
            override fun debug(messageBuilder: () -> String) = log(Level.Debug, messageBuilder)
            override fun info(messageBuilder: () -> String) = log(Level.Info, messageBuilder)
            override fun warn(messageBuilder: () -> String) = log(Level.Warn, messageBuilder)
            override fun error(messageBuilder: () -> String) = log(Level.Error, messageBuilder)
            override fun error(throwable: Throwable, messageBuilder: () -> String) = error {
                messageBuilder() + "\n" + throwable.stackTraceToString()
            }
        }
    }
}
