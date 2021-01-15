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
import com.kdocer.generator.ClassKDocGenerator
import com.kdocer.generator.KDocGenerator
import com.kdocer.generator.NamedFunctionKDocGenerator
import com.kdocer.generator.PropertyKDocGenerator
import com.kdocer.util.Constants
import com.kdocer.util.NotificationHelper
import com.kdocer.util.Validator
import org.jetbrains.kotlin.idea.kdoc.KDocElementFactory
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.kdoc.psi.impl.KDocSection
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType


/**
 * Created by Godwin on 7/23/2020 9:16 PM.
 *
 * @author : Godwin Joseph Kurinjikattu
 * @since : 2020
 */
class KDocerAllGenAction : AnAction() {

    override fun update(action: AnActionEvent) {
        super.update(action)
        val presentation = action.presentation

        val psiFile = action.getData(PlatformDataKeys.PSI_FILE) ?: return
        if (psiFile !is KtFile || !CodeInsightSettings.getInstance().SMART_INDENT_ON_ENTER) {
            println("This is not Kotlin file. ")

            presentation.isVisible = false
            presentation.isEnabled = false
            return
        }
        presentation.isVisible = true
        presentation.isEnabled = true
    }

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
                    is KtClass -> {
                        psiElements.add(it)
                        getClasses(it)
                    }
                    is KtNamedFunction -> {
                        psiElements.add(it)
                        getFunctions(it)
                    }
                    is KtProperty -> {
                        arrayListOf(it)
                    }
                    else -> arrayListOf()
                }
            )
        }

        psiElements.forEach {
            if (it is KtModifierListOwner && Validator.checkElementIsAllowed(it)) {
                processElement(it)
            }
        }
        NotificationHelper.showNotification(Constants.MESSAGE)
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
                }.let {
                    it.getChildOfType<KDocSection>()?.let {
//                    psiElement.moveToOffset(it.textOffset + 6)
                    }
                }
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
                    is KtClassOrObject -> {
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

    private fun getDocGenerator(project: Project, psiElement: PsiElement): KDocGenerator? {
        return when (psiElement) {
            is KtClassOrObject -> {
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
            element is KtFile
        ) {
            result = true
        }
        return result
    }
}