package com.kdocer

import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.*
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType

/**
 * Created by Godwin on 7/23/2020 9:16 PM.
 *
 * @author : Godwin Joseph Kurinjikattu
 * @since : 2020
 */
class KDocerAllRemoveAction : AnAction() {
    override fun actionPerformed(action: AnActionEvent) {
        val psiFile = action.getData(PlatformDataKeys.PSI_FILE) ?: return
        if (psiFile !is KtFile || !CodeInsightSettings.getInstance().SMART_INDENT_ON_ENTER) {
            println("This is not Kotlin file. ")
            return
        }

        val project = psiFile.project
        val documentManager = PsiDocumentManager.getInstance(project)
        documentManager.commitAllDocuments()

        if (!isAllowedElementType(psiFile)) return

        processFile(psiFile)
    }

    private fun processFile(file: KtFile) {
        val psiElements = ArrayList<PsiElement>()
        file.children.forEach {
            psiElements.addAll(
                when (it) {
                    is KtClass -> getClasses(it)
                    is KtNamedFunction -> getFunctions(it)
                    else -> arrayListOf()
                }
            )
        }

        psiElements.forEach {
            processElement(file, it)
        }
    }

    private fun processElement(file: KtFile, psiElement: PsiElement) {
        val project = psiElement.project

        ApplicationManager.getApplication().invokeLater {
            WriteCommandAction.runWriteCommandAction(project) {
                psiElement.getChildOfType<KDoc>()?.delete()
            }
        }
    }

    private fun getClasses(ktClass: PsiElement): List<PsiElement> {
        val elements = ArrayList<PsiElement>(1)
        ktClass.children.forEach {
            elements.addAll(
                when (it) {
                    is KtClass -> {
                        elements.add(it)
                        getClasses(it)
                    }
                    is KtClassBody -> getClasses(it)
                    is KtNamedFunction -> {
                        elements.add(it)
                        getFunctions(it)
                    }
                    is KtProperty -> arrayListOf(it)
                    else -> arrayListOf()
                }
            )
        }
        return elements.toList()
    }

    private fun getFunctions(ktNamedFunction: KtNamedFunction): List<PsiElement> {
        val elements = ArrayList<PsiElement>(1)
        ktNamedFunction.children.forEach {
            elements.addAll(
                when (it) {
                    is KtClass -> {
                        elements.add(it)
                        getClasses(it)
                    }
                    is KtNamedFunction -> {
                        elements.add(it)
                        getFunctions(it)
                    }
                    is KtBlockExpression -> {
                        getClasses(it)
                    }
                    else -> arrayListOf()
                }
            )
        }
        return elements.toList()
    }

    private fun isAllowedElementType(element: PsiElement): Boolean {
        var result = false
        if (element is PsiClass ||
            element is PsiField ||
            element is PsiMethod ||
            element is KtFile
        ) {
            result = true
        }
        return result
    }
}