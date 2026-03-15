package io.github.ptitjes.syrup.host.internal

import io.github.ptitjes.syrup.Plugin
import io.github.ptitjes.syrup.PluginId
import io.github.ptitjes.syrup.host.SomeService
import io.github.ptitjes.syrup.host.SomeServiceFoo
import io.github.ptitjes.syrup.specification.PluginSpecification
import io.github.ptitjes.syrup.specification.PluginSpecificationBuilder
import org.kodein.di.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

/**
 * Comprehensive test suite for [PluginScaffold] verifying the encapsulation rules:
 */
internal class PluginScaffoldTests {

    @Test
    fun `internal bindings are resolvable from internal DI`() = runPluginScaffoldTest(
        implementation = {
            bind<SomeService> { singleton { SomeServiceFoo("from-internal") } }
        },
    ) {
        val someService = internalDi.direct.instance<SomeService>()
        assertEquals(SomeServiceFoo("from-internal"), someService)
    }

    @Test
    fun `exposed types are not visible from internal DI`() = runPluginScaffoldTest(
        specification = {
            exposedType<SomeService> { SomeServiceFoo("from-api") }
        },
    ) {
        val throwable = assertFails { internalDi.direct.instance<SomeService>() }
        assertEquals("No binding found for SomeService", throwable.message)
    }

    fun runPluginScaffoldTest(
        specification: PluginSpecificationBuilder.() -> Unit = {},
        implementation: DI.Builder.() -> Unit = {},
        test: PluginScaffold.() -> Unit,
    ) {
        val plugin = TestPlugin(specification, implementation)
        val specification = PluginSpecification { plugin.apply { specification() } }
        val pluginScaffold = PluginScaffold(plugin, specification, implementation)
        pluginScaffold.wire(emptyList(), emptyList())
        test(pluginScaffold)
    }
}

private class TestPlugin(
    private val specification: PluginSpecificationBuilder.() -> Unit,
    private val implementation: DI.Builder.() -> Unit
) : Plugin {
    override val id: PluginId get() = PluginId("TestPlugin")
    override val dependencies: Set<Plugin> get() = emptySet()

    override fun PluginSpecificationBuilder.specification() {
        apply(this@TestPlugin.specification)
    }

    override fun DI.Builder.implementation() {
        apply(this@TestPlugin.implementation)
    }
}
