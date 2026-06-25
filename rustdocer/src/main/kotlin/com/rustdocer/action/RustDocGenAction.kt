package com.rustdocer.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import com.rustdocer.style.RustStyleLoader
import com.rustdocer.util.Constants
import com.rustdocer.util.NotificationHelper
import org.rust.lang.core.psi.RsEnumItem
import org.rust.lang.core.psi.RsFile
import org.rust.lang.core.psi.RsFunction
import org.rust.lang.core.psi.RsImplItem
import org.rust.lang.core.psi.RsStructItem
import org.rust.lang.core.psi.RsTraitItem

/**
 * Generates (or merges into) the RustDoc for the function, struct, enum, trait or impl at the caret.
 */
class RustDocGenAction : AnAction() {

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
        val target = PsiTreeUtil.getParentOfType(
            elementAtCaret,
            RsFunction::class.java,
            RsStructItem::class.java,
            RsEnumItem::class.java,
            RsTraitItem::class.java,
            RsImplItem::class.java,
        ) ?: return

        val policy = RustStyleLoader.resolve(project).existingDocPolicy
        RustDocGenerationSupport.applyTo(project, target, policy)

        NotificationHelper.showNotification(Constants.MESSAGE)
    }
}
