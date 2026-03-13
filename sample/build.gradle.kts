import dev.whyoleg.sweetspi.gradle.withSweetSpi

plugins {
    id("syrup.conventions.kotlin-jvm")
    alias(libs.plugins.ksp)
    alias(libs.plugins.sweetSpi)
    application
}

kotlin {
    withSweetSpi()
}

dependencies {
    implementation(project(":syrup-host"))
}

application {
    mainClass = "io.github.ptitjes.syrup.sample.app.MainKt"
}
