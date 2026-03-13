package io.github.ptitjes.syrup.host.internal

import io.github.ptitjes.syrup.host.SomeOtherService
import io.github.ptitjes.syrup.host.SomeService
import io.github.ptitjes.syrup.host.SomeServiceBasedSomeOtherService
import io.github.ptitjes.syrup.host.SomeServiceFoo
import org.kodein.di.*
import org.kodein.di.bindings.ExternalSource
import org.kodein.type.generic
import kotlin.test.Test
import kotlin.test.assertEquals

class SimpleDITests {
    @Test
    fun `correctly injects Toto with string`() {
        val di = DI {
            bind<SomeService> { singleton { new(::SomeServiceFoo) } }

            externalSources += ExternalSource { key ->
                if (key.type == generic<String>()) return@ExternalSource { "titi" }
                null
            }
        }

        val toto by di.instance<SomeService>()
        assertEquals("titi", toto.value)
    }

    @Test
    fun `correctly injects Toto with string - recursive`() {
        val di = DI {
            bind<SomeOtherService> { singleton { new(::SomeServiceBasedSomeOtherService) } }
            bind<SomeService> { singleton { new(::SomeServiceFoo) } }

            externalSources += ExternalSource { key ->
                if (key.type == generic<String>()) return@ExternalSource { "titi" }
                null
            }
        }

        val toto by di.instance<SomeOtherService>()
        assertEquals("titi", toto.service.value)
    }
}
