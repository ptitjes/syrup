plugins {
    alias(libs.plugins.kotlinJvm) apply false
}

val group = project.property("group") as String
val version = project.property("version") as String

subprojects.forEach {
    it.group = group
    it.version = version
}
