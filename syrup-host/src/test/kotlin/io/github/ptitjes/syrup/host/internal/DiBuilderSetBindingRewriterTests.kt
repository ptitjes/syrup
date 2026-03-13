package io.github.ptitjes.syrup.host.internal

import io.github.ptitjes.host.internal.collectBoundTypes
import org.kodein.di.DI
import org.kodein.di.inBindSet
import org.kodein.di.singleton
import org.kodein.type.TypeToken
import org.kodein.type.generic
import kotlin.test.Test
import kotlin.test.assertEquals

class DiBuilderSetBindingRewriterTests {
    @Test
    fun `correctly rewrites inBindSets`() {
        val module = DI.Module("test") {
            inBindSet<String> {
                add { singleton { "test-singleton" } }
            }
        }

        val boundSetTypes = collectBoundTypes(module)

        assertEquals<Map<TypeToken<*>, Boolean>>(
            expected = mapOf(generic<String>() to false),
            actual = boundSetTypes,
        )
    }
}
