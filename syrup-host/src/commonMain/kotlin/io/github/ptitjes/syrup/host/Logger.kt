package io.github.ptitjes.syrup.host

public interface Logger {
    public fun trace(messageBuilder: () -> String)
    public fun debug(messageBuilder: () -> String)
    public fun info(messageBuilder: () -> String)
    public fun warn(messageBuilder: () -> String)
    public fun error(messageBuilder: () -> String)
    public fun error(throwable: Throwable, messageBuilder: () -> String)

    public enum class Level(internal val level: Int) {
        None(0),
        Error(1),
        Warn(2),
        Info(3),
        Debug(4),
        Trace(5),
    }

    public companion object {
        public fun consoleLogger(
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
