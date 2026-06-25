package com.rustdocer

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.rustdocer.generator.RustEnumDocGenerator
import com.rustdocer.generator.RustFnDocGenerator
import com.rustdocer.generator.RustImplDocGenerator
import com.rustdocer.generator.RustStructDocGenerator
import com.rustdocer.generator.RustTraitDocGenerator
import org.rust.lang.RsFileType
import org.rust.lang.core.psi.RsEnumItem
import org.rust.lang.core.psi.RsFile
import org.rust.lang.core.psi.RsFunction
import org.rust.lang.core.psi.RsImplItem
import org.rust.lang.core.psi.RsStructItem
import org.rust.lang.core.psi.RsTraitItem

/**
 * When the user types `///` and presses Enter inside a Rust file, this handler detects
 * the newly created doc comment line and replaces it with a fully generated RustDoc.
 */
class EnterAfterRustDocGenHandler : EnterHandlerDelegateAdapter() {

    override fun postProcessEnter(
        file: PsiFile,
        editor: Editor,
        dataContext: DataContext
    ): EnterHandlerDelegate.Result {

        if (file !is RsFile) {
            return EnterHandlerDelegate.Result.Continue
        }

        val caretModel = editor.caretModel
        val offset = caretModel.offset
        val project = file.project
        val documentManager = PsiDocumentManager.getInstance(project)
        documentManager.commitAllDocuments()

        // Check if the line above the caret is a lone `///` comment
        val document = editor.document
        val caretLine = document.getLineNumber(offset)
        if (caretLine < 1) return EnterHandlerDelegate.Result.Continue

        val prevLine = caretLine - 1
        val prevLineStart = document.getLineStartOffset(prevLine)
        val prevLineEnd = document.getLineEndOffset(prevLine)
        val prevLineText = document.getText(com.intellij.openapi.util.TextRange(prevLineStart, prevLineEnd)).trim()

        // Only trigger on a bare `///` (just the marker, no content yet)
        if (prevLineText != "///") {
            return EnterHandlerDelegate.Result.Continue
        }

        // Find the declaration following the comment
        val elementAfterCaret = file.findElementAt(offset)
        val declaration = findNextDeclaration(elementAfterCaret)
            ?: return EnterHandlerDelegate.Result.Continue

        val generated = when (declaration) {
            is RsFunction -> RustFnDocGenerator(project, declaration).generate()
            is RsStructItem -> RustStructDocGenerator(project, declaration).generate()
            is RsEnumItem -> RustEnumDocGenerator(project, declaration).generate()
            is RsTraitItem -> RustTraitDocGenerator(project, declaration).generate()
            is RsImplItem -> RustImplDocGenerator(project, declaration).generate()
            else -> return EnterHandlerDelegate.Result.Continue
        }

        ApplicationManager.getApplication().runWriteAction {
            // Remove the bare `///` line and the newline the Enter handler created
            val deleteStart = prevLineStart
            val deleteEnd = if (caretLine < document.lineCount) {
                document.getLineEndOffset(caretLine).coerceAtMost(document.textLength)
            } else {
                document.textLength
            }

            // Replace with generated doc
            val docText = generated.trimEnd('\n')
            document.replaceString(deleteStart, deleteEnd, docText)
            documentManager.commitDocument(document)

            // Move caret to end of first line
            val firstLineEnd = document.getLineEndOffset(prevLine)
            caretModel.moveToOffset(firstLineEnd)
        }

        return EnterHandlerDelegate.Result.Continue
    }

    private fun findNextDeclaration(element: PsiElement?): PsiElement? {
        var current = element
        while (current != null) {
            when (current) {
                is RsFunction, is RsStructItem, is RsEnumItem, is RsTraitItem, is RsImplItem ->
                    return current
            }
            current = current.nextSibling ?: current.parent?.nextSibling
        }
        return null
    }
}
