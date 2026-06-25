package com.rustdocer.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import org.rust.lang.core.psi.RsEnumItem
import org.rust.lang.core.psi.RsFile
import org.rust.lang.core.psi.RsFunction
import org.rust.lang.core.psi.RsImplItem
import org.rust.lang.core.psi.RsStructItem
import org.rust.lang.core.psi.RsTraitItem

/**
 * Removes the RustDoc of the declaration at the caret.
 */
class RustDocRemoveAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(action: AnActionEvent) {
        super.update(action)
        val enabled = action.getData(PlatformDataKeys.PSI_FILE) is RsFile
        action.presentation.isVisible = enabled
        action.presentation.isEnabled = enabled
    }

    override fun actionPerformed(action: AnActionEvent) {
        val psiFile = action.getData(PlatformDataKeys.PSI_FILE) as? RsFile ?: return
        val editor = action.getData(PlatformDataKeys.EDITOR) ?: return
        val project = psiFile.project
        PsiDocumentManager.getInstance(project).commitAllDocuments()

        val elementAtCaret = psiFile.findElementAt(editor.caretModel.offset) ?: return

        // Find the enclosing declaration
        val target = PsiTreeUtil.getParentOfType(
            elementAtCaret,
            RsFunction::class.java,
            RsStructItem::class.java,
            RsEnumItem::class.java,
            RsTraitItem::class.java,
            RsImplItem::class.java,
        ) ?: return

        WriteCommandAction.runWriteCommandAction(project) {
            RustDocGenerationSupport.removeDocComment(target)
        }
    }
}
