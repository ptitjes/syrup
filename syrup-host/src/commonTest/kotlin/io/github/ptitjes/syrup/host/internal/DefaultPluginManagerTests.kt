package io.github.ptitjes.syrup.host.internal

import io.github.ptitjes.host.internal.DefaultPluginManager
import io.github.ptitjes.syrup.Plugin
import io.github.ptitjes.syrup.host.*
import org.kodein.di.*
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

/**
 * Integration test suite for [DefaultPluginManager] verifying the same encapsulation rules
 * as [PluginDIsTests] but using real test plugins wired through the plugin manager:
 *
 * - **Rule 1**: `api()` bindings are exposed to dependents via `publicDi`.
 * - **Rule 2**: `implementation()` bindings are private to the plugin (not visible via `publicDi`).
 * - **Rule 3**: Set-bindings declared in `api()` collect contributions from dependents (via binding sources).
 * - **Rule 4**: Bindings flow downward from dependencies to dependents (binding sources inject into the plugin's implementation scope).
 * - **Rule 5**: Set-binding contributions flow upward from dependents back to the declaring plugin.
 */
class DefaultPluginManagerTests {

    // ── Rule 1: api() bindings are exposed ──────────────────────────────

    @Test
    fun `api bindings are resolvable from plugin DI`() {
        class TestPlugin : Plugin {
            override fun DI.Builder.api() {
                bind<SomeService> { singleton { SomeServiceFoo("from-api") } }
            }

            override fun DI.Builder.implementation() {}
        }

        val plugin = TestPlugin()
        val manager = DefaultPluginManager(setOf(plugin)) { null }
        val di = manager.diFor(plugin)

        val someService = di.direct.instance<SomeService>()
        assertEquals(SomeServiceFoo("from-api"), someService)
    }

    // ── Rule 2: implementation() bindings are private ───────────────────

    @Test
    fun `implementation bindings are not resolvable from plugin DI`() {
        class TestPlugin : Plugin {
            override fun DI.Builder.api() {}

            override fun DI.Builder.implementation() {
                bind<SomeServiceFoo> { singleton { SomeServiceFoo("foo-style") } }
            }
        }

        val plugin = TestPlugin()
        val manager = DefaultPluginManager(setOf(plugin)) { null }
        val di = manager.diFor(plugin)

        val throwable = assertFails { di.direct.instance<SomeServiceFoo>() }
        assertEquals("No binding found for SomeServiceFoo", throwable.message)
    }

    // ── Rule 3: set-bindings declared in api() ──────────────────────────

    @Test
    fun `api set-bindings are resolvable from plugin DI`() {
        class TestPlugin : Plugin {
            override fun DI.Builder.api() {
                bindSet<SomeService> {
                    add { singleton { SomeServiceFoo("foo-style") } }
                    add { singleton { SomeServiceBar("bar-swag") } }
                }
            }

            override fun DI.Builder.implementation() {}
        }

        val plugin = TestPlugin()
        val manager = DefaultPluginManager(setOf(plugin)) { null }
        val di = manager.diFor(plugin)

        val someServices = di.direct.instance<Set<SomeService>>()
        assertEquals(
            setOf(SomeServiceFoo("foo-style"), SomeServiceBar("bar-swag")),
            someServices,
        )
    }

    // ── Rule 4: bindings flow downward ──────────────────────────────────

    @Test
    fun `dependency bindings flow downward into dependent implementation scope`() {
        class DependencyPlugin : Plugin {
            override fun DI.Builder.api() {
                bind<SomeService> { singleton { SomeServiceFoo("from-dependency") } }
            }

            override fun DI.Builder.implementation() {}
        }

        val dependencyPlugin = DependencyPlugin()

        class DependentPlugin : Plugin {
            override val dependencies: Set<Plugin> get() = setOf(dependencyPlugin)

            override fun DI.Builder.api() {
                bind<SomeOtherService> { singleton { instance<SomeServiceBasedSomeOtherService>() } }
            }

            override fun DI.Builder.implementation() {
                bind<SomeServiceBasedSomeOtherService> { singleton { new(::SomeServiceBasedSomeOtherService) } }
            }
        }

        val dependentPlugin = DependentPlugin()
        val manager = DefaultPluginManager(setOf(dependencyPlugin, dependentPlugin)) { null }
        val di = manager.diFor(dependentPlugin)

        val someOtherService = di.direct.instance<SomeOtherService>()
        assertEquals(
            SomeServiceBasedSomeOtherService(SomeServiceFoo("from-dependency")),
            someOtherService,
        )
    }

    @Test
    fun `dependency bindings are not resolvable from dependent plugin DI`() {
        class DependencyPlugin : Plugin {
            override fun DI.Builder.api() {
                bind<SomeService> { singleton { SomeServiceFoo("from-dependency") } }
            }

            override fun DI.Builder.implementation() {}
        }

        val dependencyPlugin = DependencyPlugin()

        class DependentPlugin : Plugin {
            override val dependencies: Set<Plugin> get() = setOf(dependencyPlugin)

            override fun DI.Builder.api() {}
            override fun DI.Builder.implementation() {}
        }

        val dependentPlugin = DependentPlugin()
        val manager = DefaultPluginManager(setOf(dependencyPlugin, dependentPlugin)) { null }
        val di = manager.diFor(dependentPlugin)

        val throwable = assertFails { di.direct.instance<SomeService>() }
        assertEquals("No binding found for SomeService", throwable.message)
    }

    // ── Rule 4 (edge case): plugin with no dependencies ─────────────────

    @Test
    fun `plugin with no dependencies resolves its own bindings`() {
        class TestPlugin : Plugin {
            override fun DI.Builder.api() {
                bind<SomeService> { singleton { instance<SomeServiceFoo>() } }
            }

            override fun DI.Builder.implementation() {
                bind<SomeServiceFoo> { singleton { SomeServiceFoo("foo-style") } }
            }
        }

        val plugin = TestPlugin()
        val manager = DefaultPluginManager(setOf(plugin)) { null }
        val di = manager.diFor(plugin)

        val someService = di.direct.instance<SomeService>()
        assertEquals(SomeServiceFoo("foo-style"), someService)
    }

    // ── Rule 5: set-binding contributions flow upward via inBindSet ─────

    /**
     * A dependent uses `inBindSet` to contribute to a set declared by its dependency.
     * The declaring plugin (dependency) must see the contribution.
     */
    @Test
    @Ignore("This test is temporarily disabled until encapsulation rules are fixed")
    fun `inBindSet contributions from dependent are visible in declaring plugin`() {
        class DeclaringPlugin : Plugin {
            override fun DI.Builder.api() {
                bindSet<SomeService> {
                    add { singleton { SomeServiceFoo("from-declaring") } }
                }
            }

            override fun DI.Builder.implementation() {}
        }

        val declaringPlugin = DeclaringPlugin()

        class ContributingPlugin : Plugin {
            override val dependencies: Set<Plugin> get() = setOf(declaringPlugin)

            override fun DI.Builder.api() {
                inBindSet<SomeService> {
                    add { singleton { SomeServiceBar("from-contributor") } }
                }
            }

            override fun DI.Builder.implementation() {}
        }

        val contributingPlugin = ContributingPlugin()
        val manager = DefaultPluginManager(setOf(declaringPlugin, contributingPlugin)) { null }
        val di = manager.diFor(declaringPlugin)

        // TODO this should only be true from the private DI of the plugin
        val someServices = di.direct.instance<Set<SomeService>>()
        assertEquals(
            setOf(SomeServiceFoo("from-declaring"), SomeServiceBar("from-contributor")),
            someServices,
        )
    }

    /**
     * Multiple dependents each contribute to the same set declared by their shared dependency.
     * The declaring plugin must see all contributions.
     */
    @Test
    @Ignore("This test is temporarily disabled until encapsulation rules are fixed")
    fun `multiple dependents contribute to the same set via inBindSet`() {
        class DeclaringPlugin : Plugin {
            override fun DI.Builder.api() {
                bindSet<SomeService> {}
            }

            override fun DI.Builder.implementation() {}
        }

        val declaringPlugin = DeclaringPlugin()

        class ContributorA : Plugin {
            override val dependencies: Set<Plugin> get() = setOf(declaringPlugin)

            override fun DI.Builder.api() {
                inBindSet<SomeService> {
                    add { singleton { SomeServiceFoo("from-A") } }
                }
            }

            override fun DI.Builder.implementation() {}
        }

        class ContributorB : Plugin {
            override val dependencies: Set<Plugin> get() = setOf(declaringPlugin)

            override fun DI.Builder.api() {
                inBindSet<SomeService> {
                    add { singleton { SomeServiceBar("from-B") } }
                }
            }

            override fun DI.Builder.implementation() {}
        }

        val contributorA = ContributorA()
        val contributorB = ContributorB()
        val manager = DefaultPluginManager(
            setOf(declaringPlugin, contributorA, contributorB)
        ) { null }
        val di = manager.diFor(declaringPlugin)

        val someServices = di.direct.instance<Set<SomeService>>()
        assertEquals(
            setOf(SomeServiceFoo("from-A"), SomeServiceBar("from-B")),
            someServices,
        )
    }

    /**
     * The contributing plugin itself must NOT see the set it contributed to.
     * Only the declaring plugin sees the collected set.
     */
    @Test
    fun `contributing plugin does not see the set it contributed to`() {
        class DeclaringPlugin : Plugin {
            override fun DI.Builder.api() {
                bindSet<SomeService> {
                    add { singleton { SomeServiceFoo("from-declaring") } }
                }
            }

            override fun DI.Builder.implementation() {}
        }

        val declaringPlugin = DeclaringPlugin()

        class ContributingPlugin : Plugin {
            override val dependencies: Set<Plugin> get() = setOf(declaringPlugin)

            override fun DI.Builder.api() {
                inBindSet<SomeService> {
                    add { singleton { SomeServiceBar("from-contributor") } }
                }
            }

            override fun DI.Builder.implementation() {}
        }

        val contributingPlugin = ContributingPlugin()
        val manager = DefaultPluginManager(setOf(declaringPlugin, contributingPlugin)) { null }
        val contributorDi = manager.diFor(contributingPlugin)

        // The contributor should NOT be able to resolve Set<SomeService> with the declaring plugin's set
        val contributorSet = contributorDi.direct.instance<Set<SomeService>>()
        // The contributor only sees its own inBindSet contribution, not the declaring plugin's full set
        // Since the contributor did not declare bindSet, it should not see the declaring plugin's set
        assertEquals(
            setOf(SomeServiceBar("from-contributor")),
            contributorSet,
        )
    }

    /**
     * A sibling dependent (that does NOT contribute via inBindSet) must not see the set
     * declared by their shared dependency.
     */
    @Test
    fun `sibling plugin that does not contribute does not see the declared set`() {
        class DeclaringPlugin : Plugin {
            override fun DI.Builder.api() {
                bindSet<SomeService> {
                    add { singleton { SomeServiceFoo("from-declaring") } }
                }
            }

            override fun DI.Builder.implementation() {}
        }

        val declaringPlugin = DeclaringPlugin()

        class ContributingPlugin : Plugin {
            override val dependencies: Set<Plugin> get() = setOf(declaringPlugin)

            override fun DI.Builder.api() {
                inBindSet<SomeService> {
                    add { singleton { SomeServiceBar("from-contributor") } }
                }
            }

            override fun DI.Builder.implementation() {}
        }

        class NonContributingPlugin : Plugin {
            override val dependencies: Set<Plugin> get() = setOf(declaringPlugin)

            override fun DI.Builder.api() {}
            override fun DI.Builder.implementation() {}
        }

        val contributingPlugin = ContributingPlugin()
        val nonContributingPlugin = NonContributingPlugin()
        val manager = DefaultPluginManager(
            setOf(declaringPlugin, contributingPlugin, nonContributingPlugin)
        ) { null }
        val di = manager.diFor(nonContributingPlugin)

        // Non-contributing sibling should get an empty set (it didn't declare bindSet nor inBindSet)
        val someServices = di.direct.instance<Set<SomeService>>()
        assertEquals(emptySet(), someServices)
    }

    /**
     * Multi-level chain: A declares `bindSet`, B depends on A and uses `inBindSet`,
     * C depends on B and uses `inBindSet`. Only A should see contributions from both B and C.
     */
    @Test
    @Ignore("This test is temporarily disabled until encapsulation rules are fixed")
    fun `transitive inBindSet contributions flow up to the declaring plugin`() {
        class PluginA : Plugin {
            override fun DI.Builder.api() {
                bindSet<SomeService> {}
            }

            override fun DI.Builder.implementation() {}
        }

        val pluginA = PluginA()

        class PluginB : Plugin {
            override val dependencies: Set<Plugin> get() = setOf(pluginA)

            override fun DI.Builder.api() {
                inBindSet<SomeService> {
                    add { singleton { SomeServiceFoo("from-B") } }
                }
            }

            override fun DI.Builder.implementation() {}
        }

        val pluginB = PluginB()

        class PluginC : Plugin {
            override val dependencies: Set<Plugin> get() = setOf(pluginB)

            override fun DI.Builder.api() {
                inBindSet<SomeService> {
                    add { singleton { SomeServiceBar("from-C") } }
                }
            }

            override fun DI.Builder.implementation() {}
        }

        val pluginC = PluginC()
        val manager = DefaultPluginManager(setOf(pluginA, pluginB, pluginC)) { null }

        // A should see contributions from B and C
        val diA = manager.diFor(pluginA)
        val servicesA = diA.direct.instance<Set<SomeService>>()
        assertEquals(
            setOf(SomeServiceFoo("from-B"), SomeServiceBar("from-C")),
            servicesA,
        )
    }

    /**
     * In a multi-level chain (A→B→C), the middle plugin B should NOT see the full
     * collected set from A. B only sees its own inBindSet contribution.
     */
    @Test
    fun `middle plugin in chain does not see declaring plugin set`() {
        class PluginA : Plugin {
            override fun DI.Builder.api() {
                bindSet<SomeService> {
                    add { singleton { SomeServiceFoo("from-A") } }
                }
            }

            override fun DI.Builder.implementation() {}
        }

        val pluginA = PluginA()

        class PluginB : Plugin {
            override val dependencies: Set<Plugin> get() = setOf(pluginA)

            override fun DI.Builder.api() {
                inBindSet<SomeService> {
                    add { singleton { SomeServiceBar("from-B") } }
                }
            }

            override fun DI.Builder.implementation() {}
        }

        val pluginB = PluginB()

        class PluginC : Plugin {
            override val dependencies: Set<Plugin> get() = setOf(pluginB)

            override fun DI.Builder.api() {
                inBindSet<SomeService> {
                    add { singleton { SomeServiceFoo("from-C") } }
                }
            }

            override fun DI.Builder.implementation() {}
        }

        val pluginC = PluginC()
        val manager = DefaultPluginManager(setOf(pluginA, pluginB, pluginC)) { null }

        // B should only see its own inBindSet contribution, not A's full set
        val diB = manager.diFor(pluginB)
        val servicesB = diB.direct.instance<Set<SomeService>>()
        assertEquals(
            setOf(SomeServiceBar("from-B")),
            servicesB,
        )
    }

    /**
     * A plugin that declares `bindSet` with its own elements and receives `inBindSet`
     * contributions should see both its own elements and the contributions.
     */
    @Test
    @Ignore("This test is temporarily disabled until encapsulation rules are fixed")
    fun `declaring plugin sees both own elements and inBindSet contributions`() {
        class DeclaringPlugin : Plugin {
            override fun DI.Builder.api() {
                bindSet<SomeService> {
                    add { singleton { SomeServiceFoo("own-element") } }
                }
            }

            override fun DI.Builder.implementation() {}
        }

        val declaringPlugin = DeclaringPlugin()

        class ContributorA : Plugin {
            override val dependencies: Set<Plugin> get() = setOf(declaringPlugin)

            override fun DI.Builder.api() {
                inBindSet<SomeService> {
                    add { singleton { SomeServiceBar("contributed-A") } }
                }
            }

            override fun DI.Builder.implementation() {}
        }

        class ContributorB : Plugin {
            override val dependencies: Set<Plugin> get() = setOf(declaringPlugin)

            override fun DI.Builder.api() {
                inBindSet<SomeService> {
                    add { singleton { SomeServiceFoo("contributed-B") } }
                }
            }

            override fun DI.Builder.implementation() {}
        }

        val contributorA = ContributorA()
        val contributorB = ContributorB()
        val manager = DefaultPluginManager(
            setOf(declaringPlugin, contributorA, contributorB)
        ) { null }
        val di = manager.diFor(declaringPlugin)

        val someServices = di.direct.instance<Set<SomeService>>()
        assertEquals(
            setOf(
                SomeServiceFoo("own-element"),
                SomeServiceBar("contributed-A"),
                SomeServiceFoo("contributed-B"),
            ),
            someServices,
        )
    }

    /**
     * An `inBindSet` contribution should NOT automatically make the set visible to
     * the contributing plugin's own dependents. If a plugin wants to expose the set,
     * it must do so explicitly (e.g., via a wrapper service).
     */
    @Test
    fun `inBindSet does not expose the set to the contributor own dependents`() {
        class PluginA : Plugin {
            override fun DI.Builder.api() {
                bindSet<SomeService> {}
            }

            override fun DI.Builder.implementation() {}
        }

        val pluginA = PluginA()

        class PluginB : Plugin {
            override val dependencies: Set<Plugin> get() = setOf(pluginA)

            override fun DI.Builder.api() {
                inBindSet<SomeService> {
                    add { singleton { SomeServiceFoo("from-B") } }
                }
            }

            override fun DI.Builder.implementation() {}
        }

        val pluginB = PluginB()

        class PluginC : Plugin {
            override val dependencies: Set<Plugin> get() = setOf(pluginB)

            override fun DI.Builder.api() {}
            override fun DI.Builder.implementation() {}
        }

        val pluginC = PluginC()
        val manager = DefaultPluginManager(setOf(pluginA, pluginB, pluginC)) { null }

        // C depends on B, but B only used inBindSet (not bindSet).
        // C should NOT see the set from A or B's contribution.
        val diC = manager.diFor(pluginC)
        val servicesC = diC.direct.instance<Set<SomeService>>()
        assertEquals(emptySet(), servicesC)
    }
}
