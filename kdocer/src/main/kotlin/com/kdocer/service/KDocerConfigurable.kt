package com.kdocer.service

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.ui.dsl.builder.COLUMNS_LARGE
import com.intellij.ui.dsl.builder.bind
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import com.docer.engine.merge.ExistingDocPolicy
import com.docer.engine.template.DocTemplate
import com.kdocer.util.Constants
import com.kdocer.util.NotificationHelper

class KDocerConfigurable : BoundConfigurable(Constants.SETTINGS_DISP_NAME) {

    private val settings = KDocerSettings.getInstance()
    private val default = DocTemplate.KDOC_DEFAULT

    override fun createPanel(): DialogPanel = panel {
        group("Generate KDoc For") {
            row {
                checkBox("Classes & objects").bindSelected(settings::isAllowedClass)
                checkBox("Functions").bindSelected(settings::isAllowedFun)
                checkBox("Properties").bindSelected(settings::isAllowedField)
            }
        }

        group("Visibility") {
            row {
                checkBox("Public").bindSelected(settings::isAllowedPublic)
                checkBox("Protected").bindSelected(settings::isAllowedProtected)
                checkBox("Internal").bindSelected(settings::isAllowedInternal)
                checkBox("Private").bindSelected(settings::isAllowedPrivate)
            }
            row {
                checkBox("Also document overridden members").bindSelected(settings::isAllowedOverride)
            }
        }

        group("Description") {
            row {
                checkBox("Prepend the element name as a description sentence")
                    .bindSelected(settings::isAppendName)
            }
            row {
                checkBox("Split camelCase / snake_case names into readable phrases")
                    .bindSelected(settings::isSplittedClassNames)
            }
            row {
                checkBox("Include the @constructor line for classes")
                    .bindSelected(settings::isConstructorLine)
            }
        }

        group("Framework Awareness") {
            row {
                checkBox("Detect frameworks and add notes (Compose, ViewModel, LiveData, data/sealed/enum/object/util)")
                    .bindSelected(settings::isFrameworkAware)
            }
            row {
                comment("Note wording is overridable per project via <code>aspects.notes.&lt;id&gt;</code> in <code>.kdocer.yaml</code>.")
            }
        }

        group("@throws Detection") {
            row {
                checkBox("Scan function bodies for <code>throw</code> expressions and emit <code>@throws</code> tags")
                    .bindSelected(settings::isThrowsDetection)
            }
        }

        group("@since Tag") {
            row {
                checkBox("Stamp generated KDocs with a <code>@since</code> version tag")
                    .bindSelected(settings::isSinceTag)
            }
            row("Version:") {
                textField().bindText(settings::sinceVersion).columns(COLUMNS_LARGE)
                    .comment("e.g. <code>1.0.0</code> or <code>2024.1</code>. Also configurable via <code>sinceVersion</code> in <code>.kdocer.yaml</code>.")
            }
        }

        group("@see Cross-References") {
            row {
                checkBox("Add <code>@see</code> links (overrides → super method, sealed subtypes → parent)")
                    .bindSelected(settings::isSeeReferences)
            }
        }

        group("Usage Example") {
            row {
                checkBox("Append a usage example to function KDocs (e.g. <code>val user = getUser()</code>)")
                    .bindSelected(settings::isUsageExample)
            }
            row {
                comment("A best-effort scaffold derived from names and types — review before relying on it.")
            }
        }

        group("Existing KDoc") {
            buttonsGroup {
                row {
                    radioButton("Merge — keep my text, add missing tags", ExistingDocPolicy.MERGE)
                }
                row {
                    radioButton("Keep — leave it untouched", ExistingDocPolicy.KEEP)
                }
                row {
                    radioButton("Replace — regenerate the whole comment", ExistingDocPolicy.REPLACE)
                }
            }.bind(settings::existingDocPolicy)
        }

        group("Description & Tag Templates") {
            row("Function description:") {
                textField().bindText(settings::templateFunctionDescription).columns(COLUMNS_LARGE)
                    .comment("Default: <code>${default.functionDescription}</code> · tokens: {description}")
            }
            row("@param line:") {
                textField().bindText(settings::templateParam).columns(COLUMNS_LARGE)
                    .comment("Default: <code>${default.paramLine}</code> · tokens: {name}, {noun}")
            }
            row("@return line:") {
                textField().bindText(settings::templateReturn).columns(COLUMNS_LARGE)
                    .comment("Default: <code>${default.returnLine}</code> · tokens: {description}")
            }
            row("Class description:") {
                textField().bindText(settings::templateClassDescription).columns(COLUMNS_LARGE)
                    .comment("Default: <code>${default.classDescription}</code> · tokens: {description}")
            }
            row("Property description:") {
                textField().bindText(settings::templatePropertyDescription).columns(COLUMNS_LARGE)
                    .comment("Default: <code>${default.propertyDescription}</code> · tokens: {description}")
            }
            row("@constructor line:") {
                textField().bindText(settings::templateConstructor).columns(COLUMNS_LARGE)
                    .comment("Default: <code>${default.constructorLine}</code> · tokens: {description}")
            }
            row {
                comment("Leave a field blank to use its built-in default. A project <code>.kdocer.yaml</code> overrides everything here.")
            }
        }

        group("Project Style Sheet") {
            row {
                button("Create .kdocer.yaml in Project Root") { createKDocerYaml() }
            }
            row {
                comment("Creates a <code>.kdocer.yaml</code> at the project root with all options commented out. " +
                    "Uncomment to override these settings per project.")
            }
        }

        group("Notifications") {
            row {
                checkBox("Disable the occasional KDoc-er notification")
                    .bindSelected(settings::isDisabledNotification)
            }
        }

        group("Support KDoc-er") {
            row {
                browserLink("Rate / star the plugin", Constants.MARKETPLACE_URL)
                browserLink("Donate", Constants.DONATE_URL)
                browserLink("Report an issue", Constants.ISSUES_URL)
            }
        }
    }

    private fun createKDocerYaml() {
        val project = ProjectManager.getInstance().openProjects.firstOrNull() ?: return
        val root = project.guessProjectDir() ?: return
        val fileName = ".kdocer.yaml"

        root.findChild(fileName)?.let { existing ->
            FileEditorManager.getInstance(project).openFile(existing, true)
            NotificationHelper.showNotification("KDoc-er", "$fileName already exists — opened it.")
            return
        }

        val content = javaClass.getResource("/kdocer.default.yaml")?.readText() ?: return
        WriteCommandAction.runWriteCommandAction(project) {
            val file = root.createChildData(this, fileName)
            VfsUtil.saveText(file, content)
            FileEditorManager.getInstance(project).openFile(file, true)
        }
        NotificationHelper.showNotification("KDoc-er", "Created $fileName at the project root.")
    }
}
