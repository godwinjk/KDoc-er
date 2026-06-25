package com.rustdocer.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.psi.PsiDocumentManager
import com.rustdocer.style.RustStyleLoader
import com.rustdocer.util.Constants
import com.rustdocer.util.NotificationHelper
import org.rust.lang.core.psi.RsFile

/**
 * Generates (or merges into) the RustDoc for every qualifying declaration in the current
 * Rust file -- top-level declarations and nested members alike -- honouring the
 * visibility/element-type settings via [com.rustdocer.util.RustValidator].
 */
class RustDocAllGenAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(action: AnActionEvent) {
        super.update(action)
        val enabled = action.getData(PlatformDataKeys.PSI_FILE) is RsFile
        action.presentation.isVisible = enabled
        action.presentation.isEnabled = enabled
    }

    override fun actionPerformed(action: AnActionEvent) {
        val psiFile = action.getData(PlatformDataKeys.PSI_FILE) as? RsFile ?: return
        val project = psiFile.project
        PsiDocumentManager.getInstance(project).commitAllDocuments()

        val policy = RustStyleLoader.resolve(project).existingDocPolicy
        RustDocGenerationSupport.documentFile(project, psiFile, policy)

        NotificationHelper.showNotification(Constants.MESSAGE)
    }
}
