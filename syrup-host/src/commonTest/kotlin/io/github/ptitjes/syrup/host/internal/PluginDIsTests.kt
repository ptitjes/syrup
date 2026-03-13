package io.github.ptitjes.syrup.host.internal

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.matches
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exhaustive
import io.github.ptitjes.host.internal.BindingSource
import io.github.ptitjes.host.internal.PluginDIs
import io.github.ptitjes.syrup.host.*
import org.kodein.di.*
import org.kodein.type.TypeToken
import org.kodein.type.generic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class PluginDIsTests {
    @Test
    fun `correctly injects api service binding with string`() = runPluginDIsTest(
        api = {
            bind<SomeService> { singleton { instance<SomeServiceFoo>() } }
        },
        implementation = {
            bind<SomeServiceFoo> { singleton { new(::SomeServiceFoo, "foo-style") } }
        },
    ) {
        val someService = publicDi.direct.instance<SomeService>()
        assertEquals(SomeServiceFoo("foo-style"), someService)

        val throwable = assertFails { publicDi.direct.instance<SomeServiceFoo>() }
        assertEquals("No binding found for SomeServiceFoo", throwable.message)
    }

    @Test
    fun `correctly injects api service set-binding with string`() = runPluginDIsTest(
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
            setOf(SomeServiceFoo(value = "foo-style"), SomeServiceBar(value = "bar-swag")),
            someServices,
        )

        val throwable = assertFails { publicDi.direct.instance<SomeServiceFoo>() }
        assertEquals("No binding found for SomeServiceFoo", throwable.message)
    }

    @Test
    fun `local service is retrieved with injected bindings from providers`() = runPluginDIsTest(
        api = {
            bind<SomeOtherService> { singleton { instance<SomeServiceBasedSomeOtherService>() } }
        },
        implementation = {
            bind<SomeServiceBasedSomeOtherService> { singleton { new(::SomeServiceBasedSomeOtherService) } }
        },
        bindingSource = mock {
            every { factories(anyKey()) } returns emptySequence()
            every { factories(anyKeyWithType<SomeService>()) } returns sequenceOf(
                bindingFactory { _: Unit -> SomeServiceFoo("foo-style") }
            )
        },
    ) { bindingSource ->

        // We cannot publicly instantiate a service from a binding source provider
        val throwable = assertFails { publicDi.direct.instance<SomeService>() }
        assertEquals("No binding found for SomeService", throwable.message)

        // We can be injected a service from a binding source provider
        val someOtherService = publicDi.direct.instance<SomeOtherService>()

        verify(exhaustive) {
            bindingSource.factories(anyKeyWithType<SomeServiceBasedSomeOtherService>())
            bindingSource.factories(anyKeyWithType<SomeService>())
        }

        assertEquals(
            SomeServiceBasedSomeOtherService(SomeServiceFoo(value = "foo-style")),
            someOtherService,
        )
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
