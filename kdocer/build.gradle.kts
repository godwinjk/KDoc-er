import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

val pluginName = "KDoc-er - Kotlin Doc Generator"
val pluginVersion = "2026.1.1"
val pluginSinceBuild = "251"
val platformType = "IC"
val platformVersion = "2025.2"

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
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.kotlin")
        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        name = pluginName
        version = pluginVersion

        description = providers.provider {
            val readme = file("README.md").readText()
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"
            if (!readme.contains(start) || !readme.contains(end)) {
                throw GradleException("kdocer/README.md is missing the plugin description markers")
            }
            markdownToHTML(readme.substringAfter(start).substringBefore(end).trim())
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
