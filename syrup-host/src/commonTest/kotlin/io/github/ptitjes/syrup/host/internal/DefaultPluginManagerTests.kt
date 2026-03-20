package io.github.ptitjes.syrup.host.internal

import io.github.ptitjes.syrup.PluginContext
import io.github.ptitjes.syrup.Sourced
import io.github.ptitjes.syrup.host.SomeService
import io.github.ptitjes.syrup.host.SomeServiceFoo
import io.github.ptitjes.syrup.specification.ExtensionPoint
import org.kodein.di.bind
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.singleton
import org.kodein.type.generic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

/**
 * Integration test suite for [DefaultPluginManager] verifying Syrup's encapsulation rules.
 *
 * @see docs/encapsulation-rules.md
 */
internal class DefaultPluginManagerTests : AbstractPluginManagerTests() {

    // region Internal bindings

    @Test
    fun `internal bindings are not visible from the main DI`() {
        val pluginA by testPlugin(
            implementation = {
                bind<SomeService> { singleton { SomeServiceFoo("from-pluginA") } }
            })

        runPluginManagerTest(
            plugins = setOf(pluginA),
        ) {
            val throwable = assertFails { mainDi.direct.instance<SomeService>() }
            assertEquals("No binding found for SomeService", throwable.message)
        }
    }

    @Test
    fun `internal bindings are visible from the plugin's internal DI`() {
        val pluginA by testPlugin(
            implementation = {
                bind<SomeService> { singleton { SomeServiceFoo("from-pluginA") } }
            })

        runPluginManagerTest(
            plugins = setOf(pluginA),
        ) {
            val someService = internalDiFor(pluginA).direct.instance<SomeService>()
            assertEquals(SomeServiceFoo("from-pluginA"), someService)
        }
    }

    @Test
    fun `internal bindings are not visible from dependent plugins's internal DI`() {
        val pluginA by testPlugin(
            implementation = {
                bind<SomeService> { singleton { SomeServiceFoo("from-pluginA") } }
            }
        )

        val pluginB by testPlugin(
            dependencies = setOf(pluginA),
        )

        runPluginManagerTest(
            plugins = setOf(pluginA, pluginB),
        ) {
            val throwable = assertFails { internalDiFor(pluginB).direct.instance<SomeService>() }
            assertEquals("No binding found for SomeService", throwable.message)
        }
    }

    // endregion

    // region Exposed types

    @Test
    fun `exposed types are visible from the main DI`() {
        val pluginA by testPlugin(
            specification = {
                exposedType<SomeService> { SomeServiceFoo("from-pluginA") }
            }
        )

        runPluginManagerTest(
            plugins = setOf(pluginA),
        ) {
            val someService = mainDi.direct.instance<SomeService>()
            assertEquals(SomeServiceFoo("from-pluginA"), someService)
        }
    }

    @Test
    fun `exposed types are not visible from the plugin's internal DI`() {
        val pluginA by testPlugin(
            specification = {
                exposedType<SomeService> { SomeServiceFoo("from-pluginA") }
            })

        runPluginManagerTest(
            plugins = setOf(pluginA),
        ) {
            val throwable = assertFails { internalDiFor(pluginA).direct.instance<SomeService>() }
            assertEquals("No binding found for SomeService", throwable.message)
        }
    }

    @Test
    fun `exposed types are visible from dependent plugins's internal DI`() {
        val pluginA by testPlugin(
            specification = {
                exposedType<SomeService> { SomeServiceFoo("from-pluginA") }
            }
        )

        val pluginB by testPlugin(
            dependencies = setOf(pluginA),
        )

        runPluginManagerTest(
            plugins = setOf(pluginA, pluginB),
        ) {
            val someService = internalDiFor(pluginB).direct.instance<SomeService>()
            assertEquals(SomeServiceFoo("from-pluginA"), someService)
        }
    }

    // endregion

    // region Singular contributions

    @Test
    fun `singular contributions are visible from the plugin's context`() {
        val myExtensionPoint = ExtensionPoint.Singular<SomeService>(generic())

        val pluginA by testPlugin(
            specification = {
                extensionPoint(myExtensionPoint)

                myExtensionPoint {
                    contribution { SomeServiceFoo("from-pluginA") }
                }
            }
        )

        runPluginManagerTest(
            plugins = setOf(pluginA),
        ) {
            val pluginContext = internalDiFor(pluginA).direct.instance<PluginContext>()

            val service by pluginContext.contribution(myExtensionPoint)
            assertEquals(SomeServiceFoo("from-pluginA"), service)

            val sourcedService by pluginContext.sourcedContribution(myExtensionPoint)
            assertEquals(Sourced<SomeService>(pluginA, SomeServiceFoo("from-pluginA")), sourcedService)
        }
    }

    @Test
    fun `singular contributions are not visible from the plugin's internal DI`() {
        val myExtensionPoint = ExtensionPoint.Singular<SomeService>(generic())

        val pluginA by testPlugin(
            specification = {
                extensionPoint(myExtensionPoint)

                myExtensionPoint {
                    contribution { SomeServiceFoo("from-pluginA") }
                }
            }
        )

        runPluginManagerTest(
            plugins = setOf(pluginA),
        ) {
            val throwable = assertFails { internalDiFor(pluginA).direct.instance<SomeService>() }
            assertEquals("No binding found for SomeService", throwable.message)
        }
    }

    @Test
    fun `singular contributions are not visible from the main DI`() {
        val myExtensionPoint = ExtensionPoint.Singular<SomeService>(generic())

        val pluginA by testPlugin(
            specification = {
                extensionPoint(myExtensionPoint)

                myExtensionPoint {
                    contribution { SomeServiceFoo("from-pluginA") }
                }
            }
        )

        runPluginManagerTest(
            plugins = setOf(pluginA),
        ) {
            val throwable = assertFails { mainDi.direct.instance<SomeService>() }
            assertEquals("No binding found for SomeService", throwable.message)
        }
    }

    @Test
    fun `optional singular extension point without contribution does not throw a validation error`() {
        val myExtensionPoint = ExtensionPoint.Singular<SomeService>(generic())

        val pluginA by testPlugin(
            specification = {
                extensionPoint(myExtensionPoint, optional = true)
            }
        )

        runPluginManagerTest(
            plugins = setOf(pluginA),
        ) {
            val pluginContext = internalDiFor(pluginA).direct.instance<PluginContext>()
            val service by pluginContext.contributionOrNull(myExtensionPoint)
            assertEquals(null, service)

            val sourcedService by pluginContext.sourcedContributionOrNull(myExtensionPoint)
            assertEquals(null, sourcedService)
        }
    }

    @Test
    fun `non-optional singular extension point without contribution throws a validation error`() {
        val myExtensionPoint = ExtensionPoint.Singular<SomeService>(generic())

        val pluginA by testPlugin(
            specification = {
                extensionPoint(myExtensionPoint)
            }
        )

        val throwable = assertFails { runPluginManagerTest(plugins = setOf(pluginA)) }
        assertEquals(
            "Non-optional singular extension point io.github.ptitjes.syrup.host.SomeService owned by pluginA has no contributions",
            throwable.message
        )
    }

    // endregion

    // region Plural contributions

    @Test
    fun `plural contributions are visible from the plugin's context`() {
        val myExtensionPoint = ExtensionPoint.Plural<SomeService>(generic())

        val pluginA by testPlugin(
            specification = {
                extensionPoint(myExtensionPoint)

                myExtensionPoint {
                    contribution { SomeServiceFoo("from-pluginA") }
                }
            }
        )

        runPluginManagerTest(
            plugins = setOf(pluginA),
        ) {
            val pluginContext = internalDiFor(pluginA).direct.instance<PluginContext>()
            val services by pluginContext.contributions(myExtensionPoint)
            assertEquals(setOf(SomeServiceFoo("from-pluginA")), services)

            val sourcedServices by pluginContext.sourcedContributions(myExtensionPoint)
            assertEquals(setOf(Sourced<SomeService>(pluginA, SomeServiceFoo("from-pluginA"))), sourcedServices)
        }
    }

    @Test
    fun `plural contributions are aggregated from all of the plugin's dependents`() {
        val myExtensionPoint = ExtensionPoint.Plural<SomeService>(generic())

        val pluginA by testPlugin(
            specification = {
                extensionPoint(myExtensionPoint)

                myExtensionPoint {
                    contribution { SomeServiceFoo("from-pluginA") }
                }
            }
        )

        val pluginB by testPlugin(
            dependencies = setOf(pluginA),
            specification = {
                myExtensionPoint {
                    contribution { SomeServiceFoo("from-pluginB") }
                }
            }
        )

        val pluginC by testPlugin(
            dependencies = setOf(pluginA),
            specification = {
                myExtensionPoint {
                    contribution { SomeServiceFoo("from-pluginC") }
                }
            }
        )

        runPluginManagerTest(
            plugins = setOf(pluginA, pluginB, pluginC),
        ) {
            val pluginContext = internalDiFor(pluginA).direct.instance<PluginContext>()

            val services by pluginContext.contributions(myExtensionPoint)
            assertEquals(
                setOf(
                    SomeServiceFoo("from-pluginA"),
                    SomeServiceFoo("from-pluginB"),
                    SomeServiceFoo("from-pluginC"),
                ),
                services,
            )

            val sourcedServices by pluginContext.sourcedContributions(myExtensionPoint)
            assertEquals(
                setOf(
                    Sourced<SomeService>(pluginA, SomeServiceFoo("from-pluginA")),
                    Sourced<SomeService>(pluginB, SomeServiceFoo("from-pluginB")),
                    Sourced<SomeService>(pluginC, SomeServiceFoo("from-pluginC")),
                ),
                sourcedServices,
            )
        }
    }

    @Test
    fun `plural contributions are not visible from the plugin's internal DI`() {
        val myExtensionPoint = ExtensionPoint.Plural<SomeService>(generic())

        val pluginA by testPlugin(
            specification = {
                extensionPoint(myExtensionPoint)

                myExtensionPoint {
                    contribution { SomeServiceFoo("from-pluginA") }
                }
            }
        )

        runPluginManagerTest(
            plugins = setOf(pluginA),
        ) {
            val throwable = assertFails { internalDiFor(pluginA).direct.instance<Set<SomeService>>() }
            assertEquals("No binding found for Set<out SomeService>", throwable.message)
        }
    }

    @Test
    fun `plural contributions are not visible from the main DI`() {
        val myExtensionPoint = ExtensionPoint.Plural<SomeService>(generic())

        val pluginA by testPlugin(
            specification = {
                extensionPoint(myExtensionPoint)

                myExtensionPoint {
                    contribution { SomeServiceFoo("from-pluginA") }
                }
            }
        )

        runPluginManagerTest(
            plugins = setOf(pluginA),
        ) {
            val throwable = assertFails { mainDi.direct.instance<Set<SomeService>>() }
            assertEquals("No binding found for Set<out SomeService>", throwable.message)
        }
    }

    @Test
    fun `optional plural extension point without contribution does not throw a validation error`() {
        val myExtensionPoint = ExtensionPoint.Plural<SomeService>(generic())

        val pluginA by testPlugin(
            specification = {
                extensionPoint(myExtensionPoint, optional = true)
            }
        )

        runPluginManagerTest(
            plugins = setOf(pluginA),
        ) {
            val pluginContext = internalDiFor(pluginA).direct.instance<PluginContext>()

            val services by pluginContext.contributions(myExtensionPoint)
            assertEquals(setOf(), services)

            val sourcedServices by pluginContext.sourcedContributions(myExtensionPoint)
            assertEquals(setOf(), sourcedServices)
        }
    }

    @Test
    fun `non-optional plural extension point without contribution throws a validation error`() {
        val myExtensionPoint = ExtensionPoint.Plural<SomeService>(generic())

        val pluginA by testPlugin(
            specification = {
                extensionPoint(myExtensionPoint)
            }
        )

        val throwable = assertFails { runPluginManagerTest(plugins = setOf(pluginA)) }
        assertEquals(
            "Non-optional plural extension point io.github.ptitjes.syrup.host.SomeService owned by pluginA has no contributions",
            throwable.message
        )
    }

    // endregion
}
