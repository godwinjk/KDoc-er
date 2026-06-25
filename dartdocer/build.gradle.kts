import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

val pluginName = "DartDoc-er - Dart Doc Generator"
val pluginVersion = "2026.1.1"
val pluginSinceBuild = "251"
val platformType = "IC"
val platformVersion = "2025.2"
val dartPluginVersion = "251.27812.12"

plugins {
    id("org.jetbrains.intellij.platform")
}

version = pluginVersion

repositories {
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation(project(":engine"))
    compileOnly("org.yaml:snakeyaml:2.2")

    intellijPlatform {
        create(platformType, platformVersion)
        plugin("Dart", dartPluginVersion)
        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    buildSearchableOptions = false
    pluginConfiguration {
        name = pluginName
        version = pluginVersion

        description = providers.provider {
            val readme = file("README.md")
            if (readme.exists()) {
                val text = readme.readText()
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"
                if (text.contains(start) && text.contains(end)) {
                    markdownToHTML(text.substringAfter(start).substringBefore(end).trim())
                } else {
                    "Automatically generates DartDoc comments for Dart files in IntelliJ-based IDEs."
                }
            } else {
                "Automatically generates DartDoc comments for Dart files in IntelliJ-based IDEs."
            }
        }

        changeNotes = providers.provider {
            val cl = rootProject.extensions.getByType(org.jetbrains.changelog.ChangelogPluginExtension::class.java)
            with(cl) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

        ideaVersion {
            sinceBuild = pluginSinceBuild
            untilBuild.unset()
        }
    }
}
