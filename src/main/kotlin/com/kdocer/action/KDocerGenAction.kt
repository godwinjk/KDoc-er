package com.kdocer.action

import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.PsiTreeUtil
import com.kdocer.generator.ClassKDocGenerator
import com.kdocer.generator.KDocGenerator
import com.kdocer.generator.NamedFunctionKDocGenerator
import com.kdocer.generator.PropertyKDocGenerator
import com.kdocer.util.Constants
import com.kdocer.util.NotificationHelper
import org.jetbrains.kotlin.idea.kdoc.KDocElementFactory
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType


/**
 * Created by Godwin on 7/23/2020 9:16 PM.
 *
 * @author : Godwin Joseph Kurinjikattu
 * @since : 2020
 */
class KDocerGenAction : AnAction() {
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

        NotificationHelper.showNotification(Constants.MESSAGE)
    }

    private fun preProcessElement(element: PsiElement) {
        if (element is KtNamedFunction ||
            element is KtClass ||
            element is KtProperty
        ) {
            processElement(element)
        } else if (element is PsiWhiteSpace) {
            preProcessElement(element.nextSibling)
        } else {
            val clazz = PsiTreeUtil.getParentOfType(element, KtClass::class.java)
            if (clazz != null) processElement(clazz)
        }
    }

    private fun processElement(psiElement: PsiElement) {
        val project = psiElement.project
        val generator = getDocGenerator(project, psiElement) ?: return
        val comment = generator.generate()

        ApplicationManager.getApplication().invokeLater {
            WriteCommandAction.runWriteCommandAction(project) {
                val kDocElementFactory = KDocElementFactory(project)
                comment.let {
                    kDocElementFactory.createKDocFromText(it)
                        .let {
                            val kdoc = psiElement.getChildOfType<KDoc>();
                            if (kdoc != null) {
                                kdoc.replace(it)
                            } else {
                                psiElement.addAfter(it, null)
                            }
                        }
                        .let { CodeStyleManager.getInstance(project).reformat(it) }
                }
            }
        }
    }

    private fun getDocGenerator(project: Project, psiElement: PsiElement): KDocGenerator? {
        return when (psiElement) {
            is KtClass -> {
                ClassKDocGenerator(project, psiElement)
            }
            is KtNamedFunction -> {
                NamedFunctionKDocGenerator(project, psiElement)
            }
            is KtProperty -> {
                PropertyKDocGenerator(project, psiElement)
            }
            else -> null
        } as KDocGenerator
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