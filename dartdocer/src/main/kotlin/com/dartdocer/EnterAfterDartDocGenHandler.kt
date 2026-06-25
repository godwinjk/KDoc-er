package com.dartdocer

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.dartdocer.action.DartDocGenerationSupport
import com.dartdocer.generator.DartClassDocGenerator
import com.dartdocer.generator.DartFieldDocGenerator
import com.dartdocer.generator.DartFunctionDocGenerator
import com.jetbrains.lang.dart.psi.DartClassDefinition
import com.jetbrains.lang.dart.psi.DartFile
import com.jetbrains.lang.dart.psi.DartFunctionDeclarationWithBodyOrNative
import com.jetbrains.lang.dart.psi.DartGetterDeclaration
import com.jetbrains.lang.dart.psi.DartMethodDeclaration
import com.jetbrains.lang.dart.psi.DartVarDeclarationList

/**
 * Detects when the user types `///` and presses Enter in a Dart file.
 * If the `///` is the only doc comment line above a declaration, this handler
 * replaces it with a fully generated DartDoc comment.
 */
class EnterAfterDartDocGenHandler : EnterHandlerDelegateAdapter() {

    override fun postProcessEnter(
        file: PsiFile,
        editor: Editor,
        dataContext: DataContext,
    ): EnterHandlerDelegate.Result {
        if (file !is DartFile) return EnterHandlerDelegate.Result.Continue

        val project = file.project
        val document = editor.document
        val caretOffset = editor.caretModel.offset

        // Check if we're right after a /// line
        val caretLine = document.getLineNumber(caretOffset)
        if (caretLine < 1) return EnterHandlerDelegate.Result.Continue

        val prevLineNum = caretLine - 1
        val prevLineStart = document.getLineStartOffset(prevLineNum)
        val prevLineEnd = document.getLineEndOffset(prevLineNum)
        val prevLineText = document.getText(com.intellij.openapi.util.TextRange(prevLineStart, prevLineEnd)).trim()

        // Only trigger if the previous line is exactly "///" (empty doc comment start)
        if (prevLineText != "///") return EnterHandlerDelegate.Result.Continue

        PsiDocumentManager.getInstance(project).commitAllDocuments()

        // Find the declaration that follows (skip whitespace and the current comment line)
        val elementAtPrevLine = file.findElementAt(prevLineStart)
        if (elementAtPrevLine !is PsiComment) return EnterHandlerDelegate.Result.Continue

        // Find the next non-comment, non-whitespace sibling to determine the declaration
        var nextElement = elementAtPrevLine.nextSibling
        while (nextElement is PsiWhiteSpace || (nextElement is PsiComment && nextElement.text.trimStart().startsWith("///"))) {
            nextElement = nextElement.nextSibling
        }

        if (nextElement == null) return EnterHandlerDelegate.Result.Continue

        // Determine the declaration type and generate
        val generator = when (nextElement) {
            is DartFunctionDeclarationWithBodyOrNative -> DartFunctionDocGenerator(project, nextElement)
            is DartMethodDeclaration -> DartFunctionDocGenerator(project, nextElement)
            is DartClassDefinition -> DartClassDocGenerator(project, nextElement)
            is DartGetterDeclaration -> DartFieldDocGenerator(project, nextElement)
            is DartVarDeclarationList -> DartFieldDocGenerator(project, nextElement)
            else -> null
        } ?: return EnterHandlerDelegate.Result.Continue

        val docText = generator.generate()
        if (docText.isBlank()) return EnterHandlerDelegate.Result.Continue

        ApplicationManager.getApplication().runWriteAction {
            // Replace the "///" line and the current empty line with the generated doc
            val currentLineEnd = document.getLineEndOffset(caretLine)
            document.replaceString(prevLineStart, currentLineEnd, docText.trimEnd())

            // Move caret to end of first doc line
            val newFirstLineEnd = document.getLineEndOffset(prevLineNum)
            editor.caretModel.moveToOffset(newFirstLineEnd)
        }

        return EnterHandlerDelegate.Result.Continue
    }
}
