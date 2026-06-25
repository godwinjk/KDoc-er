package com.dartdocer.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.psi.PsiDocumentManager
import com.dartdocer.style.DartStyleLoader
import com.dartdocer.util.Constants
import com.dartdocer.util.NotificationHelper
import com.jetbrains.lang.dart.psi.DartFile

/**
 * Generates (or merges into) the DartDoc for every qualifying declaration in the current
 * Dart file -- top-level declarations and nested members alike -- honouring the
 * visibility/element-type settings via [com.dartdocer.util.DartValidator].
 */
class DartDocAllGenAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(action: AnActionEvent) {
        super.update(action)
        val enabled = action.getData(PlatformDataKeys.PSI_FILE) is DartFile
        action.presentation.isVisible = enabled
        action.presentation.isEnabled = enabled
    }

    override fun actionPerformed(action: AnActionEvent) {
        val psiFile = action.getData(PlatformDataKeys.PSI_FILE) as? DartFile ?: return
        val project = psiFile.project
        PsiDocumentManager.getInstance(project).commitAllDocuments()

        val policy = DartStyleLoader.resolve(project).existingDocPolicy
        DartDocGenerationSupport.documentFile(project, psiFile, policy)

        NotificationHelper.showNotification(Constants.MESSAGE)
    }
}
