package com.dartdocer.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import com.dartdocer.style.DartStyleLoader
import com.dartdocer.util.Constants
import com.dartdocer.util.NotificationHelper
import com.jetbrains.lang.dart.psi.DartClassDefinition
import com.jetbrains.lang.dart.psi.DartComponent
import com.jetbrains.lang.dart.psi.DartFile
import com.jetbrains.lang.dart.psi.DartFunctionDeclarationWithBodyOrNative
import com.jetbrains.lang.dart.psi.DartGetterDeclaration
import com.jetbrains.lang.dart.psi.DartMethodDeclaration
import com.jetbrains.lang.dart.psi.DartVarDeclarationList

/**
 * Generates (or merges into) the DartDoc for the class, function, method or field at the caret.
 */
class DartDocGenAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(action: AnActionEvent) {
        super.update(action)
        val enabled = action.getData(PlatformDataKeys.PSI_FILE) is DartFile
        action.presentation.isVisible = enabled
        action.presentation.isEnabled = enabled
    }

    override fun actionPerformed(action: AnActionEvent) {
        val psiFile = action.getData(PlatformDataKeys.PSI_FILE) as? DartFile ?: return
        val editor = action.getData(PlatformDataKeys.EDITOR) ?: return
        val project = psiFile.project
        PsiDocumentManager.getInstance(project).commitAllDocuments()

        val elementAtCaret = psiFile.findElementAt(editor.caretModel.offset) ?: return
        val target = PsiTreeUtil.getParentOfType(
            elementAtCaret,
            DartFunctionDeclarationWithBodyOrNative::class.java,
            DartMethodDeclaration::class.java,
            DartClassDefinition::class.java,
            DartGetterDeclaration::class.java,
            DartVarDeclarationList::class.java,
        ) ?: return

        val policy = DartStyleLoader.resolve(project).existingDocPolicy
        DartDocGenerationSupport.applyTo(project, target, policy)

        NotificationHelper.showNotification(Constants.MESSAGE)
    }
}
