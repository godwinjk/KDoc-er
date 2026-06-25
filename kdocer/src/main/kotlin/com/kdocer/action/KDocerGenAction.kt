package com.kdocer.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import com.kdocer.style.StyleLoader
import com.kdocer.util.Constants
import com.kdocer.util.NotificationHelper
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty

/**
 * Generates (or merges into) the KDoc for the class, object, function or property at the
 * caret.
 *
 * Created by Godwin on 7/23/2020 9:16 PM.
 *
 * @author : Godwin Joseph Kurinjikattu
 * @since : 2020
 */
class KDocerGenAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(action: AnActionEvent) {
        super.update(action)
        val enabled = action.getData(PlatformDataKeys.PSI_FILE) is KtFile
        action.presentation.isVisible = enabled
        action.presentation.isEnabled = enabled
    }

    override fun actionPerformed(action: AnActionEvent) {
        val psiFile = action.getData(PlatformDataKeys.PSI_FILE) as? KtFile ?: return
        val editor = action.getData(PlatformDataKeys.EDITOR) ?: return
        val project = psiFile.project
        PsiDocumentManager.getInstance(project).commitAllDocuments()

        val elementAtCaret = psiFile.findElementAt(editor.caretModel.offset) ?: return
        val target = PsiTreeUtil.getParentOfType(
            elementAtCaret,
            KtNamedFunction::class.java,
            KtClassOrObject::class.java,
            KtProperty::class.java,
        ) as? KtDeclaration ?: return

        val policy = StyleLoader.resolve(project).existingDocPolicy
        KDocGenerationSupport.applyTo(project, target, policy)

        NotificationHelper.showNotification(Constants.MESSAGE)
    }
}
