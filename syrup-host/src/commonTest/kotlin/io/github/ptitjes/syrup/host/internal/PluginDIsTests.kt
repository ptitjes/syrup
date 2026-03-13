package io.github.ptitjes.syrup.host.internal

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.matches
import dev.mokkery.mock
import io.github.ptitjes.host.internal.BindingSource
import io.github.ptitjes.host.internal.PluginDIs
import io.github.ptitjes.syrup.host.*
import org.kodein.di.*
import org.kodein.type.TypeToken
import org.kodein.type.generic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

/**
 * Comprehensive test suite for [PluginDIs] verifying the encapsulation rules:
 *
 * - **Rule 1**: `api()` bindings are exposed to dependents via `publicDi`.
 * - **Rule 2**: `implementation()` bindings are private to the plugin (not visible via `publicDi`).
 * - **Rule 3**: Set-bindings declared in `api()` collect contributions from dependents (via binding sources).
 * - **Rule 4**: Bindings flow downward from dependencies to dependents (binding sources inject into the plugin's implementation scope).
 * - **Rule 5**: Set-binding contributions flow upward from dependents back to the declaring plugin.
 */
class PluginDIsTests {

    /**
     * Covers **Rule 1**: `api()` bindings are exposed to dependents via `publicDi`.
     *
     * A service bound in the `api` scope must be resolvable from `publicDi`.
     */
    @Test
    fun `api bindings are resolvable from publicDi`() = runPluginDIsTest(
        api = {
            bind<SomeService> { singleton { SomeServiceFoo("from-api") } }
        },
    ) {
        val someService = publicDi.direct.instance<SomeService>()
        assertEquals(SomeServiceFoo("from-api"), someService)
    }

    /**
     * Covers **Rule 2 (negative)**: `implementation()` bindings are private and not visible via `publicDi`.
     *
     * A service bound only in the `implementation` scope must not be resolvable from `publicDi`.
     */
    @Test
    fun `implementation bindings are not resolvable from publicDi`() = runPluginDIsTest(
        implementation = {
            bind<SomeServiceFoo> { singleton { new(::SomeServiceFoo, "foo-style") } }
        },
    ) {
        val throwable = assertFails { publicDi.direct.instance<SomeServiceFoo>() }
        assertEquals("No binding found for SomeServiceFoo", throwable.message)
    }

    /**
     * Covers **Rule 2 (positive)**: `implementation()` bindings are accessible from the private scope.
     *
     * A service bound in the `implementation` scope must be resolvable from `privateDI`.
     */
    @Test
    fun `implementation bindings are resolvable from privateDI`() = runPluginDIsTest(
        implementation = {
            bind<SomeServiceFoo> { singleton { new(::SomeServiceFoo, "foo-style") } }
        },
    ) {
        val someServiceFoo = privateDI.direct.instance<SomeServiceFoo>()
        assertEquals(SomeServiceFoo("foo-style"), someServiceFoo)
    }

    /**
     * Covers **Rule 1 / Rule 2 (scope boundary)**: api bindings live only in the public scope and
     * are not accessible from the private scope.
     *
     * A service bound in the `api` scope must not be resolvable from `privateDI`, confirming that
     * the two scopes are properly isolated.
     */
    @Test
    fun `api bindings are not accessible via privateDI`() = runPluginDIsTest(
        api = {
            bind<SomeService> { singleton { SomeServiceFoo("from-api") } }
        },
    ) {
        val throwable = assertFails { privateDI.direct.instance<SomeService>() }
        assertEquals("No binding found for SomeService", throwable.message)
    }

    /**
     * Covers **Rule 3**: set-bindings declared in `api()` are visible via `publicDi`.
     *
     * A set-binding declared in the `api` scope, with individual implementations bound in
     * `implementation`, must be resolvable as a set from `publicDi`.
     */
    @Test
    fun `api set-bindings are resolvable from publicDi`() = runPluginDIsTest(
        api = {
            bindSet<SomeService> {
                addSingleton { instance<SomeServiceFoo>() }
                addSingleton { instance<SomeServiceBar>() }
            }
        },
        implementation = {
            bind<SomeServiceFoo> { singleton { new(::SomeServiceFoo, "foo-style") } }
            bind<SomeServiceBar> { singleton { new(::SomeServiceBar, "bar-swag") } }
        },
    ) {
        val someServices = publicDi.direct.instance<Set<SomeService>>()
        assertEquals(
            setOf(SomeServiceFoo("foo-style"), SomeServiceBar("bar-swag")),
            someServices,
        )
    }

    /**
     * Covers **Rule 2 + Rule 3**: individual implementation types used as set-binding contributors
     * are not independently resolvable from `publicDi`.
     *
     * Even though `SomeServiceFoo` and `SomeServiceBar` contribute to a set-binding declared in
     * `api()`, they must not be individually resolvable from `publicDi`.
     */
    @Test
    fun `implementation types do not leak through api set-bindings`() = runPluginDIsTest(
        api = {
            bindSet<SomeService> {
                addSingleton { instance<SomeServiceFoo>() }
                addSingleton { instance<SomeServiceBar>() }
            }
        },
        implementation = {
            bind<SomeServiceFoo> { singleton { new(::SomeServiceFoo, "foo-style") } }
            bind<SomeServiceBar> { singleton { new(::SomeServiceBar, "bar-swag") } }
        },
    ) {
        val throwableFoo = assertFails { publicDi.direct.instance<SomeServiceFoo>() }
        assertEquals("No binding found for SomeServiceFoo", throwableFoo.message)

        val throwableBar = assertFails { publicDi.direct.instance<SomeServiceBar>() }
        assertEquals("No binding found for SomeServiceBar", throwableBar.message)
    }

    /**
     * Covers **Rule 4**: bindings from a dependency (binding source) are injectable in the plugin's
     * implementation scope.
     *
     * A service provided by a binding source can be injected into an implementation binding, and the
     * resulting api binding resolves correctly from `publicDi`.
     */
    @Test
    fun `binding sources flow downward into implementation scope`() = runPluginDIsTest(
        api = {
            bind<SomeOtherService> { singleton { instance<SomeServiceBasedSomeOtherService>() } }
        },
        implementation = {
            bind<SomeServiceBasedSomeOtherService> { singleton { new(::SomeServiceBasedSomeOtherService) } }
        },
        bindingSource = mock {
            every { factories(anyKey()) } returns emptySequence()
            every { factories(anyKeyWithType<SomeService>()) } returns sequenceOf(
                bindingFactory { _: Unit -> SomeServiceFoo("from-binding-source") }
            )
        },
    ) { _ ->
        val someOtherService = publicDi.direct.instance<SomeOtherService>()
        assertEquals(
            SomeServiceBasedSomeOtherService(SomeServiceFoo("from-binding-source")),
            someOtherService,
        )
    }

    /**
     * Covers **Rule 4 (direct access)**: a binding source service can be resolved directly from
     * `privateDI`, not only through another binding.
     *
     * A service provided by a binding source must be directly resolvable from `privateDI`.
     */
    @Test
    fun `binding source services are directly resolvable from privateDI`() = runPluginDIsTest(
        bindingSource = mock {
            every { factories(anyKey()) } returns emptySequence()
            every { factories(anyKeyWithType<SomeService>()) } returns sequenceOf(
                bindingFactory { _: Unit -> SomeServiceFoo("from-binding-source") }
            )
        },
    ) { _ ->
        val someService = privateDI.direct.instance<SomeService>()
        assertEquals(SomeServiceFoo("from-binding-source"), someService)
    }

    /**
     * Covers **Rule 2**: services provided by a binding source are private and not directly
     * resolvable from `publicDi`.
     *
     * A service provided only by a binding source (not declared in `api()`) must not be
     * resolvable from `publicDi`.
     */
    @Test
    fun `binding source services are not resolvable from publicDi`() = runPluginDIsTest(
        bindingSource = mock {
            every { factories(anyKey()) } returns emptySequence()
            every { factories(anyKeyWithType<SomeService>()) } returns sequenceOf(
                bindingFactory { _: Unit -> SomeServiceFoo("from-binding-source") }
            )
        },
    ) { _ ->
        val throwable = assertFails { publicDi.direct.instance<SomeService>() }
        assertEquals("No binding found for SomeService", throwable.message)
    }

    /**
     * Covers **Rule 5 (upward flow scope)**: binding source contributions do not automatically
     * merge into a set-binding declared in `api()`.
     *
     * When `api()` declares a set-binding with one element and a binding source provides a service
     * of the same type, only the explicitly declared api contributions appear in the resolved set.
     * Binding source services remain in the private scope and are not added to api set-bindings.
     */
    @Test
    fun `binding source contributions do not merge into api set-bindings`() = runPluginDIsTest(
        api = {
            bindSet<SomeService> {
                addSingleton { SomeServiceFoo("from-api") }
            }
        },
        bindingSource = mock {
            every { factories(anyKey()) } returns emptySequence()
            every { factories(anyKeyWithType<SomeService>()) } returns sequenceOf(
                bindingFactory { _: Unit -> SomeServiceBar("from-binding-source") }
            )
        },
    ) { _ ->
        val someServices = publicDi.direct.instance<Set<SomeService>>()
        assertEquals(
            setOf(SomeServiceFoo("from-api")),
            someServices,
        )
    }

    /**
     * Covers **Rule 2 + Rule 5**: a set-binding contributed only by a binding source (with no
     * `bindSet` in `api()`) resolves to an empty set from `publicDi`.
     *
     * Without an explicit set-binding declaration in `api()`, binding source contributions remain
     * private. The resolved set from `publicDi` is empty.
     */
    @Test
    fun `binding source set-contributions are not public without api declaration`() = runPluginDIsTest(
        bindingSource = mock {
            every { factories(anyKey()) } returns emptySequence()
            every { factories(anyKeyWithType<SomeService>()) } returns sequenceOf(
                bindingFactory { _: Unit -> SomeServiceFoo("from-binding-source") }
            )
        },
    ) { _ ->
        val someServices = publicDi.direct.instance<Set<SomeService>>()
        assertEquals(emptySet(), someServices)
    }

    /**
     * Covers **Rule 4 (edge case)**: a present but empty binding source does not interfere with
     * normal resolution.
     *
     * When a binding source is present but returns no factories for any requested key, the plugin's
     * own bindings must still resolve correctly.
     */
    @Test
    fun `empty binding source does not cause errors`() = runPluginDIsTest(
        api = {
            bind<SomeService> { singleton { instance<SomeServiceFoo>() } }
        },
        implementation = {
            bind<SomeServiceFoo> { singleton { new(::SomeServiceFoo, "foo-style") } }
        },
        bindingSource = mock {
            every { factories(anyKey()) } returns emptySequence()
        },
    ) { _ ->
        val someService = publicDi.direct.instance<SomeService>()
        assertEquals(SomeServiceFoo("foo-style"), someService)
    }

    fun runPluginDIsTest(
        api: DI.Builder.() -> Unit = {},
        implementation: DI.Builder.() -> Unit = {},
        bindingSourceProvider: (() -> BindingSource)? = null,
        test: PluginDIs.() -> Unit,
    ) = test(
        PluginDIs(
            name = "test",
            api = api,
            implementation = implementation,
            bindingSourceProvider = bindingSourceProvider,
        )
    )

    fun runPluginDIsTest(
        api: DI.Builder.() -> Unit = {},
        implementation: DI.Builder.() -> Unit = {},
        bindingSource: BindingSource,
        test: PluginDIs.(bindingSource: BindingSource) -> Unit,
    ) = runPluginDIsTest(
        api = api,
        implementation = implementation,
        bindingSourceProvider = { bindingSource },
        test = { test(bindingSource) },
    )
}

fun <A, T : Any> bindingFactory(f: (A) -> T): (A) -> T = f

inline fun <reified T : Any> MokkeryMatcherScope.anyKeyWithType(): DI.Key<*, *, *> =
    anyKeyWithType(generic<T>())

fun MokkeryMatcherScope.anyKeyWithType(
    type: TypeToken<*>,
): DI.Key<*, *, *> = matches(
    toString = { "anyKeyWithType(${type.simpleDispString()})" },
    predicate = { it.type == type }
)

fun MokkeryMatcherScope.anyKey(): DI.Key<*, *, *> = matches(
    toString = { "anyKey()" },
    predicate = { true }
)
