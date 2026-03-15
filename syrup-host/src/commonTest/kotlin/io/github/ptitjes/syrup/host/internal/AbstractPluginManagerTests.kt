package io.github.ptitjes.syrup.host.internal

import io.github.ptitjes.syrup.Plugin
import io.github.ptitjes.syrup.PluginId
import io.github.ptitjes.syrup.host.PluginManager
import io.github.ptitjes.syrup.specification.PluginSpecificationBuilder
import org.kodein.di.DI
import kotlin.reflect.KProperty

internal open class AbstractPluginManagerTests {
    protected fun runPluginManagerTest(
        plugins: Set<Plugin> = emptySet(),
        internalPluginBindings: DI.Builder.(Plugin) -> Unit = {},
        testBloc: DefaultPluginManager.() -> Unit = {},
    ) {
        val manager = DefaultPluginManager(plugins, internalPluginBindings)
        manager.testBloc()
    }

    protected fun testPlugin(
        name: String? = null,
        dependencies: Set<Plugin> = emptySet(),
        specification: PluginSpecificationBuilder.() -> Unit = {},
        implementation: DI.Builder.() -> Unit = {},
    ): TestPluginDelegate = TestPluginDelegate(name, dependencies, specification, implementation)

    protected class TestPluginDelegate(
        private val name: String? = null,
        private val dependencies: Set<Plugin> = emptySet(),
        private val specification: PluginSpecificationBuilder.() -> Unit = {},
        private val implementation: DI.Builder.() -> Unit = {},
    ) {
        private var plugin: Plugin? = null

        operator fun getValue(thisRef: Any?, property: KProperty<*>): Plugin {
            return plugin ?: TestPlugin(name ?: property.name, dependencies, specification, implementation).also {
                plugin = it
            }
        }
    }

    private class TestPlugin(
        private val name: String,
        override val dependencies: Set<Plugin>,
        private val specification: PluginSpecificationBuilder.() -> Unit,
        private val implementation: DI.Builder.() -> Unit
    ) : Plugin {
        override val id: PluginId get() = PluginId(name)

        override fun PluginSpecificationBuilder.specification() {
            apply(this@TestPlugin.specification)
        }

        override fun DI.Builder.implementation() {
            apply(this@TestPlugin.implementation)
        }

//        override fun toString(): String = "TestPlugin($name)"
    }
}