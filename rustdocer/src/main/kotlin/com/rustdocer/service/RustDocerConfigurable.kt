package com.rustdocer.service

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
import com.rustdocer.util.Constants
import com.rustdocer.util.NotificationHelper

class RustDocerConfigurable : BoundConfigurable(Constants.SETTINGS_DISP_NAME) {

    private val settings = RustDocerSettings.getInstance()
    private val default = DocTemplate.RUSTDOC_DEFAULT

    override fun createPanel(): DialogPanel = panel {
        group("Generate RustDoc For") {
            row {
                checkBox("Functions").bindSelected(settings::isAllowedFunction)
                checkBox("Structs").bindSelected(settings::isAllowedStruct)
                checkBox("Enums").bindSelected(settings::isAllowedEnum)
            }
            row {
                checkBox("Traits").bindSelected(settings::isAllowedTrait)
                checkBox("Impl blocks").bindSelected(settings::isAllowedImpl)
                checkBox("Type aliases").bindSelected(settings::isAllowedTypeAlias)
            }
            row {
                checkBox("Constants").bindSelected(settings::isAllowedConst)
            }
        }

        group("Visibility") {
            row {
                checkBox("pub").bindSelected(settings::isAllowedPub)
                checkBox("pub(crate)").bindSelected(settings::isAllowedPubCrate)
                checkBox("pub(super)").bindSelected(settings::isAllowedPubSuper)
            }
            row {
                checkBox("Private (no visibility modifier)").bindSelected(settings::isAllowedPrivate)
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
        }

        group("Crate Awareness") {
            row {
                checkBox("Detect crate patterns and add notes (#[derive(...)], serde, #[async_trait], builder, thiserror)")
                    .bindSelected(settings::isFrameworkAware)
            }
            row {
                comment("Note wording is overridable per project via <code>aspects.notes.&lt;id&gt;</code> in <code>.rustdocer.yaml</code>.")
            }
        }

        group("Panic Detection") {
            row {
                checkBox("Scan function bodies for <code>panic!()</code>, <code>.unwrap()</code>, <code>.expect()</code>, <code>todo!()</code> and document them in a <b># Panics</b> section")
                    .bindSelected(settings::isPanicDetection)
            }
        }

        group("Safety Section") {
            row {
                checkBox("Generate a <b># Safety</b> section for <code>unsafe fn</code> and <code>unsafe impl</code>")
                    .bindSelected(settings::isUnsafeSafetySection)
            }
        }

        group("Errors Section") {
            row {
                checkBox("Generate a <b># Errors</b> section for functions returning <code>Result&lt;T, E&gt;</code>")
                    .bindSelected(settings::isErrorsSection)
            }
        }

        group("Examples Section") {
            row {
                checkBox("Generate a <b># Examples</b> section with a usage example (e.g. <code>let result = my_fn();</code>)")
                    .bindSelected(settings::isExamplesSection)
            }
            row {
                comment("A best-effort scaffold derived from names and types - review before relying on it.")
            }
        }

        group("Since Tag") {
            row {
                checkBox("Stamp generated RustDocs with a version tag")
                    .bindSelected(settings::isSinceTag)
            }
            row("Version:") {
                textField().bindText(settings::sinceVersion).columns(COLUMNS_LARGE)
                    .comment("e.g. <code>1.0.0</code> or <code>0.1.0</code>. Also configurable via <code>sinceVersion</code> in <code>.rustdocer.yaml</code>.")
            }
        }

        group("See References") {
            row {
                checkBox("Add see-also references (super traits, implemented traits, types)")
                    .bindSelected(settings::isSeeReferences)
            }
        }

        group("Usage Example") {
            row {
                checkBox("Append a usage example to function RustDocs (e.g. <code>let user = get_user();</code>)")
                    .bindSelected(settings::isUsageExample)
            }
            row {
                comment("A best-effort scaffold derived from names and types - review before relying on it.")
            }
        }

        group("Existing RustDoc") {
            buttonsGroup {
                row {
                    radioButton("Merge - keep my text, add missing content", ExistingDocPolicy.MERGE)
                }
                row {
                    radioButton("Keep - leave it untouched", ExistingDocPolicy.KEEP)
                }
                row {
                    radioButton("Replace - regenerate the whole comment", ExistingDocPolicy.REPLACE)
                }
            }.bind(settings::existingDocPolicy)
        }

        group("Description & Tag Templates") {
            row("Function description:") {
                textField().bindText(settings::templateFunctionDescription).columns(COLUMNS_LARGE)
                    .comment("Default: <code>${default.functionDescription}</code> - tokens: {description}")
            }
            row("Return description:") {
                textField().bindText(settings::templateReturn).columns(COLUMNS_LARGE)
                    .comment("Default: <code>${default.returnLine}</code> - tokens: {description}")
            }
            row("Struct description:") {
                textField().bindText(settings::templateStructDescription).columns(COLUMNS_LARGE)
                    .comment("Default: <code>${default.classDescription}</code> - tokens: {description}")
            }
            row("Field description:") {
                textField().bindText(settings::templateFieldDescription).columns(COLUMNS_LARGE)
                    .comment("Default: <code>${default.propertyDescription}</code> - tokens: {description}")
            }
            row("Param line:") {
                textField().bindText(settings::templateParam).columns(COLUMNS_LARGE)
                    .comment("Default: <code>${default.paramLine}</code> - tokens: {name}, {noun}")
            }
            row {
                comment("Leave a field blank to use its built-in default. A project <code>.rustdocer.yaml</code> overrides everything here.")
            }
        }

        group("Project Style Sheet") {
            row {
                button("Create .rustdocer.yaml in Project Root") { createRustDocerYaml() }
            }
            row {
                comment("Creates a <code>.rustdocer.yaml</code> at the project root with all options commented out. " +
                    "Uncomment to override these settings per project.")
            }
        }

        group("Notifications") {
            row {
                checkBox("Disable the occasional RustDoc-er notification")
                    .bindSelected(settings::isDisabledNotification)
            }
        }

        group("Support RustDoc-er") {
            row {
                browserLink("Rate / star the plugin", Constants.MARKETPLACE_URL)
                browserLink("Donate", Constants.DONATE_URL)
                browserLink("Report an issue", Constants.ISSUES_URL)
            }
        }
    }

    private fun createRustDocerYaml() {
        val project = ProjectManager.getInstance().openProjects.firstOrNull() ?: return
        val root = project.guessProjectDir() ?: return
        val fileName = ".rustdocer.yaml"

        root.findChild(fileName)?.let { existing ->
            FileEditorManager.getInstance(project).openFile(existing, true)
            NotificationHelper.showNotification("RustDoc-er", "$fileName already exists - opened it.")
            return
        }

        val content = javaClass.getResource("/rustdocer.default.yaml")?.readText() ?: return
        WriteCommandAction.runWriteCommandAction(project) {
            val file = root.createChildData(this, fileName)
            VfsUtil.saveText(file, content)
            FileEditorManager.getInstance(project).openFile(file, true)
        }
        NotificationHelper.showNotification("RustDoc-er", "Created $fileName at the project root.")
    }
}
