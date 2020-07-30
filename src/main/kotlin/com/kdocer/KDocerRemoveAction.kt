package com.kdocer

import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty


/**
 * Created by Godwin on 7/23/2020 9:16 PM.
 *
 * @author : Godwin Joseph Kurinjikattu
 * @since : 2020
 */
class KDocerRemoveAction : AnAction() {
    override fun actionPerformed(action: AnActionEvent) {
        val psiFile = action.getData(PlatformDataKeys.PSI_FILE) ?: return
        if (psiFile !is KtFile || !CodeInsightSettings.getInstance().SMART_INDENT_ON_ENTER) {
            println("This is not Kotlin file. ")
            return
        }
        if (!isAllowedElementType(psiFile)) return

        val project = psiFile.project
        val documentManager = PsiDocumentManager.getInstance(project)
        documentManager.commitAllDocuments()

        val editor = action.getData(PlatformDataKeys.EDITOR) ?: return
        val caretModel = editor.caretModel
        val elementAtCaret = psiFile.findElementAt(caretModel.offset) ?: return

        preProcessElement(elementAtCaret)
    }

    private fun preProcessElement(element: PsiElement): Boolean {
        return when (element) {
            is KDoc -> {
                processElement(element)
                true
            }
            is PsiWhiteSpace -> {
                val kDoc = PsiTreeUtil.getParentOfType(element, KDoc::class.java)
                return if (kDoc != null) {
                    processElement(kDoc)
                    true
                } else {
                    var status = preProcessElement(element.nextSibling)
                    if (!status) status = preProcessElement(element.prevSibling)
                    status
                }
            }
            else -> false
        }
    }

    private fun processElement(kdoc: KDoc) {
        ApplicationManager.getApplication().invokeLater {
            WriteCommandAction.runWriteCommandAction(kdoc.project) {
                kdoc.delete()
            }
        }
    }

    private fun isAllowedElementType(element: PsiElement): Boolean {
        var result = false
        if (element is PsiClass ||
            element is PsiField ||
            element is PsiMethod ||
            element is KtFile ||
            element is KtNamedFunction ||
            element is KtClass ||
            element is KtProperty
        ) {
            result = true
        }
        return result
    }
}