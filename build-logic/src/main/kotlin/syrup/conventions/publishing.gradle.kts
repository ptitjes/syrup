package syrup.conventions

plugins {
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    coordinates(
        groupId = project.group.toString(),
        artifactId = project.name,
        version = project.version.toString(),
    )

    pom {
        name.set("Syrup")
        description.set("Syrup is fizzy mix of sweet-spi and kodein-di.")
        inceptionYear.set("2026")
        url.set("https://github.com/ptitjes/syrup/")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("ptitjes")
                name.set("Didier Villevalois")
                url.set("https://github.com/ptitjes/")
            }
        }
        scm {
            url.set("https://github.com/ptitjes/syrup/")
            connection.set("scm:git:git://github.com/ptitjes/syrup.git")
            developerConnection.set("scm:git:ssh://git@github.com/ptitjes/syrup.git")
        }
    }
}
