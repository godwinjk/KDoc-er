import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    java
    id("org.jetbrains.kotlin.jvm") version "2.4.0"
    id("org.jetbrains.intellij.platform") version "2.16.0"
    id("org.jetbrains.changelog") version "2.5.0"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    // SnakeYAML is bundled with the IDE; compileOnly so we don't ship a second copy.
    compileOnly("org.yaml:snakeyaml:2.2")

    // IntelliJ Platform Gradle Plugin 2.x: target IDE, bundled plugins and the test framework.
    // The IDE type/version come from gradle.properties.
    intellijPlatform {
        create(
            providers.gradleProperty("platformType").get(),
            providers.gradleProperty("platformVersion").get(),
        )
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.kotlin")
        testFramework(TestFrameworkType.Platform)
    }

    // Pure-logic tests use JUnit 5 (Jupiter); the JUnit 3/4-style IntelliJ fixture tests
    // run on the same JUnit Platform via the Vintage engine.
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.10.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

intellijPlatform {
    pluginConfiguration {
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")

        // Plugin description = the marked section of README.md, converted Markdown -> HTML.
        description = providers.provider {
            val readme = file("README.md").readText()
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"
            if (!readme.contains(start) || !readme.contains(end)) {
                throw GradleException("README.md is missing the plugin description markers")
            }
            markdownToHTML(readme.substringAfter(start).substringBefore(end).trim())
        }

        // Change notes = the matching CHANGELOG.md entry, converted to HTML.
        changeNotes = providers.provider {
            val pluginVersion = providers.gradleProperty("pluginVersion").get()
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            // Open-ended compatibility (no upper bound).
            untilBuild.unset()
        }
    }
}

changelog {
    version = providers.gradleProperty("pluginVersion")
    groups = listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security")
}

tasks.test {
    useJUnitPlatform()
}
