package com.dartdocer.service

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
import com.dartdocer.util.Constants
import com.dartdocer.util.NotificationHelper

class DartDocerConfigurable : BoundConfigurable(Constants.SETTINGS_DISP_NAME) {

    private val settings = DartDocerSettings.getInstance()
    private val default = DocTemplate.DARTDOC_DEFAULT

    override fun createPanel(): DialogPanel = panel {
        group("Generate DartDoc For") {
            row {
                checkBox("Classes & enums").bindSelected(settings::isAllowedClass)
                checkBox("Functions & methods").bindSelected(settings::isAllowedFunction)
                checkBox("Fields & getters").bindSelected(settings::isAllowedField)
            }
            row {
                checkBox("Extensions").bindSelected(settings::isAllowedExtension)
                checkBox("Mixins").bindSelected(settings::isAllowedMixin)
            }
        }

        group("Visibility") {
            row {
                checkBox("Public (no underscore prefix)").bindSelected(settings::isAllowedPublic)
                checkBox("Private (underscore-prefixed _member)").bindSelected(settings::isAllowedPrivate)
            }
            row {
                checkBox("Also document @override members").bindSelected(settings::isAllowedOverride)
            }
        }

        group("Description") {
            row {
                checkBox("Prepend the element name as a description sentence")
                    .bindSelected(settings::isAppendName)
            }
            row {
                checkBox("Split camelCase / snake_case names into readable phrases")
                    .bindSelected(settings::isSplitNames)
            }
            row {
                checkBox("Include constructor description for classes")
                    .bindSelected(settings::isConstructorLine)
            }
        }

        group("Flutter Awareness") {
            row {
                checkBox("Detect Flutter patterns and add notes (StatelessWidget, StatefulWidget, ChangeNotifier, @freezed)")
                    .bindSelected(settings::isFlutterAware)
            }
            row {
                comment("Note wording is overridable per project via <code>aspects.notes.&lt;id&gt;</code> in <code>.dartdocer.yaml</code>.")
            }
        }

        group("Throws Detection") {
            row {
                checkBox("Scan function bodies for <code>throw</code> expressions and document them")
                    .bindSelected(settings::isThrowsDetection)
            }
        }

        group("@since Tag") {
            row {
                checkBox("Stamp generated DartDocs with a version tag")
                    .bindSelected(settings::isSinceTag)
            }
            row("Version:") {
                textField().bindText(settings::sinceVersion).columns(COLUMNS_LARGE)
                    .comment("e.g. <code>1.0.0</code> or <code>2024.1</code>. Also configurable via <code>sinceVersion</code> in <code>.dartdocer.yaml</code>.")
            }
        }

        group("See References") {
            row {
                checkBox("Add see-also references (superclass, implemented interfaces, overrides)")
                    .bindSelected(settings::isSeeReferences)
            }
        }

        group("Usage Example") {
            row {
                checkBox("Append a usage example to function DartDocs (e.g. <code>final user = getUser();</code>)")
                    .bindSelected(settings::isUsageExample)
            }
            row {
                comment("A best-effort scaffold derived from names and types — review before relying on it.")
            }
        }

        group("Existing DartDoc") {
            buttonsGroup {
                row {
                    radioButton("Merge — keep my text, add missing content", ExistingDocPolicy.MERGE)
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
            row("Return description:") {
                textField().bindText(settings::templateReturn).columns(COLUMNS_LARGE)
                    .comment("Default: <code>${default.returnLine}</code> · tokens: {description}")
            }
            row("Class description:") {
                textField().bindText(settings::templateClassDescription).columns(COLUMNS_LARGE)
                    .comment("Default: <code>${default.classDescription}</code> · tokens: {description}")
            }
            row("Field description:") {
                textField().bindText(settings::templateFieldDescription).columns(COLUMNS_LARGE)
                    .comment("Default: <code>${default.propertyDescription}</code> · tokens: {description}")
            }
            row("Constructor line:") {
                textField().bindText(settings::templateConstructor).columns(COLUMNS_LARGE)
                    .comment("Default: <code>${default.constructorLine}</code> · tokens: {name}, {description}")
            }
            row {
                comment("Leave a field blank to use its built-in default. A project <code>.dartdocer.yaml</code> overrides everything here.")
            }
        }

        group("Project Style Sheet") {
            row {
                button("Create .dartdocer.yaml in Project Root") { createDartDocerYaml() }
            }
            row {
                comment("Creates a <code>.dartdocer.yaml</code> at the project root with all options commented out. " +
                    "Uncomment to override these settings per project.")
            }
        }

        group("Notifications") {
            row {
                checkBox("Disable the occasional DartDoc-er notification")
                    .bindSelected(settings::isDisabledNotification)
            }
        }

        group("Support DartDoc-er") {
            row {
                browserLink("Rate / star the plugin", Constants.MARKETPLACE_URL)
                browserLink("Donate", Constants.DONATE_URL)
                browserLink("Report an issue", Constants.ISSUES_URL)
            }
        }
    }

    private fun createDartDocerYaml() {
        val project = ProjectManager.getInstance().openProjects.firstOrNull() ?: return
        val root = project.guessProjectDir() ?: return
        val fileName = ".dartdocer.yaml"

        root.findChild(fileName)?.let { existing ->
            FileEditorManager.getInstance(project).openFile(existing, true)
            NotificationHelper.showNotification("DartDoc-er", "$fileName already exists — opened it.")
            return
        }

        val content = javaClass.getResource("/dartdocer.default.yaml")?.readText() ?: return
        WriteCommandAction.runWriteCommandAction(project) {
            val file = root.createChildData(this, fileName)
            VfsUtil.saveText(file, content)
            FileEditorManager.getInstance(project).openFile(file, true)
        }
        NotificationHelper.showNotification("DartDoc-er", "Created $fileName at the project root.")
    }
}
