package io.github.ptitjes.syrup.host

/**
 * A logger interface for plugin assembly and management.
 * Allows tracing the contribution resolution process.
 */
public interface Logger {
    /**
     * Logs a message at the TRACE level.
     */
    public fun trace(messageBuilder: () -> String)

    /**
     * Logs a message at the DEBUG level.
     */
    public fun debug(messageBuilder: () -> String)

    /**
     * Logs a message at the INFO level.
     */
    public fun info(messageBuilder: () -> String)

    /**
     * Logs a message at the WARN level.
     */
    public fun warn(messageBuilder: () -> String)

    /**
     * Logs a message at the ERROR level.
     */
    public fun error(messageBuilder: () -> String)

    /**
     * Logs a message and an exception at the ERROR level.
     */
    public fun error(throwable: Throwable, messageBuilder: () -> String)

    /**
     * Logging levels.
     */
    public enum class Level(internal val level: Int) {
        None(0),
        Error(1),
        Warn(2),
        Info(3),
        Debug(4),
        Trace(5),
    }

    public companion object {
        /**
         * Creates a logger that prints to the console.
         *
         * @param minimumLevel The minimum level of messages to log.
         * @param tag The tag to use for log messages.
         */
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
