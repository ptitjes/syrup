plugins {
    id("syrup.conventions.kotlin-jvm")
    application
}
dependencies {
    implementation(project(":syrup-host"))
}

application {
    mainClass = "io.github.ptitjes.syrup.sample.app.MainKt"
}
