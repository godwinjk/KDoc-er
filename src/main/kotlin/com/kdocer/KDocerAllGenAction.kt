package com.kdocer

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
import org.jetbrains.kotlin.idea.kdoc.KDocElementFactory
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.kdoc.psi.impl.KDocSection
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.kotlin.psi.psiUtil.isPrivate


/**
 * Created by Godwin on 7/23/2020 9:16 PM.
 *
 * @author : Godwin Joseph Kurinjikattu
 * @since : 2020
 */
class KDocerAllGenAction : AnAction() {
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
                        if (!it.isPrivate()) {
                            psiElements.add(it)
                            getClasses(it)
                        } else {
                            arrayListOf()
                        }
                    }
                    is KtNamedFunction -> {
                        if (!it.isPrivate()) {
                            psiElements.add(it)
                            getFunctions(it)
                        } else {
                            arrayListOf()
                        }
                    }
//                    is KtProperty -> {
//                        arrayListOf(it)
//                    }
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
                        if (!it.isPrivate()) {
                            elements.add(it)
                            getClasses(it)
                        } else {
                            arrayListOf()
                        }
                    }
                    is KtClassBody -> getClasses(it)
                    is KtNamedFunction -> {
                        if (!it.isPrivate()) {
                            elements.add(it)
                            getFunctions(it)
                        } else {
                            arrayListOf()
                        }
                    }
//                    is KtProperty -> arrayListOf(it)
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
                        if (!it.isPrivate()) {
                            elements.add(it)
                            getClasses(it)
                        } else {
                            arrayListOf()
                        }
                    }
                    is KtNamedFunction -> {
                        if (!it.isPrivate()) {
                            elements.add(it)
                            getFunctions(it)
                        } else {
                            arrayListOf()
                        }
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
            element is KtFile
        ) {
            result = true
        }
        return result
    }
}