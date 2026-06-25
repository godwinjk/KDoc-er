package com.dartdocer.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.lang.dart.psi.DartClassDefinition
import com.jetbrains.lang.dart.psi.DartComponent
import com.jetbrains.lang.dart.psi.DartFile
import com.jetbrains.lang.dart.psi.DartFunctionDeclarationWithBodyOrNative
import com.jetbrains.lang.dart.psi.DartGetterDeclaration
import com.jetbrains.lang.dart.psi.DartMethodDeclaration
import com.jetbrains.lang.dart.psi.DartVarDeclarationList

/**
 * Removes the DartDoc of the declaration at the caret.
 */
class DartDocRemoveAction : AnAction() {

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

        // Find the enclosing declaration
        val target = PsiTreeUtil.getParentOfType(
            elementAtCaret,
            DartFunctionDeclarationWithBodyOrNative::class.java,
            DartMethodDeclaration::class.java,
            DartClassDefinition::class.java,
            DartGetterDeclaration::class.java,
            DartVarDeclarationList::class.java,
        ) ?: return

        WriteCommandAction.runWriteCommandAction(project) {
            DartDocGenerationSupport.removeDocComment(target)
        }
    }
}
