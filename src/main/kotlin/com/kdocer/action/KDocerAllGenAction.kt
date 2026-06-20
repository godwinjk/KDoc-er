package com.kdocer.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.psi.PsiDocumentManager
import com.kdocer.style.StyleLoader
import com.kdocer.util.Constants
import com.kdocer.util.NotificationHelper
import org.jetbrains.kotlin.psi.KtFile

/**
 * Generates (or merges into) the KDoc for every qualifying declaration in the current
 * Kotlin file — top-level declarations and nested members alike — honouring the
 * visibility/element-type settings via [com.kdocer.util.Validator].
 *
 * Created by Godwin on 7/23/2020 9:16 PM.
 *
 * @author : Godwin Joseph Kurinjikattu
 * @since : 2020
 */
class KDocerAllGenAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(action: AnActionEvent) {
        super.update(action)
        val enabled = action.getData(PlatformDataKeys.PSI_FILE) is KtFile
        action.presentation.isVisible = enabled
        action.presentation.isEnabled = enabled
    }

    override fun actionPerformed(action: AnActionEvent) {
        val psiFile = action.getData(PlatformDataKeys.PSI_FILE) as? KtFile ?: return
        val project = psiFile.project
        PsiDocumentManager.getInstance(project).commitAllDocuments()

        val policy = StyleLoader.resolve(project).existingKDocPolicy
        KDocGenerationSupport.documentFile(project, psiFile, policy)

        NotificationHelper.showNotification(Constants.MESSAGE)
    }
}
