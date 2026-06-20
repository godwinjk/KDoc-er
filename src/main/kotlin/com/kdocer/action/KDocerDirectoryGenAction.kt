package com.kdocer.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.kdocer.style.StyleLoader
import com.kdocer.util.Constants
import com.kdocer.util.NotificationHelper
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFile

/**
 * Generates KDocs for every qualifying declaration in every Kotlin file under the
 * selected folder(s)/module — the Project-view batch counterpart to
 * [KDocerAllGenAction]. Runs in the background; each file's edits are computed in a read
 * action (type inference included) and applied in a write command on the EDT.
 *
 * Created by Godwin on 7/23/2020 9:16 PM.
 *
 * @author : Godwin Joseph Kurinjikattu
 * @since : 2020
 */
class KDocerDirectoryGenAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(action: AnActionEvent) {
        val files = action.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        action.presentation.isEnabledAndVisible =
            files != null && files.any { it.isDirectory || it.extension == "kt" }
    }

    override fun actionPerformed(action: AnActionEvent) {
        val project = action.project ?: return
        val roots = action.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.toList() ?: return
        val ktFiles = collectKtFiles(roots)
        if (ktFiles.isEmpty()) return

        val policy = StyleLoader.resolve(project).existingKDocPolicy

        object : Task.Backgroundable(project, "Generating KDocs", true) {
            override fun run(indicator: ProgressIndicator) {
                ktFiles.forEachIndexed { index, file ->
                    indicator.checkCanceled()
                    indicator.fraction = (index + 1).toDouble() / ktFiles.size
                    indicator.text = file.name

                    // Read action (off-EDT): resolve PSI + generate/merge text (type inference here).
                    val edits = ReadAction.compute<List<Pair<KtDeclaration, String>>, RuntimeException> {
                        val ktFile = PsiManager.getInstance(project).findFile(file) as? KtFile
                            ?: return@compute emptyList()
                        KDocGenerationSupport.collectTargets(ktFile)
                            .mapNotNull { d -> KDocGenerationSupport.computeText(project, d, policy)?.let { d to it } }
                    }
                    if (edits.isEmpty()) return@forEachIndexed

                    // Write command on the EDT: pure PSI mutation, one undo step per file.
                    ApplicationManager.getApplication().invokeAndWait {
                        WriteCommandAction.runWriteCommandAction(project, "Generate KDocs", null, Runnable {
                            edits.forEach { (declaration, text) -> KDocGenerationSupport.insert(project, declaration, text) }
                        })
                    }
                }
            }

            override fun onSuccess() {
                NotificationHelper.showNotification(Constants.MESSAGE)
            }
        }.queue()
    }

    private fun collectKtFiles(roots: List<VirtualFile>): List<VirtualFile> {
        val result = ArrayList<VirtualFile>()
        val seen = HashSet<VirtualFile>()
        fun visit(file: VirtualFile) {
            if (!seen.add(file)) return
            if (file.isDirectory) file.children.forEach { visit(it) }
            else if (file.extension == "kt") result.add(file)
        }
        roots.forEach { visit(it) }
        return result
    }
}
