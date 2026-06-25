package com.kdocer.nlp

import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject

object SeeReferenceResolver {

    fun resolve(element: KtNamedDeclaration): List<String> {
        val refs = mutableListOf<String>()

        when (element) {
            is KtNamedFunction -> {
                if (element.hasModifier(org.jetbrains.kotlin.lexer.KtTokens.OVERRIDE_KEYWORD)) {
                    resolveOverriddenFunction(element)?.let { refs += it }
                }
            }
            is KtClassOrObject -> {
                resolveSuperTypes(element, refs)
            }
        }

        return refs
    }

    private fun resolveOverriddenFunction(function: KtNamedFunction): String? {
        val containingClass = function.containingClassOrObject ?: return null
        val supers = (containingClass as? KtClass)?.superTypeListEntries ?: return null
        if (supers.isEmpty()) return null
        val superName = supers.first().typeReference?.text?.substringBefore('<')?.trim() ?: return null
        return "$superName.${function.name}"
    }

    private fun resolveSuperTypes(element: KtClassOrObject, refs: MutableList<String>) {
        // Sealed subtypes → link to parent sealed type
        val parent = element.containingClassOrObject
        if (parent is KtClass && (parent.isSealed())) {
            parent.name?.let { refs += it }
            return
        }

        // Object implementing an interface or extending a class
        if (element is KtObjectDeclaration && !element.isCompanion()) {
            element.superTypeListEntries.forEach { entry ->
                entry.typeReference?.text?.substringBefore('<')?.trim()?.let { refs += it }
            }
        }
    }
}
