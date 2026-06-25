package com.dartdocer.nlp

import com.intellij.psi.PsiElement
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartClassDefinition
import com.jetbrains.lang.dart.psi.DartComponent
import com.jetbrains.lang.dart.psi.DartMethodDeclaration

/**
 * Resolves see-also references for Dart elements: superclass, implemented interfaces,
 * overrides to their super method.
 */
object DartSeeReferenceResolver {

    fun resolve(element: PsiElement): List<String> {
        val refs = mutableListOf<String>()

        when (element) {
            is DartMethodDeclaration -> {
                if (isOverride(element)) {
                    resolveOverriddenMethod(element)?.let { refs += it }
                }
            }
            is DartClassDefinition -> {
                resolveSuperTypes(element, refs)
            }
        }

        return refs
    }

    private fun isOverride(element: DartComponent): Boolean =
        element.metadataList.any { it.referenceExpression?.text == "override" }

    private fun resolveOverriddenMethod(method: DartMethodDeclaration): String? {
        val containingClass = method.parent?.parent as? DartClassDefinition ?: return null
        val superName = containingClass.superclass?.type?.referenceExpression?.text ?: return null
        return "$superName.${method.name}"
    }

    private fun resolveSuperTypes(element: DartClassDefinition, refs: MutableList<String>) {
        // Link to superclass
        element.superclass?.type?.referenceExpression?.text?.let { refs += it }
        // Link to implemented interfaces (DartClass.getImplementsList returns List<DartType>)
        (element as? DartClass)?.implementsList?.forEach { dartType ->
            dartType.referenceExpression?.text?.let { refs += it }
        }
        // Link to mixed-in types (DartClass.getMixinsList returns List<DartType>)
        (element as? DartClass)?.mixinsList?.forEach { dartType ->
            dartType.referenceExpression?.text?.let { refs += it }
        }
    }
}
