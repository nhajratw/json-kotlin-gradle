/*
 * @(#) build.gradle.kts
 */

import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

group = "net.pwall.json"
version = "0.121"
description = "Gradle Code Generation Plugin for JSON Schema"

val displayName = "JSON Schema Code Generation Plugin"
val projectURL = "https://github.com/pwall567/${project.name}"

plugins {
    kotlin("jvm") version "2.0.21"
    id("org.jetbrains.dokka") version "2.0.0"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("com.github.ben-manes.versions") version "0.53.0"
    `kotlin-dsl`
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        languageVersion.set(KotlinVersion.KOTLIN_2_0)
        apiVersion.set(KotlinVersion.KOTLIN_2_0)
    }
}

tasks {
    val sourcesJar by registering(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }
    val javadocJar by registering(Jar::class) {
        archiveClassifier.set("javadoc")
        from(dokkaJavadoc)
        dependsOn(dokkaJavadoc)
    }
    artifacts {
        add("archives", sourcesJar)
        add("archives", javadocJar)
    }
    dokkaJavadoc {
        outputDirectory.set(layout.buildDirectory.dir("javadoc"))
        dokkaSourceSets.configureEach {
            reportUndocumented.set(false)
            jdkVersion.set(21)
            perPackageOption {
                matchingRegex.set(".*\\.internal($|\\.).*")
                suppress.set(true)
            }
        }
    }
    javadoc {
        enabled = false
    }
    named<Jar>("javadocJar").configure {
        from(dokkaJavadoc)
    }
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation("net.pwall.json:json-kotlin-schema:0.57")
    implementation("net.pwall.json:json-kotlin-schema-codegen:0.123")
    implementation("io.kjson:kjson-pointer:8.12")
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation("io.kstuff:should-test:4.5")
}

publishing {
    publications {
        afterEvaluate {
            named<MavenPublication>("pluginMaven") {
                artifact(file("build/libs/${project.name}-${project.version}-sources.jar")) {
                    classifier = "sources"
                }
                artifact(file("build/libs/${project.name}-${project.version}-javadoc.jar")) {
                    classifier = "javadoc"
                }
                pom {
                    name.set(displayName)
                    url.set(projectURL)
                    description.set(project.description)
                    packaging = "jar"
                    scm {
                        connection.set("scm:git:git://github.com/pwall567/${project.name}.git")
                        url.set(projectURL)
                    }
                    licenses {
                        license {
                            name.set("The MIT License (MIT)")
                            url.set("http://opensource.org/licenses/MIT")
                        }
                    }
                    developers {
                        developer {
                            id.set("pwall@pwall.net")
                            name.set("Peter Wall")
                            email.set("pwall@pwall.net")
                            url.set("https://pwall.net")
                            roles.set(setOf("architect", "developer"))
                            timezone.set("Australia/Sydney")
                        }
                    }
                }
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            username.set(project.findProperty("ossrhUsername").toString())
            password.set(project.findProperty("ossrhPassword").toString())
        }
    }
}

signing {
    afterEvaluate {
        useGpgCmd()
        sign(*publishing.publications.toTypedArray())
    }
}
