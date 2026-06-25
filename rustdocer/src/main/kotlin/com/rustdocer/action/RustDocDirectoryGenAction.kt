package com.rustdocer.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.rustdocer.style.RustStyleLoader
import com.rustdocer.util.Constants
import com.rustdocer.util.NotificationHelper
import org.rust.lang.core.psi.RsFile

/**
 * Generates RustDocs for every qualifying declaration in every Rust file under the
 * selected folder(s)/module. Runs in the background; each file's edits are computed in a
 * read action and applied in a write command on the EDT.
 */
class RustDocDirectoryGenAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(action: AnActionEvent) {
        val files = action.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        action.presentation.isEnabledAndVisible =
            files != null && files.any { it.isDirectory || it.extension == "rs" }
    }

    override fun actionPerformed(action: AnActionEvent) {
        val project = action.project ?: return
        val roots = action.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.toList() ?: return
        val rsFiles = collectRsFiles(roots)
        if (rsFiles.isEmpty()) return

        val policy = RustStyleLoader.resolve(project).existingDocPolicy

        object : Task.Backgroundable(project, "Generating RustDocs", true) {
            override fun run(indicator: ProgressIndicator) {
                rsFiles.forEachIndexed { index, file ->
                    indicator.checkCanceled()
                    indicator.fraction = (index + 1).toDouble() / rsFiles.size
                    indicator.text = file.name

                    // Read action (off-EDT): resolve PSI + generate/merge text.
                    val edits = runReadAction<List<Pair<PsiElement, String>>> {
                        val rsFile = PsiManager.getInstance(project).findFile(file) as? RsFile
                            ?: return@runReadAction emptyList()
                        RustDocGenerationSupport.collectTargets(rsFile)
                            .mapNotNull { d ->
                                RustDocGenerationSupport.computeText(project, d, policy)?.let { d to it }
                            }
                    }
                    if (edits.isEmpty()) return@forEachIndexed

                    // Write command on the EDT: pure PSI mutation, one undo step per file.
                    ApplicationManager.getApplication().invokeAndWait {
                        WriteCommandAction.runWriteCommandAction(project, "Generate RustDocs", null, Runnable {
                            edits.forEach { (element, text) ->
                                RustDocGenerationSupport.insert(project, element, text)
                            }
                        })
                    }
                }
            }

            override fun onSuccess() {
                NotificationHelper.showNotification(Constants.MESSAGE)
            }
        }.queue()
    }

    private fun collectRsFiles(roots: List<VirtualFile>): List<VirtualFile> {
        val result = ArrayList<VirtualFile>()
        val seen = HashSet<VirtualFile>()
        fun visit(file: VirtualFile) {
            if (!seen.add(file)) return
            if (file.isDirectory) file.children.forEach { visit(it) }
            else if (file.extension == "rs") result.add(file)
        }
        roots.forEach { visit(it) }
        return result
    }
}
