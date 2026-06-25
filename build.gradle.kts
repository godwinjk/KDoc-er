import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML

plugins {
    java
    id("org.jetbrains.kotlin.jvm") version "2.4.0" apply false
    id("org.jetbrains.intellij.platform") version "2.16.0" apply false
    id("org.jetbrains.changelog") version "2.5.0"
}

changelog {
    version = "2026.1.1"
    groups = listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security")
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = providers.gradleProperty("pluginGroup").get()

    afterEvaluate {
        val javaVersion = if (plugins.hasPlugin("org.jetbrains.intellij.platform")) JavaVersion.VERSION_21 else JavaVersion.VERSION_17
        val jvmTarget = if (plugins.hasPlugin("org.jetbrains.intellij.platform"))
            org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21 else org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17

        java {
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
        }

        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            compilerOptions {
                this.jvmTarget.set(jvmTarget)
            }
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        "testImplementation"("org.junit.jupiter:junit-jupiter:5.10.2")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
        "testImplementation"("junit:junit:4.13.2")
        "testRuntimeOnly"("org.junit.vintage:junit-vintage-engine:5.10.2")
    }
}

// Convenience tasks to build/run/verify all three plugins from the root.
val pluginModules = listOf("kdocer", "dartdocer", "rustdocer")

tasks.register("buildAllPlugins") {
    group = "build"
    description = "Builds distributable ZIPs for all three plugins."
    dependsOn(pluginModules.map { ":$it:buildPlugin" })
}

tasks.register("verifyAllPlugins") {
    group = "verification"
    description = "Verifies plugin structure and compatibility for all three plugins."
    dependsOn(pluginModules.map { ":$it:verifyPlugin" })
}

tasks.register("testAll") {
    group = "verification"
    description = "Runs tests for all modules (engine + plugins)."
    dependsOn(":engine:test")
    dependsOn(pluginModules.map { ":$it:test" })
}

tasks.register("runKdocer") {
    group = "intellij platform"
    description = "Runs a sandboxed IDE with the KDoc-er (Kotlin) plugin loaded."
    dependsOn(":kdocer:runIde")
}

tasks.register("runDartdocer") {
    group = "intellij platform"
    description = "Runs a sandboxed IDE with the DartDoc-er (Dart) plugin loaded."
    dependsOn(":dartdocer:runIde")
}

tasks.register("runRustdocer") {
    group = "intellij platform"
    description = "Runs a sandboxed IDE with the RustDoc-er (Rust) plugin loaded."
    dependsOn(":rustdocer:runIde")
}
