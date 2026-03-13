package io.github.ptitjes.syrup.host

interface SomeService {
    val value: String
}

data class SomeServiceFoo(override val value: String) : SomeService
data class SomeServiceBar(override val value: String) : SomeService

interface SomeOtherService {
    val service: SomeService
}

data class SomeServiceBasedSomeOtherService(override val service: SomeService) : SomeOtherService
