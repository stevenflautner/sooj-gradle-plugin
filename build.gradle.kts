import java.util.Date

plugins {
    kotlin("jvm") version "1.4.30-RC"
    id("java-gradle-plugin")
    id("maven")
    id("org.gradle.kotlin.kotlin-dsl") version "1.4.2"
    id("maven-publish")
    id("com.jfrog.bintray") version "1.8.4"
}

group = "io.sooj"
version = "0.1-SNAPSHOT"

repositories {
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
    maven {
        url = uri("https://dl.bintray.com/sooj/sooj")
    }
}

kotlin {
    target.compilations.all {
        kotlinOptions.jvmTarget = "11"
    }
}

dependencies {
    testImplementation(kotlin("test-junit"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.30")
    implementation("org.jetbrains.kotlin:kotlin-serialization:1.4.30")
}

gradlePlugin {
    plugins {
        create("io.sooj.gradle.plugin") {
            id = "io.sooj.gradle.plugin"
            implementationClass = "io.sooj.gradle.soojPlugin"
        }
    }
}

// Publications

val artifactName = project.name
val artifactGroup = project.group.toString()
val artifactVersion = project.version.toString()

val bintrayRepo = "sooj"
val owner = "stevenflautner"
val packageName = "sooj-gradle-plugin"
val versionDescription = "Pre-release 0.0.1"
val license = "MIT"
val projVcsUrl = "https://github.com/stevenflautner/sooj-gradle-plugin.git"

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

publishing {
    publications {
        create<MavenPublication>("sooj-gradle-plugin") {
            groupId = artifactGroup
            artifactId = artifactName
            version = artifactVersion
            from(components["java"])

            artifact(sourcesJar)
        }

        bintray {
            user = "stevenflautner"
            key = project.findProperty("bintrayKey").toString()
            publish = true

            setPublications("sooj-gradle-plugin")

            pkg.apply {
                repo = bintrayRepo
                name = packageName
                userOrg = "sooj"
                setLicenses("MIT")
                vcsUrl = projVcsUrl
                version.apply {
                    name = artifactVersion
                    desc = versionDescription
                    released = Date().toString()
                    vcsTag = artifactVersion
                }
            }
        }
    }
}